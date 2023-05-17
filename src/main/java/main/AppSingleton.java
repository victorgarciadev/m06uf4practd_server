package main;

import common.Partida;
import common.PartidaException;
import common.SelectorParaules;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 *
 * @author GrupD
 */
@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class AppSingleton {

    private static final String APP_VERSION = "1.0.0";
    private static final String DATE_VERSION = "16/05/2023";

    private static final Logger log = Logger.getLogger(AppSingleton.class.getName());

    @Resource
    private TimerService timerService;

    // darrera actualitzacio de la BBDD
    private Date lastUpdateDBUTC;

    // moment d'inici de l'aplicacio
    private Date uptimeUTC;

    @PersistenceContext(unitName = "WordlePersistenceUnit")
    private EntityManager em;

    @PostConstruct //With this annotation, it'll be called by the container upon instantiation of the bean
    public void initialize() {
        this.showLogo();

        this.lastUpdateDBUTC = new Date();

        this.uptimeUTC = new Date();

        log.log(Level.INFO, "Inicialitzant AppSingleton.  lastUpdateDBUTC={0} , uptimeUTC={1}", new Object[]{this.lastUpdateDBUTC, this.uptimeUTC});
    }

    /**
     * *
     * Obté el timestamp d'inici de l'aplicació
     *
     * @return
     */
    @Lock(LockType.READ)
    public Date getUptimeUTC() {

        return this.uptimeUTC;
    }

    /**
     * *
     * Obté la darrera data d'actualització de la BBDD
     *
     * @return
     */
    @Lock(LockType.READ)
    public Date getLastDBUpdateUTC() {

        return this.lastUpdateDBUTC;
    }

    /**
     * *
     * Estableix la data d'actualització de la BBDD
     */
    @Lock(LockType.WRITE)
    public void setLastDBUpdateUTC() {

        this.lastUpdateDBUTC = new Date();

    }

    /**
     * *
     * Mostra un banner amb la versio
     */
    private void showLogo() {

        String fileName = File.separator + "files" + File.separator + "banner.txt";
        StringBuilder banner = new StringBuilder();

        try {
            // arrel del context de fitxers del war
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            // carreguem el fixter ubicat a resoources
            InputStream input = classLoader.getResourceAsStream(fileName);

            // llegim el contingut
            for (byte b : input.readAllBytes()) {
                banner.append((char) b);
            }

        } catch (IOException ex) {
            log.log(Level.WARNING, "Error llegint fitxer de logo: {0} : {1}", new Object[]{fileName, ex.toString()});
        }

        banner.append(System.lineSeparator());
        banner.append(System.lineSeparator());
        banner.append("Versió del servidor: " + AppSingleton.APP_VERSION + " de " + AppSingleton.DATE_VERSION);
        banner.append(System.lineSeparator());

        log.log(Level.INFO, banner.toString());
    }

    @Lock(LockType.WRITE)
    public Partida createPartida() throws PartidaException {
        if (getPartidaActual() != null) {
            throw new PartidaException("Ja hi ha una partida en marxa");
        }
        String[] dificultats = {"Facil", "Mig", "Alta"};
        Partida partida = new Partida();
        partida.setUsuaris(new ArrayList<>());
        partida.setDataPartida(Date.from(Instant.now()));
        partida.setActual(1);
        partida.setDificultat(dificultats[new Random().nextInt(dificultats.length)]);
        partida.setParaules(SelectorParaules.getLlistatParaules(partida.getDificultat()));
        partida.setComencada(0);
        log.log(Level.INFO, "Creada nova partida amb dificultat {0} i amb data de {1}", new Object[]{partida.getDificultat(), partida.getDataPartida()});
        Timer timer = timerService.createSingleActionTimer(300000, new TimerConfig());
        return partida;
    }

    @Lock(LockType.READ)
    public Partida getPartidaActual() {
        TypedQuery<Partida> query = em.createQuery("SELECT p FROM Partida p where p.actual = 1", Partida.class);
        Partida ret = null;

        try {
            Partida p = query.getSingleResult();
            ret = p;
        } catch (NoResultException ex) {
            log.log(Level.INFO, "getPartidaActual() --> no hi ha cap partida en marxa");
            return null;
        }

        return ret;
    }

    @Timeout
    public void timeout(Timer timer) {
        log.log(Level.INFO, "TimerBean: timeout occurred");
        finalitzaPartida();
    }

    public void finalitzaPartida() {
        Partida pActual = getPartidaActual();
        if (pActual != null) {
            pActual.setActual(0);
            try {
                em.merge(pActual);
                log.log(Level.INFO, "Nova partida finalitzada correctament amb ID: {0}", pActual.getId());
                em.createQuery("DELETE FROM SalaEspera").executeUpdate();
                log.log(Level.INFO, "Taula SalaEspera esborrada");
            } catch (Exception ex) {
                log.log(Level.SEVERE, "Error al finalitzar la partida o a l'esborrar la taula SalaEspera: {0}", ex.toString());
            }
        }
    }
}

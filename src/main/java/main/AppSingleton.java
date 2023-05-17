package main;

import common.Partida;
import common.PartidaException;
import common.SelectorParaules;
import common.Usuari;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
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
    public Partida createPartida(List<Usuari> usuaris) throws PartidaException {
        if (getPartidaActual() != null) {
            throw new PartidaException("Ja hi ha una partida en marxa");
        }
        String[] dificultats = {"Facil", "Mig", "Alta"};
        Partida partida = new Partida();
        partida.setUsuaris(usuaris);
        partida.setDataPartida(Date.from(Instant.now()));
        partida.setActual(true);
        partida.setDificultat(dificultats[new Random().nextInt(dificultats.length)]);
        partida.setParaules(SelectorParaules.getLlistatParaules(partida.getDificultat()));
        log.log(Level.INFO, "Creada nova partida amb dificultat {0} i amb data de {1}", new Object[]{partida.getDificultat(), partida.getDataPartida()});
        return partida;
    }
    
    @Lock(LockType.READ)
    public Partida getPartidaActual() {
        TypedQuery<Partida> query = em.createQuery("SELECT p FROM Partida p where p.actual = true", Partida.class);
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
}

package main;

import common.Partida;
import common.PartidaException;
import java.io.File;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.annotation.Resource;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import java.util.Timer;
import java.util.TimerTask;
import javax.ejb.Timeout;

/**
 *
 * @author GrupD
 */
@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class AppSingletonEJB {

    private static final String APP_VERSION = "0.1";
    private static final String DATE_VERSION = "02/05/2023";

    private static final Logger log = Logger.getLogger(AppSingletonEJB.class.getName());

    // darrera actualitzacio de la BBDD
    private Date lastUpdateDBUTC;

    // moment d'inici de l'aplicacio
    private Date uptimeUTC;

    @PersistenceContext(unitName = "WordlePersistenceUnit")
    private EntityManager em;

    @EJB
    private PartidaEJB partidaEJB;

    @Resource
    private TimerService timerService;

    @PostConstruct //With this annotation, it'll be called by the container upon instantiation of the bean
    public void initialize() {
        this.showLogo();

        this.lastUpdateDBUTC = new Date();

        this.uptimeUTC = new Date();

        log.log(Level.INFO, "Inicialitzant AppSingletonEJB.  lastUpdateDBUTC={0} , uptimeUTC={1}", new Object[]{this.lastUpdateDBUTC, this.uptimeUTC});
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
     * Mostra un banner amb la versió
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

        } catch (Exception ex) {
            log.log(Level.WARNING, "Error llegint fitxer de logo: {0} : {1}", new Object[]{fileName, ex.toString()});
        }

        banner.append(System.lineSeparator());
        banner.append(System.lineSeparator());
        banner.append("Versió del servidor: " + AppSingletonEJB.APP_VERSION + " de " + AppSingletonEJB.DATE_VERSION);
        banner.append(System.lineSeparator());

        log.log(Level.INFO, banner.toString());
    }

    @Lock(LockType.WRITE)
    public void createPartida() throws PartidaException {
        if (partidaEJB.getPartidaActual() != null) {
            throw new PartidaException("Ja hi ha una partida en marxa");
        }
        Partida partida = new Partida();
        partida.setDataPartida(Timestamp.from(Instant.now()));
        partida.setActual(true);
        partida.setDificultat(dificultatRandom());
        em.persist(partida);

        timerService.createSingleActionTimer(Duration.ofMinutes(3).toMillis(), new TimerConfig(partida, true));
    }

    @Timeout
    public void handleTimeout(Timer timer) {
        int partidaId = partidaEJB.getPartidaActual().getId();

        Partida partida = em.find(Partida.class, partidaId);
        partida.setActual(false);

        em.merge(partida);

        //crearWaitingRoom();
    }

    public void crearWaitingRoom() {
        TimerConfig timerConfig = new TimerConfig();
        timerService.createSingleActionTimer(Duration.ofMinutes(2).toMillis(), timerConfig);
    }

    public String dificultatRandom() {
        Random rand = new Random();
        int randNum = rand.nextInt(3);
        switch (randNum) {
            case 0:
                return "Facil";
            case 1:
                return "Mig";
            case 2:
                return "Alta";
            default:
                return "Mig";
        }
    }
}

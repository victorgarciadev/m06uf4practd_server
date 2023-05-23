package main;

import common.Partida;
import common.PartidaException;
import common.SalaEspera;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
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

/**
 *
 * @author pablomorante
 */
@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class WaitingSingleton {

    private static final Logger log = Logger.getLogger(WaitingSingleton.class.getName());

    @Resource
    private TimerService timerService;

    @PersistenceContext(unitName = "WordlePersistenceUnit")
    private EntityManager em;

    @EJB
    private AppSingleton gameSingleton;

    @PostConstruct //With this annotation, it'll be called by the container upon instantiation of the bean
    public void initialize() {
        log.log(Level.INFO, "Inicialitzant WaitingSingleton.");
    }

    /**
     * Mètode que comença la partida dins la waitingRoom per preparar la següent fase,
     * canviar l'estat de la partida a començada i posara l'hora final de la partida
     */
    @Lock(LockType.WRITE)
    public void waitingRoom() {
        Partida p = gameSingleton.getPartidaActual(false);
        if (p.getComencada() == 0) {
            Date dataComencada = new Date();
            dataComencada.setMinutes(dataComencada.getMinutes() + 3);
            p.setComencada(1);
            p.setDataPartida(dataComencada);
            em.merge(p);
            log.log(Level.INFO, "waitingRoom() --> partida començada");
        }
    }

    /**
     * Mètode que retorna els temps restant que falta per canviar de pantalla
     * @param pantalla pantalla actual
     * @return int temps d'espera
     */
    @Lock(LockType.WRITE)
    public int timeRemaining(String pantalla) {
        SalaEspera se = getSalaEsperaActual(pantalla);
        Date horaComenca = se.getHoraComenca();
        log.log(Level.INFO, ">>>> Hora comença " + horaComenca);
        Date actualDate = new Date();
        log.log(Level.INFO, ">>> hora actual " + actualDate);
        Instant startDate = horaComenca.toInstant();
        Instant now = actualDate.toInstant();
        Duration duration = Duration.between(now, startDate);
        log.log(Level.INFO, ">>> diferencia " + duration.getSeconds());
        return (int) duration.getSeconds();

    }
    
    /**
     * Mètode que retorna la sala d'espera actual: si no hi ha cap creada crea una nova,
     * si no, depenent de la pantalla retorna un temps d'espera o un altre
     * @param pantalla pantalla actual
     * @return SalaEspera temps d'espera
     */
    @Lock(LockType.WRITE)
    public SalaEspera getSalaEsperaActual(String pantalla) {
        Partida p = gameSingleton.getPartidaActual(false);
        if (p == null) {
            try {
                SalaEspera se = em.createQuery("SELECT se FROM SalaEspera se", SalaEspera.class).getSingleResult();
                return se;
            } catch (NoResultException ex) {
                Date dateComenca = new Date();
                dateComenca.setMinutes(dateComenca.getMinutes() + 2);
                SalaEspera se = new SalaEspera(dateComenca);
                try {
                    em.persist(se);
                    gameSingleton.createPartida();
                    TimerConfig timerConfig = new TimerConfig();
                    timerConfig.setPersistent(false);
                    Timer timer = timerService.createSingleActionTimer(115000, timerConfig);
                    return se;
                } catch (PartidaException e) {
                    log.log(Level.SEVERE, "Error de persist\u00e8ncia a la base de dades: {0}", e.toString());
                    return null;
                }
            }
        } else {
            if (p.getComencada() == 1) {
                Date newDate = p.getDataPartida();
                if (pantalla.equals("joc")) {
                    newDate.setMinutes(newDate.getMinutes());
                } else {
                    newDate.setMinutes(newDate.getMinutes() + 5);
                }
                return new SalaEspera(newDate);
            } else {
                SalaEspera se = em.createQuery("SELECT se FROM SalaEspera se", SalaEspera.class).getSingleResult();
                return se;
            }
        }
    }

    /**
     * Timeout que finalitza la waitingRoom després de 115000 milisegons.
     *
     * @param timer
     */
    @Timeout
    public void timeout(Timer timer) {
        log.log(Level.INFO, "TimerBean: timeout occurred");
        waitingRoom();
    }

}

package main;

import common.IPartida;
import common.Partida;
import common.PartidaException;
import common.PartidaPuntuacio;
import common.Usuari;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Stateful;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

/**
 *
 * @author GrupD
 */
@Stateful
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@TransactionManagement(value = TransactionManagementType.CONTAINER)
public class PartidaEJB implements IPartida {

    @PersistenceContext(unitName = "WordlePersistenceUnit")
    private EntityManager em;

    @Inject
    private UserTransaction userTransaction;

    @Inject
    private AppSingletonEJB gameSingleton;

    @EJB
    private UsuariEJB usuariEJB;

    private static final Logger log = Logger.getLogger(PartidaEJB.class.getName());
    private final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();

    @Override
    @Lock(LockType.WRITE)
    public void actualitzarPuntuacio(String nomJugador, int punts) throws PartidaException {

        if (nomJugador == null || nomJugador.isBlank() || nomJugador.isEmpty()) {
            throw new PartidaException("Jugador no vàlid");
        }

        Usuari jugador = usuariEJB.getUsuari(nomJugador);

        Partida partida = gameSingleton.getPartidaActual();

        PartidaPuntuacio puntuacio = em.createQuery(
                "SELECT pp FROM PartidaPuntuacio pp WHERE pp.usuari = :usuari AND pp.partida = :partida",
                PartidaPuntuacio.class)
                .setParameter("usuari", jugador)
                .setParameter("partida", partida)
                .getResultList()
                .stream()
                .findFirst()
                .orElse(null);

        if (puntuacio != null) {
            puntuacio.setPunts(punts);
            em.merge(puntuacio); //canviar després perquè passi per persisteixTransaccio
        } else {
            PartidaPuntuacio novaPuntuacio = new PartidaPuntuacio();
            novaPuntuacio.setPartida(partida);
            novaPuntuacio.setUsuari(jugador);
            novaPuntuacio.setPunts(punts);
            em.persist(novaPuntuacio); //canviar després perquè passi per persisteixTransaccio
        }
    }

    @Override
    @Lock(LockType.WRITE)
    public void novaPartida() {
        try {
            Partida partida = gameSingleton.createPartida();
            em.persist(partida);

            ses.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    finalitzaPartida();
                }
            }, 0, 180, TimeUnit.SECONDS
            );
        } catch (PartidaException ex) {
            // controlar que la partida ja existeix
        }
    }

    @Override
    @Lock(LockType.WRITE)
    public void finalitzaPartida() {
        Partida pActual = gameSingleton.getPartidaActual();
        if (pActual != null) {
            pActual.setActual(false);
            em.merge(pActual);
            waitingRoom();
        }
    }

    @Override
    public List<Usuari> getLlistatJugadors() {
        Partida p = gameSingleton.getPartidaActual();

        if (p == null) {
            return null;
        }

        return p.getUsuaris();
    }

    @Override
    @Lock(LockType.WRITE)
    public void afegirJugador(Usuari nomJugador) {
        Partida p = gameSingleton.getPartidaActual();

        if (p == null) {
            // logica per si s'entra a afegirJugador quan no hi ha partida actual
            return;
        }

        List<Usuari> listJugador = p.getUsuaris();
        listJugador.add(nomJugador);
        p.setUsuaris(listJugador);
        em.merge(p);
    }

    @Override
    public String comprovarParaula(String paraula, int ronda, Usuari nomJugador) {
        Partida p = gameSingleton.getPartidaActual();
        List<String> paraules = p.getParaules();
        String pActual = paraules.get(ronda);
        String result = "";
        
        for (int i = 0; i < pActual.length(); i++) {
            char targetChar = pActual.charAt(i);
            char guessChar = paraula.charAt(i);
            if (targetChar == guessChar) {
                result += targetChar;
            } else if (pActual.contains(String.valueOf(guessChar))) {
                result += "+";
            } else {
                result += "-";
            }
        }
        
        return result;
    }

    @Override
    public void waitingRoom() {
        ses.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                List<Usuari> usuaris = usuariEJB.getUsuarisEsperant();
                if (usuaris.isEmpty()) {
                    waitingRoom();
                } else {
                    novaPartida();
                }
            }
        }, 0, 120, TimeUnit.SECONDS
        );

    }

    private Object persisteixTransaccio(Object ob) throws PartidaException {
        List<String> errors = Validadors.validaBean(ob);

        if (errors.isEmpty()) {
            try {
                userTransaction.begin();
                em.persist(ob);
                userTransaction.commit();
            } catch (SystemException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | javax.transaction.NotSupportedException | javax.transaction.RollbackException ex) {
                String msg = "Error desant: " + errors.toString();
                log.log(Level.INFO, msg);
                throw new PartidaException(msg);
            }
        } else {
            String msg = "Errors de validació: " + errors.toString();
            log.log(Level.INFO, msg);
            throw new PartidaException(msg);

        }

        return ob;
    }
}

package main;

import common.IPartida;
import common.IUsuari;
import common.Lookups;
import common.Partida;
import common.PartidaException;
import common.PartidaPuntuacio;
import common.Usuari;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Stateful;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.naming.NamingException;
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
@TransactionManagement(value = TransactionManagementType.BEAN)
public class PartidaEJB implements IPartida {

    @PersistenceContext(unitName = "WordlePersistenceUnit")
    private EntityManager em;

    @Inject
    private UserTransaction userTransaction;

    @EJB
    private AppSingleton gameSingleton;

    private static final Logger log = Logger.getLogger(PartidaEJB.class.getName());
    private final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> waitingTime;

    @PostConstruct
    public void onStartup() {
        log.log(Level.INFO, "Inicialitzant programa... iniciant sala d'espera");
        waitingRoom();
    }

    @Override
    public void actualitzarPuntuacio(String nomJugador, String resultat, int ronda, double temps) throws PartidaException {

        try {
            int punts = calcularPuntsRonda(resultat, ronda);
            IUsuari usuariEJB = Lookups.usuariEJBRemoteLookup();
            
            if (nomJugador == null || nomJugador.isBlank() || nomJugador.isEmpty()) {
                log.log(Level.WARNING, "actualitzarPuntuacio() --> Nom del jugador no vàlid");
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
                puntuacio.setPunts(puntuacio.getPunts() + punts);
                puntuacio.setRonda(ronda);
                if (temps < puntuacio.getMenorTempsEncert() && punts > 0) {
                    puntuacio.setMenorTempsEncert(temps);
                }
                try {
                    mergeTransaccio(puntuacio);
                    log.log(Level.INFO, "Puntuaci\u00f3 de l''usuari {0}actualitzada correctament", nomJugador);
                } catch (PartidaException ex) {
                    log.log(Level.SEVERE, "Error de persist\u00e8ncia a l''actualitzar la puntuaci\u00f3 del jugador {0}:{1}", new Object[]{nomJugador, ex.toString()});
                    throw new PartidaException("Error de persistència a la base de dades: " + ex.toString());
                }
            } else {
                PartidaPuntuacio novaPuntuacio = new PartidaPuntuacio();
                novaPuntuacio.setPartida(partida);
                novaPuntuacio.setUsuari(jugador);
                novaPuntuacio.setPunts(punts);
                novaPuntuacio.setMenorTempsEncert(temps);
                novaPuntuacio.setRonda(ronda);
                try {
                    persisteixTransaccio(novaPuntuacio);
                    log.log(Level.INFO, "Puntuaci\u00f3 de l''usuari {0}actualitzada correctament", nomJugador);
                } catch (PartidaException ex) {
                    log.log(Level.SEVERE, "Error de persist\u00e8ncia a l''actualitzar la puntuaci\u00f3 del jugador {0}:{1}", new Object[]{nomJugador, ex.toString()});
                    throw new PartidaException("Error de persistència a la base de dades: " + ex.toString());
                }
            }
        } catch (NamingException ex) {
            throw new PartidaException("Error de beans intern: " + ex.toString());
        }
    }

    @Override
    public void novaPartida(List<Usuari> usuaris) {
        try {
            Partida partida = gameSingleton.createPartida(usuaris);
            try {
                persisteixTransaccio(partida);
                log.log(Level.INFO, "Nova partida comen\u00e7ada correctament amb ID: {0}", partida.getId());
            } catch (PartidaException ex) {
                log.log(Level.SEVERE, "Error al crear nova partida: {0}", ex.toString());
            }

            waitingTime = ses.scheduleWithFixedDelay(this::finalitzaPartida, 0, 180, TimeUnit.SECONDS
            );
        } catch (PartidaException ex) {
            log.log(Level.WARNING, "Ja hi ha en marxa una partida: {0}", ex.toString());
        }
    }

    @Override
    @Lock(LockType.WRITE)
    public void finalitzaPartida() {
        Partida pActual = gameSingleton.getPartidaActual();
        if (pActual != null) {
            pActual.setActual(false);
            try {
                mergeTransaccio(pActual);
                log.log(Level.INFO, "Nova partida finalitzada correctament amb ID: {0}", pActual.getId());
            } catch (PartidaException ex) {
                log.log(Level.SEVERE, "Error al finalitzar la partida: {0}", ex.toString());
            }
            waitingRoom();
        }
    }

    @Override
    public List<Usuari> getLlistatJugadors() {
        Partida p = gameSingleton.getPartidaActual();

        if (p == null) {
            log.log(Level.INFO, "getLlistatJugadors() --> no hi ha cap partida en marxa");
            return null;
        }

        return p.getUsuaris();
    }

    @Override
    public List<Usuari> afegirJugadors() throws PartidaException {
        List<Usuari> jugadors = new ArrayList<>();
        try {
            IUsuari usuariEJB = Lookups.usuariEJBRemoteLookup();
            jugadors = usuariEJB.getUsuarisEsperant();

            if (jugadors.isEmpty()) {
                log.log(Level.INFO, "afegirJugadors() --> no hi ha cap jugador esperant");
                return null;
            }

            return jugadors;
        } catch (NamingException ex) {
            log.log(Level.WARNING, "Error intern d'EJB: {0}", ex.toString());
            throw new PartidaException("Error de beans intern: " + ex.toString());
        }
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
        log.log(Level.INFO, "Comprovada correctament la paraula enviada ''{0}'' per l''usuari {1}", new Object[]{paraula, nomJugador});

        return result;
    }

    @Override
    public List<Usuari> getJugadorsPartidaAmbPuntuacio(Partida p) {
        List<Usuari> ret = new ArrayList();
        List<PartidaPuntuacio> jugadors = em.createQuery(
                "SELECT pp FROM PartidaPuntuacio pp WHERE pp.partida = :partida",
                PartidaPuntuacio.class)
                .setParameter("partida", p)
                .getResultList();

        for (PartidaPuntuacio pp : jugadors) {
            ret.add(new Usuari(pp.getUsuari().getNickname(), pp.getPunts()));
        }

        return ret;
    }

    public List<PartidaPuntuacio> getJugadorsPerRonda(Partida p, int ronda) {
        List<PartidaPuntuacio> jugadors = em.createQuery("SELECT pp FROM PartidaPuntuacio pp WHERE pp.partida = :partida AND pp.ronda = :ronda", PartidaPuntuacio.class)
                .setParameter("partida", p).setParameter("ronda", ronda).getResultList();

        return jugadors;
    }

    @Override
    @Lock(LockType.WRITE)
    public void waitingRoom() {
        waitingTime = ses.scheduleWithFixedDelay(() -> {
            List<Usuari> usuaris = new ArrayList();
            try {
                usuaris = afegirJugadors();
            } catch (PartidaException ex) {
                log.log(Level.SEVERE, "Error intern d'EJB: {0}", ex.toString());
            }
            if (usuaris.isEmpty()) {
                log.log(Level.INFO, "waitingRoom() --> la sala està buida, esperant més jugadors...");
                waitingRoom();
            } else {
                log.log(Level.INFO, "waitingRoom() --> creant nova partida amb {0} jugadors.", usuaris.size());
                novaPartida(usuaris);
            }
        }, 0, 120, TimeUnit.SECONDS
        );
    }

    @Override
    public Long timeRemaining() {
        if (waitingTime != null) {
            return TimeUnit.MILLISECONDS.toSeconds(waitingTime.getDelay(TimeUnit.MILLISECONDS));
        } else {
            return 0L;
        }
    }

    private int calcularPuntsRonda(String resultat, int ronda) {
        int punts = 0;
        Partida p = gameSingleton.getPartidaActual();
        List<String> paraules = p.getParaules();
        String pActual = paraules.get(ronda);

        if (pActual.equals(resultat)) {
            punts += resultat.length();
            List<PartidaPuntuacio> jugadorsRondaAcabada = getJugadorsPerRonda(p, ronda);
            if (jugadorsRondaAcabada.isEmpty()) {
                punts += 10;
            } else if (jugadorsRondaAcabada.size() == 1) {
                punts += 8;
            } else if (jugadorsRondaAcabada.size() == 2) {
                punts += 6;
            }
        } else {
            for (int i = 0; i < pActual.length(); i++) {
                char targetChar = pActual.charAt(i);
                char guessChar = resultat.charAt(i);
                if (targetChar == guessChar) {
                    punts++;
                }
            }
        }

        return punts;
    }

    private void persisteixTransaccio(Object ob) throws PartidaException {
        List<String> errors = Validadors.validaBean(ob);

        if (errors.isEmpty()) {
            try {
                userTransaction.begin();
                em.persist(ob);
                userTransaction.commit();
                log.log(Level.INFO, "Dades desades correctament a la BD --> {0}", ob.toString());
            } catch (SystemException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | javax.transaction.NotSupportedException | javax.transaction.RollbackException ex) {
                log.log(Level.WARNING, "Error desant dades a la BD: {0}", ex.toString());
                throw new PartidaException("Error desant: " + ex.toString());
            }
        } else {
            log.log(Level.WARNING, "Error de validaci\u00f3 de dades: {0}", errors.toString());
            throw new PartidaException("Error de validació de dades: " + errors.toString());

        }

//        return ob;
    }

    private void mergeTransaccio(Object ob) throws PartidaException {
        List<String> errors = Validadors.validaBean(ob);

        if (errors.isEmpty()) {
            try {
                userTransaction.begin();
                em.merge(ob);
                userTransaction.commit();
                log.log(Level.INFO, "Dades desades correctament a la BD --> {0}", ob.toString());
            } catch (SystemException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | javax.transaction.NotSupportedException | javax.transaction.RollbackException ex) {
                log.log(Level.WARNING, "Error desant dades a la BD: {0}", ex.toString());
                throw new PartidaException("Error desant: " + ex.toString());
            }
        } else {
            log.log(Level.WARNING, "Error de validaci\u00f3 de dades: {0}", errors.toString());
            throw new PartidaException("Error de validació de dades: " + errors.toString());
        }

//        return ob;
    }
}

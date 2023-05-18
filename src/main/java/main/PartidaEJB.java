package main;

import common.IPartida;
import common.IUsuari;
import common.Lookups;
import common.Partida;
import common.PartidaException;
import common.PartidaPuntuacio;
import common.SalaEspera;
import common.Usuari;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
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
    private SalaEspera se;

    @PostConstruct
    public void onStartup() {
        checkPartida("hall");
    }

    @Override
    public void checkPartida(String pantalla) {
        if (gameSingleton.getPartidaActual(false) == null) {
            log.log(Level.INFO, "Inicialitzant programa... iniciant sala d'espera");
            try {
                se = em.createQuery("SELECT se FROM SalaEspera se", SalaEspera.class).getSingleResult();
                waitingRoom();
            } catch (NoResultException ex) {
                Date dateComenca = new Date();
                dateComenca.setMinutes(dateComenca.getMinutes() + 2);
                se = new SalaEspera(dateComenca);
                try {
                    persisteixTransaccio(se);
                    novaPartida();
                } catch (PartidaException e) {
                    log.log(Level.SEVERE, "Error de persist\u00e8ncia a la base de dades: {0}", e.toString());
                }
            }
        } else {
            log.log(Level.INFO, "Ja hi ha una partida iniciada");
            Partida p = gameSingleton.getPartidaActual(false);
            if (p.getComencada() == 1) {
                Date newDate = p.getDataPartida();
                if (pantalla.equals("joc")) {
                    newDate.setMinutes(newDate.getMinutes() + 5);
                } else {
                    newDate.setMinutes(newDate.getMinutes() + 7);
                }
                se = new SalaEspera(newDate);
            } else {
                se = em.createQuery("SELECT se FROM SalaEspera se", SalaEspera.class).getSingleResult();
                waitingRoom();
            }
        }
    }

    @Override
    public void actualitzarPuntuacio(Usuari jugador, String resultat, int ronda, double temps) throws PartidaException {

        int punts = calcularPuntsRonda(resultat, ronda);

        Partida partida = gameSingleton.getPartidaActual(false);

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
                log.log(Level.INFO, "Puntuaci\u00f3 de l''usuari {0}actualitzada correctament", jugador.getNickname());
            } catch (PartidaException ex) {
                log.log(Level.SEVERE, "Error de persist\u00e8ncia a l''actualitzar la puntuaci\u00f3 del jugador {0}:{1}", new Object[]{jugador.getNickname(), ex.toString()});
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
                log.log(Level.INFO, "Puntuaci\u00f3 de l''usuari {0}actualitzada correctament", jugador.getNickname());
            } catch (PartidaException ex) {
                log.log(Level.SEVERE, "Error de persist\u00e8ncia a l''actualitzar la puntuaci\u00f3 del jugador {0}:{1}", new Object[]{jugador.getNickname(), ex.toString()});
                throw new PartidaException("Error de persistència a la base de dades: " + ex.toString());
            }
        }
    }

    @Override
    public void novaPartida() {
        try {
            Partida partida = gameSingleton.createPartida();
            try {
                persisteixTransaccio(partida);
                log.log(Level.INFO, "Nova partida comen\u00e7ada correctament amb ID: {0}", partida.getId());
            } catch (PartidaException ex) {
                log.log(Level.SEVERE, "Error al crear nova partida: {0}", ex.toString());
            }
        } catch (PartidaException ex) {
            log.log(Level.WARNING, "Ja hi ha en marxa una partida: {0}", ex.toString());
        }
    }

    @Override
    public void afegirJugador(IUsuari usuariEJB, String email) throws PartidaException {
        Usuari jugador = new Usuari();

        jugador = usuariEJB.getUsuari(email);
        Partida partida = gameSingleton.getPartidaActual(false);
        PartidaPuntuacio pp = new PartidaPuntuacio(partida, jugador, 0, 180d, 0);
        persisteixTransaccio(pp);
        log.log(Level.INFO, "Jugador afegit a la partida correctament");
    }

    @Override
    public String comprovarParaula(String paraula, int ronda, Usuari nomJugador) {
        List<String> paraules = getParaulesPartida();
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
        int timeRemaining;
        do {
            timeRemaining = timeRemaining();
            log.log(Level.INFO, "esperant per començar la partida ... " + timeRemaining);
        } while (timeRemaining > 0);
        Partida p = gameSingleton.getPartidaActual(false);
        p.setComencada(1);
        try {
            mergeTransaccio(p);
            log.log(Level.INFO, "waitingRoom() --> partida començada");
        } catch (PartidaException ex) {
            log.log(Level.SEVERE, "Error al posar la partida en marxa: {0}", ex.toString());
        }
    }

    @Override
    public int timeRemaining() {
        if (se != null) {
            Date horaComenca = se.getHoraComenca();
            log.log(Level.INFO, ">>>> Hora comença " + horaComenca);
            Date actualDate = new Date();
            log.log(Level.INFO, ">>> hora actual " + actualDate);
            Instant startDate = horaComenca.toInstant();
            Instant endDate = actualDate.toInstant();
            Duration duration = Duration.between(endDate, startDate);
            log.log(Level.INFO, ">>> diferencia " + duration.getSeconds());
            return (int) duration.getSeconds();
        } else {
            return 0;
        }
    }

    @Override
    public String getDificultatPartidaActual() {
        Partida p = gameSingleton.getPartidaActual(false);
        log.log(Level.INFO, "Partida acutal --> {0}", p);
        log.log(Level.INFO, "dificultat --> {0}", p.getDificultat());
        return p.getDificultat();
    }

    @Override
    public List<String> getParaulesPartida() {
        Partida p = gameSingleton.getPartidaActual(true);
        log.log(Level.INFO, "Paraules partida --> {0}", p.getParaules());
        List<String> nestedList = p.getParaules();
        List<String> flattenedList = nestedList.stream()
                .collect(Collectors.toList());
        log.log(Level.INFO, "Paraules sense array --> {0}", flattenedList);
        return flattenedList;
    }

    private int calcularPuntsRonda(String resultat, int ronda) {
        int punts = 0;
        Partida p = gameSingleton.getPartidaActual(true);
        List<String> paraules = p.getParaules();
        String pActual = paraules.get(ronda);

        if (pActual.equals(resultat)) {
            punts += resultat.length();
            List<PartidaPuntuacio> jugadorsRondaAcabada = getJugadorsPerRonda(p, ronda+1);
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

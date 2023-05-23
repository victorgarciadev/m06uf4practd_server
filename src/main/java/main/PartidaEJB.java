package main;

import common.IPartida;
import common.Partida;
import common.PartidaException;
import common.PartidaPuntuacio;
import common.Usuari;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Remove;
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
@TransactionManagement(value = TransactionManagementType.BEAN)
public class PartidaEJB implements IPartida {

    @PersistenceContext(unitName = "WordlePersistenceUnit")
    private EntityManager em;

    @Inject
    private UserTransaction userTransaction;

    @EJB
    private AppSingleton gameSingleton;

    @EJB
    private WaitingSingleton waitingRoom;

    private static final Logger log = Logger.getLogger(PartidaEJB.class.getName());

    @Remove
    @Override
    public void tancaSessio() {
        log.log(Level.INFO, "Finalitzant sessió de PartidaEJB...");
    }

    @PreDestroy
    public void destroy() {
        log.log(Level.INFO, "UsuariEJB finalitzant...");
    }

    @Override
    public void checkPartida(String pantalla) {
        //waitingRoom.getSalaEsperaActual(pantalla);
    }

    @Override
    @Lock(LockType.WRITE)
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
            if (ronda == 0 && punts > 0) {
                puntuacio.setMenorTempsEncert(temps);
            }
            actualitzarPuntuacioUsuari(jugador, jugador.getPuntuacio() + puntuacio.getPunts());
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
            actualitzarPuntuacioUsuari(jugador, jugador.getPuntuacio() + punts);
            try {
                persisteixTransaccio(novaPuntuacio);
                log.log(Level.INFO, "Puntuaci\u00f3 de l''usuari {0}actualitzada correctament", jugador.getNickname());
            } catch (PartidaException ex) {
                log.log(Level.SEVERE, "Error de persist\u00e8ncia a l''actualitzar la puntuaci\u00f3 del jugador {0}:{1}", new Object[]{jugador.getNickname(), ex.toString()});
                throw new PartidaException("Error de persistència a la base de dades: " + ex.toString());
            }
        }
    }

    /**
     * Actualitza la puntuació total de l'usuari
     *
     * @param usuari usuari actual
     * @param puntuacio puntuació a afegir a l'usuari
     */
    public void actualitzarPuntuacioUsuari(Usuari usuari, int puntuacio) {
        log.log(Level.INFO, "Actualizant puntuaci\u00f3 de l'' usuari {0}...", usuari.getNickname());
        usuari.setPuntuacio(puntuacio);
        try {
            mergeTransaccio(usuari);
        } catch (PartidaException ex) {
            log.log(Level.SEVERE, "Error de persist\u00e8ncia a l''actualitzar la puntuaci\u00f3 del jugador {0}:{1}", new Object[]{usuari.getNickname(), ex.toString()});
        }
    }

    @Override
    public void afegirJugador(Usuari jugador) throws PartidaException {
        Partida partida = gameSingleton.getPartidaActual(false);
        PartidaPuntuacio pp = new PartidaPuntuacio(partida, jugador, 0, 180d, 0);
        persisteixTransaccio(pp);
        log.log(Level.INFO, "Jugador afegit a la partida correctament");
    }

    @Override
    public String comprovarParaula(String paraula, int ronda, Usuari nomJugador) {
        List<String> paraules = getParaulesPartida();
        paraula = paraula.toLowerCase();
        String words = paraules.get(0);
        String pActual = getParaulaRonda(words, ronda);
        String result = "";
        log.log(Level.INFO, "paraula comprovant --> {0}", pActual);

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

    /**
     * Retorna els jugadors que han pasat de ronda.
     *
     * @param p partida actual
     * @param ronda número de ronda
     * @return llista de jugadors
     */
    public List<PartidaPuntuacio> getJugadorsPerRonda(Partida p, int ronda) {
        List<PartidaPuntuacio> jugadors = em.createQuery("SELECT pp FROM PartidaPuntuacio pp WHERE pp.partida = :partida AND pp.ronda = :ronda", PartidaPuntuacio.class)
                .setParameter("partida", p).setParameter("ronda", ronda).getResultList();

        return jugadors;
    }

    @Override
    public int timeRemaining(String pantalla) {
        return waitingRoom.timeRemaining(pantalla);
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
        List<String> nestedList = p.getParaules();
        List<String> flattenedList = nestedList.stream()
                .collect(Collectors.toList());
        log.log(Level.INFO, "Paraules sense array --> {0}", flattenedList);
        return flattenedList;
    }

    /**
     * Mètode que calcula els punts que s'han d'afegir a un usuari
     *
     * @param resultat string de la paraula comprovada
     * @param ronda ronda actual
     * @return int total punts nous
     */
    private int calcularPuntsRonda(String resultat, int ronda) {
        int punts = 0;
        Partida p = gameSingleton.getPartidaActual(true);
        List<String> paraules = p.getParaules();
        String words = paraules.get(0);
        String pActual = getParaulaRonda(words, ronda);

        if (pActual.equals(resultat)) {
            punts += resultat.length();
            List<PartidaPuntuacio> jugadorsRondaAcabada = getJugadorsPerRonda(p, ronda + 1);
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

    /**
     * Mètode que retorna la paraula de la ronda actual de una partida
     *
     * @param words String amb totes les paraules de la partida
     * @param ronda ronda actual
     * @return String paraula actual
     */
    private String getParaulaRonda(String words, int ronda) {
        String wordsString = words.replaceAll("\\[|\\]", "");
        String[] wordsArray = wordsString.split(",");
        for (int i = 0; i < wordsArray.length; i++) {
            wordsArray[i] = wordsArray[i].replaceAll("\"", "");
            wordsArray[i] = wordsArray[i].trim();
        }
        return wordsArray[ronda];
    }

    /**
     * Mètode que persisteix un objecte a la BD després de comprovar les
     * validacions
     *
     * @param ob
     * @throws PartidaException
     */
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
    }

    /**
     * Mètode que actualitza un objecte a la BD després de comprovar les
     * validacions
     *
     * @param ob
     * @throws PartidaException
     */
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
    }
}

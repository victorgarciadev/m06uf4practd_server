package main;

import com.sun.tools.sjavac.Log;
import common.IPartida;
import common.Partida;
import common.PartidaException;
import common.PartidaPuntuacio;
import common.Usuari;
import java.util.ArrayList;
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
    private AppSingleton gameSingleton;
    
    @EJB
    private UsuariEJB usuariEJB;
    
    private static final Logger log = Logger.getLogger(PartidaEJB.class.getName());
    private final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
    
    @Override
    @Lock(LockType.WRITE)
    public void actualitzarPuntuacio(String nomJugador, int punts, int ronda, double temps) throws PartidaException {
        
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
            puntuacio.setPunts(puntuacio.getPunts() + punts);
            if (temps < puntuacio.getMenorTempsEncert() && punts > 0) {
                puntuacio.setMenorTempsEncert(temps);
            }
            try {
                mergeTransaccio(puntuacio);
            } catch (PartidaException ex) {
                Logger.getLogger(PartidaEJB.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            PartidaPuntuacio novaPuntuacio = new PartidaPuntuacio();
            novaPuntuacio.setPartida(partida);
            novaPuntuacio.setUsuari(jugador);
            novaPuntuacio.setPunts(punts);
            novaPuntuacio.setMenorTempsEncert(temps);
            try {
                persisteixTransaccio(novaPuntuacio);
            } catch (PartidaException ex) {
                Logger.getLogger(PartidaEJB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    @Override
    @Lock(LockType.WRITE)
    public void novaPartida() {
        try {
            Partida partida = gameSingleton.createPartida();
            try {
                persisteixTransaccio(partida);
            } catch (PartidaException ex) {
                Logger.getLogger(PartidaEJB.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            ses.scheduleWithFixedDelay(this::finalitzaPartida, 0, 180, TimeUnit.SECONDS
            );
        } catch (PartidaException ex) {
            Log.info("La partida ja existeix: " + ex.toString());
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
            } catch (PartidaException ex) {
                Logger.getLogger(PartidaEJB.class.getName()).log(Level.SEVERE, null, ex);
            }
            waitingRoom();
        }
    }
    
    @Override
    public List<Usuari> getLlistatJugadors() {
        Partida p = gameSingleton.getPartidaActual();
        
        if (p == null) {
            log.info("No hi ha cap partida en marxa");
            return null;
        }
        
        return p.getUsuaris();
    }
    
    @Override
    @Lock(LockType.WRITE)
    public void afegirJugador(Usuari nomJugador) {
        Partida p = gameSingleton.getPartidaActual();
        
        if (p == null) {
            log.info("No hi ha cap partida en marxa.");
            return;
        }
        
        List<Usuari> listJugador = p.getUsuaris();
        listJugador.add(nomJugador);
        p.setUsuaris(listJugador);
        try {
            mergeTransaccio(p);
        } catch (PartidaException ex) {
            Logger.getLogger(PartidaEJB.class.getName()).log(Level.SEVERE, null, ex);
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
        
        return result;
    }
    
    @Override
    @Lock(LockType.READ)
    public List<Usuari> getJugadorsPartidaAmbPuntuacio(Partida p) throws PartidaException {
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
    
    @Override
    public void waitingRoom() {
        ses.scheduleWithFixedDelay(() -> {
            List<Usuari> usuaris = usuariEJB.getUsuarisEsperant();
            if (usuaris.isEmpty()) {
                waitingRoom();
            } else {
                novaPartida();
            }
        }, 0, 120, TimeUnit.SECONDS
        );
        
    }
    
    private void persisteixTransaccio(Object ob) throws PartidaException {
        List<String> errors = Validadors.validaBean(ob);
        
        if (errors.isEmpty()) {
            try {
                userTransaction.begin();
                em.persist(ob);
                userTransaction.commit();
            } catch (SystemException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | javax.transaction.NotSupportedException | javax.transaction.RollbackException ex) {
                String msg = "Error desant: " + ex.toString();
                throw new PartidaException(msg);
            }
        } else {
            String msg = "Errors de validació: " + errors.toString();
            log.log(Level.INFO, msg);
            throw new PartidaException(msg);
            
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
            } catch (SystemException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | javax.transaction.NotSupportedException | javax.transaction.RollbackException ex) {
                String msg = "Error desant: " + ex.toString();
                throw new PartidaException(msg);
            }
        } else {
            String msg = "Errors de validació: " + errors.toString();
            throw new PartidaException(msg);
        }

//        return ob;
    }
}

package main;

import common.IPartida;
import common.Partida;
import common.PartidaException;
import common.PartidaPuntuacio;
import common.Usuari;
import java.util.List;
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

    @EJB
    private UsuariEJB usuariEJB;

    private static final Logger log = Logger.getLogger(PartidaEJB.class.getName());

    @Override
    @Lock(LockType.WRITE)
    public void actualitzarPuntuacio(String nomJugador, int punts) throws PartidaException {

        if (nomJugador == null || nomJugador.isBlank() || nomJugador.isEmpty()) {
            throw new PartidaException("Jugador no vàlid");
        }

        Usuari jugador = usuariEJB.getUsuari(nomJugador);

        Partida partida = getPartidaActual();

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
    public void novaPartida() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void finalitzaPartida() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public List<String> getLlistatJugadors() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void afegirJugador(String nomJugador) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean comprovarParaula(String paraula, String nomJugador) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Partida getPartidaActual() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
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

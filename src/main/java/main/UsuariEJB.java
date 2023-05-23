package main;

import common.IUsuari;
import common.PartidaException;
import common.Usuari;
import common.PartidaPuntuacio;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

/**
 *
 * @author GrupD
 */
@Stateful
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@TransactionManagement(value = TransactionManagementType.BEAN)
public class UsuariEJB implements IUsuari {

    @PersistenceContext(unitName = "WordlePersistenceUnit")
    private EntityManager em;

    @Inject
    private UserTransaction userTransaction;

    private static final Logger log = Logger.getLogger(UsuariEJB.class.getName());

    @Override
    public void crearUsuari(String email, String nickname) throws PartidaException {

        try {

            //crear usuario
            Usuari u = new Usuari();

            u.setNickname(nickname);
            u.setEmail(email);

            persisteixAmbTransaccio(u);

        } catch (PartidaException e) {
            throw new PartidaException(e.getMessage());
        }
    }

    @Remove
    @Override
    public void tancaSessio() {
        log.log(Level.INFO, "Finalitzant sessió de UsuariEJB...");
    }

    @PreDestroy
    public void destroy() {
        log.log(Level.INFO, "UsuariEJB finalitzant...");
    }

    @Override
    public Usuari getUsuari(String email) {
        try {
            Usuari user = em.find(Usuari.class, email);
            return user;
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public List<Usuari> getUsuaris() throws PartidaException {
        List<Usuari> usuaris = new ArrayList<>();
        try {
            usuaris = em.createQuery("SELECT u FROM Usuari u", Usuari.class).getResultList();

        } catch (Exception ex) {
            throw new PartidaException(ex.toString());
        }
        return usuaris;
    }

    @Override
    public List<Usuari> getUsuarisEsperant() throws PartidaException {
        List<Usuari> usuaris = new ArrayList<>();
        try {
            usuaris = em.createQuery("SELECT u FROM Usuari u WHERE u.jugadorActual = true", Usuari.class).getResultList();
        } catch (Exception ex) {
            throw new PartidaException(ex.toString());
        }
        return usuaris;
    }

    @Override
    public int getPuntuacioTotalUsuari(Usuari usuari) {
        int puntuacioTotal = 0;
        List<PartidaPuntuacio> puntuacions = em.createQuery(
                "SELECT pp FROM PartidaPuntuacio pp WHERE pp.usuari = :usuari",
                PartidaPuntuacio.class)
                .setParameter("usuari", usuari)
                .getResultList();

        for (PartidaPuntuacio pp : puntuacions) {
            puntuacioTotal += pp.getPunts();
        }

        return puntuacioTotal;
    }

    @Override
    public void setUsuariJugant(Usuari usuari) {
        log.log(Level.INFO, "Canviant estat de l''usuari {0} a jugant.", usuari.getNickname());
        usuari.setJugadorActual(1);
        try {
            mergeTransaccio(usuari);
        } catch (PartidaException ex) {
            log.log(Level.SEVERE, "Error de base de dades canviat l'estat de l'usuari");
        }
    }

    @Override
    public void setUsuariDesactiu(Usuari usuari) {
        log.log(Level.INFO, "Canviant estat de l''usuari {0} a desactiu.", usuari.getNickname());
        usuari.setJugadorActual(0);
        try {
            mergeTransaccio(usuari);
        } catch (PartidaException ex) {
            log.log(Level.SEVERE, "Error de base de dades canviat l'estat de l'usuari");
        }
    }

    /**
     * *
     * Valida regles de negoci anotades (veure anotacions al BEAN +
     * https://javaee.github.io/tutorial/bean-validation002.html) i controla
     * transacció
     *
     * @param ob
     * @return
     * @throws PartidaException
     */
    private Object persisteixAmbTransaccio(Object ob) throws PartidaException {
        List<String> errors = Validadors.validaBean(ob);

        if (errors.isEmpty()) {
            try {

                userTransaction.begin();
                em.persist(ob);
                userTransaction.commit();

            } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
                String msg = "Error desant: " + errors.toString();
                log.log(Level.INFO, msg);
                throw new PartidaException("Error desant: " + ex.toString());
            }

        } else {
            String msg = "Errors de validació: " + errors.toString();
            log.log(Level.INFO, msg);
            throw new PartidaException(msg);
        }

        return ob;
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

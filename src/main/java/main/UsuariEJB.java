package main;

import common.IUsuari;
import common.Usuari;
import common.PartidaPuntuacio;
import java.util.List;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Stateful;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

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

    @Override
    public void crearUsuari(String email, String nickname) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Usuari getUsuari(String email) {
        try {
            Usuari user = em.createQuery("SELECT u FROM Usuari u where u.email = :email", Usuari.class).setParameter("email", email).getSingleResult();
            return user;
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public List<Usuari> getUsuaris() {
        List<Usuari> usuaris = em.createQuery("SELECT u FROM Usuari u", Usuari.class).getResultList();
        return usuaris;
    }

    @Override
    public List<Usuari> getUsuarisEsperant() {
        List<Usuari> usuaris = em.createQuery("SELECT u FROM Usuari u WHERE u.jugadorActual = true", Usuari.class).getResultList();
        return usuaris;
    }

    @Override
    public void actualitzarPuntuacioUsuari(Usuari usuari, int puntuacio) {
        usuari.setPuntuacio(puntuacio);
        em.merge(usuari);
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
        usuari.setJugadorActual(true);
        em.merge(usuari);
    }

}

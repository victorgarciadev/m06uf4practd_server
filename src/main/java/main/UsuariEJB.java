package main;

import common.IUsuari;
import common.Usuari;
import java.util.List;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Stateful;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 *
 * @author GrupD
 */
@Stateful
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@TransactionManagement(value=TransactionManagementType.BEAN)
public class UsuariEJB implements IUsuari {
    
    @PersistenceContext(unitName = "WordlePersistenceUnit")
    private EntityManager em;

    @Override
    public void crearUsuari(String email, String nickname) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Usuari getUsuari(String email) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public List<Usuari> getUsuaris() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    @Lock(LockType.READ)
    public List<Usuari> getUsuarisEsperant() {
        TypedQuery<Usuari> query = em.createQuery("SELECT u FROM Usuari u WHERE u.jugadorActual = true", Usuari.class);
        return query.getResultList();
    }

    @Override
    public void actualitzarPuntuacioUsuari(Usuari usuari, int puntuacio) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}

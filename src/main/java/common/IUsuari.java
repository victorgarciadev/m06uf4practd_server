package common;

import java.util.List;
import javax.ejb.Remote;

/**
 *
 * @author victor
 */
@Remote
public interface IUsuari {
    
    /**
     * Crea un nou usuari
     * @param email
     * @param nickname
     */
    public void crearUsuari(String email, String nickname);
    
    /**
     * Retorna un unsuari pel seu Id
     * @param email
     * @return 
     */
    public Usuari getUsuari(String email);
    
    /**
     * Retorna una llista amb tots els usuaris
     * @return 
     */
    public List<Usuari> getUsuaris();
    
    /**
     * Actualitza la puntuació total de l'usuari
     * @param usuari
     * @param puntuacio 
     */
    public void actualitzarPuntuacioUsuari(Usuari usuari, int puntuacio);
}

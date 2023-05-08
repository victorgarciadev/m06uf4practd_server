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
     * Retorna la llista d'usuaris esperant
     * @return 
     */
    public List<Usuari> getUsuarisEsperant();
    
    /**
     * Actualitza la puntuació total de l'usuari
     * @param usuari
     * @param puntuacio 
     */
    public void actualitzarPuntuacioUsuari(Usuari usuari, int puntuacio);
    
    /**
     * Retorna la puntuació total històrica de l'usuari
     * @param usuari
     * @return 
     */
    public int getPuntuacioTotalUsuari(Usuari usuari);
    
    /**
     * M�tode que canvia l'estat del jugador a Jugant
     * @param usuari 
     */
    public void setUsuariJugant(Usuari usuari);
}

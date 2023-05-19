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
     * @param email email
     * @param nickname nom
     * @throws common.PartidaException
     */
    public void crearUsuari(String email, String nickname) throws PartidaException;
    
    /**
     * Retorna un unsuari pel seu Id
     * @param email email
     * @return 
     */
    public Usuari getUsuari(String email);
    
    /**
     * Retorna una llista amb tots els usuaris
     * @return llista d'usuaris
     * @throws common.PartidaException 
     */
    public List<Usuari> getUsuaris() throws PartidaException;
    
    /**
     * Retorna la llista d'usuaris esperant
     * @return llista d'usuaris
     * @throws common.PartidaException 
     */
    public List<Usuari> getUsuarisEsperant() throws PartidaException;
    
    /**
     * Actualitza la puntuació total de l'usuari
     * @param usuari usuari actual
     * @param puntuacio puntuació a afegir a l'usuari
     */
    public void actualitzarPuntuacioUsuari(Usuari usuari, int puntuacio);
    
    /**
     * Retorna la puntuació total històrica de l'usuari
     * @param usuari usuari actual
     * @return int total puntuació
     */
    public int getPuntuacioTotalUsuari(Usuari usuari);
    
    /**
     * M�tode que canvia l'estat del jugador a Jugant
     * @param usuari usuari actual
     */
    public void setUsuariJugant(Usuari usuari);
}

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
     * Tancar sessió d'EJB
     */
    public void tancaSessio();
    
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
    
    /**
     * Mètode que canviar l'estat del jugador a desactiu
     * @param usuari usuari actual
     */
    public void setUsuariDesactiu(Usuari usuari);
}

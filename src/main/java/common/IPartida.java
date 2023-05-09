package common;

import java.util.List;
import javax.ejb.Remote;

/**
 *
 * @author victor
 */
@Remote
public interface IPartida {
    
   /**
    * Comença una nova partida
     * @param usuaris
     * @throws common.PartidaException
    */
    public void novaPartida(List<Usuari> usuaris) throws PartidaException;
    
    /**
     * Tanca una partida
     * @throws common.PartidaException
     */
    public void finalitzaPartida() throws PartidaException;
    
    /**
     * Llista dels jugadors d'una partida
     * @return 
     * @throws common.PartidaException 
     */
    public List<Usuari> getLlistatJugadors() throws PartidaException;
    
    /**
     * Actualitza la puntuació d'un jugador a una partida
     * @param nomJugador
     * @param punts 
     * @param ronda 
     * @param temps 
     * @throws common.PartidaException 
     */
    public void actualitzarPuntuacio(String nomJugador, int punts, int ronda, double temps) throws PartidaException;
    
    /**
     * Afegeix un jugador a la partida 
     * @return  
     * @throws common.PartidaException 
     */
    public List<Usuari> afegirJugadors() throws PartidaException;
    
    /**
     * Comprova la paraula enviada de l'usuari lletra per lletra
     * @param paraula
     * @param ronda
     * @param nomJugador
     * @return 
     * @throws common.PartidaException 
     */
    public String comprovarParaula(String paraula, int ronda, Usuari nomJugador) throws PartidaException;
    
    /**
     * Retorna tots els jugadors d'una partida amb la seva puntuació
     * @param p
     * @return
     * @throws PartidaException 
     */
    public List<Usuari> getJugadorsPartidaAmbPuntuacio(Partida p) throws PartidaException;
    
    /**
     * M�tode que controla el temps d'espera entre partida i partida
     */
    public void waitingRoom();
}

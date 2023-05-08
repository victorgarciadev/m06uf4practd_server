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
    * Comen�a una nova partida
     * @throws common.PartidaException
    */
    public void novaPartida() throws PartidaException;
    
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
     * Actualitza la puntuaci� d'un jugador a una partida
     * @param nomJugador
     * @param punts 
     * @param ronda 
     * @param temps 
     * @throws common.PartidaException 
     */
    public void actualitzarPuntuacio(String nomJugador, int punts, int ronda, double temps) throws PartidaException;
    
    /**
     * Afegeix un jugador a la partida
     * @param nomJugador 
     * @throws common.PartidaException 
     */
    public void afegirJugador(Usuari nomJugador) throws PartidaException;
    
    /**
     * Comprova la paraula enviada de l'usuari lletra per lletra
     * @param paraula
     * @param posicio
     * @param nomJugador
     * @return 
     * @throws common.PartidaException 
     */
    public String comprovarParaula(String paraula, int ronda, Usuari nomJugador) throws PartidaException;
    
    /**
     * Retorna tots els jugadors d'una partida amb la seva puntuaci�
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

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
     * @throws common.PartidaException
    */
    public void novaPartida() throws PartidaException;
    
    /**
     * Tanca una partida
     * @throws common.PartidaException
     */
    public void finalitzaPartida() throws PartidaException;
    
    /**
     * Retorna l'id de la partida actual
     * @return 
     * @throws common.PartidaException 
     */
    public Partida getPartidaActual() throws PartidaException;
    
    /**
     * Llista dels jugadors d'una partida
     * @return 
     * @throws common.PartidaException 
     */
    public List<String> getLlistatJugadors() throws PartidaException;
    
    /**
     * Actualitza la puntuació d'un jugador a una partida
     * @param nomJugador
     * @param punts 
     * @throws common.PartidaException 
     */
    public void actualitzarPuntuacio(String nomJugador, int punts) throws PartidaException;
    
    /**
     * Afegeix un jugador a la partida
     * @param nomJugador 
     * @throws common.PartidaException 
     */
    public void afegirJugador(String nomJugador) throws PartidaException;
    
    /**
     * Comprova la paraula enviada de l'usuari
     * @param paraula
     * @param nomJugador
     * @return 
     * @throws common.PartidaException 
     */
    public boolean comprovarParaula(String paraula, String nomJugador) throws PartidaException;
}

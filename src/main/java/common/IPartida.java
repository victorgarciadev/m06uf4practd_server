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
    */
    public void novaPartida(List<Usuari> usuaris);
    
    /**
     * Tanca una partida
     */
    public void finalitzaPartida();
    
    /**
     * Llista dels jugadors d'una partida
     * @return 
     * @throws common.PartidaException 
     */
    public List<Usuari> getLlistatJugadors() throws PartidaException;
    
    /**
     * Actualitza la puntuació d'un jugador a una partida
     * @param nomJugador
     * @param resultat
     * @param ronda 
     * @param temps 
     * @throws common.PartidaException 
     */
    public void actualitzarPuntuacio(String nomJugador, String resultat, int ronda, double temps) throws PartidaException;
    
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
     */
    public List<Usuari> getJugadorsPartidaAmbPuntuacio(Partida p);
    
    /**
     * M�tode que controla el temps d'espera entre partida i partida
     */
    public void waitingRoom();
    
    /**
     * Mètode que retorna el temps que falta pel canvi de pantalla entre el joc i la sala d'espera
     * @return 
     */
    public Long timeRemaining();
}

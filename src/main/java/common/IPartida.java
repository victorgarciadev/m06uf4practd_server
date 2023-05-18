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
    */
    public void novaPartida();

    /**
     * Mètode que checkeja l'estat del servidor
     * @param pantalla
     */
    public void checkPartida(String pantalla);
    
    /**
     * Actualitza la puntuació d'un jugador a una partida
     * @param jugador
     * @param resultat
     * @param ronda 
     * @param temps 
     * @throws common.PartidaException 
     */
    public void actualitzarPuntuacio(Usuari jugador, String resultat, int ronda, double temps) throws PartidaException;
    
    /**
     * Afegeix un jugador a la partida 
     * @param usuariEJB
     * @param email
     * @throws common.PartidaException 
     */
    public void afegirJugador(IUsuari usuariEJB, String email) throws PartidaException;
    
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
     * Mètode que retorna la dificultat de la partida que s'esta jugant actualment
     * @return 
     */
    public String getDificultatPartidaActual();
    
    
    /**
     * Mètode que retorna les paraules a endevinar de la partida
     * @return 
     */
    public List<String> getParaulesPartida();
    
    /**
     * Mètode que retorna el temps que falta pel canvi de pantalla entre el joc i la sala d'espera
     * @return 
     */
    public int timeRemaining();
}

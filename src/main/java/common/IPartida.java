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
     * Tancar sessió d'EJB
     */
    public void tancaSessio();
    
    /**
     * Actualitza la puntuació d'un jugador a una partida
     * @param jugador actual
     * @param resultat string amb la comprovació de la paraula
     * @param ronda ronda del joc
     * @param temps temps trigat a resoldre la paraula
     * @throws common.PartidaException 
     */
    public void actualitzarPuntuacio(Usuari jugador, String resultat, int ronda, double temps) throws PartidaException;
    
    /**
     * Afegeix un jugador a la partida 
     * @param jugador usuari acutal 
     * @throws common.PartidaException 
     */
    public void afegirJugador(Usuari jugador) throws PartidaException;
    
    /**
     * Comprova la paraula enviada de l'usuari lletra per lletra
     * @param paraula paraula enviada
     * @param ronda ronda de la partida
     * @param nomJugador jugador actual
     * @return string comprovant de la paraula
     * @throws common.PartidaException 
     */
    public String comprovarParaula(String paraula, int ronda, Usuari nomJugador) throws PartidaException;
    
    /**
     * Retorna tots els jugadors d'una partida amb la seva puntuació
     * @param p partida actual
     * @return llista amb usuaris de la partida
     */
    public List<Usuari> getJugadorsPartidaAmbPuntuacio(Partida p);
    
    /**
     * Mètode que retorna la dificultat de la partida que s'esta jugant actualment
     * @return string amb la dificultat de la partida
     */
    public String getDificultatPartidaActual();
    
    /**
     * Mètode que retorna el temps que falta pel canvi de pantalla entre el joc i la sala d'espera
     * @param pantalla actual
     * @return int segons que falten
     */
    public int timeRemaining(String pantalla);
}

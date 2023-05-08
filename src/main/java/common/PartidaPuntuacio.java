package common;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 *
 * @author pmorante
 */
public class PartidaPuntuacio implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_partida", nullable = false)
    private Partida partida;

    @ManyToOne
    @JoinColumn(name = "id_usuari", nullable = false)
    private Usuari usuari;

    @Column(name = "punts")
    private int punts;
    
    private double menorTempsEncert;

    public PartidaPuntuacio() {
    }

    public PartidaPuntuacio(Partida partida, Usuari usuari, int punts, double menorTempsEncert) {
        this.partida = partida;
        this.usuari = usuari;
        this.punts = punts;
        this.menorTempsEncert = menorTempsEncert;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Partida getPartida() {
        return partida;
    }

    public void setPartida(Partida partida) {
        this.partida = partida;
    }

    public Usuari getUsuari() {
        return usuari;
    }

    public void setUsuari(Usuari usuari) {
        this.usuari = usuari;
    }

    public int getPunts() {
        return punts;
    }

    public void setPunts(int punts) {
        this.punts = punts;
    }

    public double getMenorTempsEncert() {
        return menorTempsEncert;
    }

    public void setMenorTempsEncert(double menorTempsEncert) {
        this.menorTempsEncert = menorTempsEncert;
    }
    
}

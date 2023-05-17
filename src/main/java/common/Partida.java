package common;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author victor
 */
@Entity
public class Partida implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataPartida;
    
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> paraules;
    
    private String dificultat;
    
    private int actual;
    
    private int comencada;

    public Partida() {
    }

    public Partida(Date dataPartida, List<String> paraules, String dificultat, int actual, int comencada) {
        this.dataPartida = dataPartida;
        this.paraules = paraules;
        this.dificultat = dificultat;
        this.actual = actual;
        this.comencada = comencada;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDataPartida() {
        return dataPartida;
    }

    public void setDataPartida(Date dataPartida) {
        this.dataPartida = dataPartida;
    }

    public List<String> getParaules() {
        return paraules;
    }

    public void setParaules(List<String> paraules) {
        this.paraules = paraules;
    }

    public String getDificultat() {
        return dificultat;
    }

    public void setDificultat(String dificultat) {
        this.dificultat = dificultat;
    }

    public int isActual() {
        return actual;
    }

    public void setActual(int actual) {
        this.actual = actual;
    }

    public int getComencada() {
        return comencada;
    }

    public void setComencada(int comencada) {
        this.comencada = comencada;
    }
    
}

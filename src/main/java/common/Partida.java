package common;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
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
    
    @ElementCollection
    private List<String> paraules;
    
    @OneToMany
    private List<Usuari> usuaris;
    
    private String dificultat;
    
    private boolean actual;

    public Partida() {
    }

    public Partida(Date dataPartida, List<String> paraules, List<Usuari> usuaris, String dificultat, boolean actual) {
        this.dataPartida = dataPartida;
        this.paraules = paraules;
        this.usuaris = usuaris;
        this.dificultat = dificultat;
        this.actual = actual;
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

    public List<Usuari> getUsuaris() {
        return usuaris;
    }

    public void setUsuaris(List<Usuari> usuaris) {
        this.usuaris = usuaris;
    }

    public String getDificultat() {
        return dificultat;
    }

    public void setDificultat(String dificultat) {
        this.dificultat = dificultat;
    }

    public boolean isActual() {
        return actual;
    }

    public void setActual(boolean actual) {
        this.actual = actual;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + this.id;
        hash = 79 * hash + Objects.hashCode(this.dataPartida);
        hash = 79 * hash + Objects.hashCode(this.paraules);
        hash = 79 * hash + Objects.hashCode(this.usuaris);
        hash = 79 * hash + Objects.hashCode(this.dificultat);
        hash = 79 * hash + (this.actual ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Partida other = (Partida) obj;
        if (this.id != other.id) {
            return false;
        }
        if (this.actual != other.actual) {
            return false;
        }
        if (!Objects.equals(this.dificultat, other.dificultat)) {
            return false;
        }
        if (!Objects.equals(this.dataPartida, other.dataPartida)) {
            return false;
        }
        if (!Objects.equals(this.paraules, other.paraules)) {
            return false;
        }
        return Objects.equals(this.usuaris, other.usuaris);
    }

    @Override
    public String toString() {
        return "Partida{" + "id=" + id + ", dataPartida=" + dataPartida + ", paraules=" + paraules + ", usuaris=" + usuaris + ", dificultat=" + dificultat + ", actual=" + actual + '}';
    }
    
}

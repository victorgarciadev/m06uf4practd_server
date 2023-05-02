package common;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 *
 * @author victor
 */
@Entity
public class Partida implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    private int id;
    
    private Timestamp dataPartida;
    
    private List<String> paraules;
    
    private List<Usuari> usuaris;
    
    private int dificultat;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Timestamp getDataPartida() {
        return dataPartida;
    }

    public void setDataPartida(Timestamp dataPartida) {
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

    public int getDificultat() {
        return dificultat;
    }

    public void setDificultat(int dificultat) {
        this.dificultat = dificultat;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + this.id;
        hash = 67 * hash + Objects.hashCode(this.dataPartida);
        hash = 67 * hash + Objects.hashCode(this.paraules);
        hash = 67 * hash + Objects.hashCode(this.usuaris);
        hash = 67 * hash + this.dificultat;
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
        if (this.dificultat != other.dificultat) {
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
        return "Partida{" + "id=" + id + ", dataPartida=" + dataPartida + ", paraules=" + paraules + ", usuaris=" + usuaris + ", dificultat=" + dificultat + '}';
    }
    
}

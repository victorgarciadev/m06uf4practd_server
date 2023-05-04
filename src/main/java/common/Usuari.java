package common;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

/**
 *
 * @author victor
 */
@Entity
public class Usuari implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    private String email;
    
    @NotNull(message = "Un usuari ha de tenir un nickname")
    @Column(unique = true)
    private String nickname;
    
    private int puntuacio;
    
    private boolean jugadorActual;

    public Usuari() {
    }

    public Usuari(String email, String nickname, int puntuacio, boolean jugadorActual) {
        this.email = email;
        this.nickname = nickname;
        this.puntuacio = puntuacio;
        this.jugadorActual = jugadorActual;
    }
    
    public Usuari(String nickname, int puntuacio) {
        this.nickname = nickname;
        this.puntuacio = puntuacio;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getPuntuacio() {
        return puntuacio;
    }

    public void setPuntuacio(int puntuacio) {
        this.puntuacio = puntuacio;
    }

    public boolean isJugadorActual() {
        return jugadorActual;
    }

    public void setJugadorActual(boolean jugadorActual) {
        this.jugadorActual = jugadorActual;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.email);
        hash = 97 * hash + Objects.hashCode(this.nickname);
        hash = 97 * hash + this.puntuacio;
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
        final Usuari other = (Usuari) obj;
        if (this.puntuacio != other.puntuacio) {
            return false;
        }
        if (!Objects.equals(this.email, other.email)) {
            return false;
        }
        return Objects.equals(this.nickname, other.nickname);
    }

    @Override
    public String toString() {
        return "Partida{" + "email=" + email + ", nickname=" + nickname + ", puntuacio=" + puntuacio + '}';
    }
    
}

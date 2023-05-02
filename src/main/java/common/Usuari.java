package common;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 *
 * @author victor
 */
@Entity
public class Usuari implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    private String email;
    
    private String nickname;
    
    private int puntuacio;

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

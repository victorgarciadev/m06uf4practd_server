package common;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author pablomorante
 */
@Entity
public class SalaEspera implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Id
    private Date horaComenca;

    public SalaEspera(Date horaComenca) {
        this.horaComenca = horaComenca;
    }

    public SalaEspera() {
    }

    public Date getHoraComenca() {
        return horaComenca;
    }

    public void setHoraComenca(Date horaComenca) {
        this.horaComenca = horaComenca;
    }
    
    
}

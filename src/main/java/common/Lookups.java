package common;

import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import main.PartidaEJB;
import main.UsuariEJB;

/**
 * Classe encarregada de fer les connexions amb els EJB remots
 * @author GrupD
 */
public class Lookups {
    
    static final String wildFlyInitialContextFactory = "org.wildfly.naming.client.WildFlyInitialContextFactory";
    
    static final String appName = "M06UF4PracTD_Server";
    
    /**
     * Connexió a un EJB amb @remote via local (entre components del mateix servidor) per Partida
     * @return
     * @throws NamingException 
     */
    public static IPartida partidaEJBRemoteLookup() throws NamingException {
        String strlookup = "ejb:/" + appName + "/" + PartidaEJB.class.getSimpleName() + "!" + IPartida.class.getSimpleName()+ "?stateful";
        
        Properties jndiProperties = new Properties();
        
        jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY, wildFlyInitialContextFactory);
        
        Context context = new InitialContext(jndiProperties);
        
        return (IPartida) context.lookup(strlookup);
    }
    
    /**
     * Connexió a un EJB amb @remote via local (entre components del mateix servidor) per Usuari
     * @return
     * @throws NamingException 
     */
    public static IUsuari usuariEJBRemoteLookup() throws NamingException {
        String strlookup = "ejb:/" + appName + "/" + UsuariEJB.class.getSimpleName() + "!" + IUsuari.class.getSimpleName() + "?stateful";
        
        Properties jndiProperties = new Properties();
        
        jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY, wildFlyInitialContextFactory);
        
        Context context = new InitialContext(jndiProperties);
        
        return (IUsuari) context.lookup(strlookup);
    }
    
    public static IUsuari usuariEJBLocalLookup() throws NamingException {
        String strlookup = "java:jboss/exported/" + appName + "/" + UsuariEJB.class.getSimpleName() + "!" + IUsuari.class.getName();
            
        Properties jndiProperties = new Properties();

        jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY,  wildFlyInitialContextFactory);
        
        Context context = new InitialContext(jndiProperties);

        return (IUsuari) context.lookup(strlookup);
    }
}

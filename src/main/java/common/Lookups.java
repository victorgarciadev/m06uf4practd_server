package common;

/**
 * Classe encarregada de fer les connexions amb els EJB remots
 * @author manel
 */
public class Lookups {
    
//    static final String wildFlyInitialContextFactory = "org.wildfly.naming.client.WildFlyInitialContextFactory";
//    
//    
//    static final String appName = "EJB_Exemple1_Server-1";
//    
//    public static ICarroCompra carroCompraEJBRemoteLookup() throws NamingException
//    {
//        
//        String strlookup = "ejb:/" + appName + "/" + CarroCompraEJB.class.getSimpleName() + "!" + ICarroCompra.class.getName()+"?stateful";
//            
//        Properties jndiProperties = new Properties();
//
//        jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY,  wildFlyInitialContextFactory);
//        
//        Context context = new InitialContext(jndiProperties);
//
//        return (ICarroCompra) context.lookup(strlookup);
//    }
//    
//    public static ITenda tendaEJBRemoteLookup() throws NamingException
//    {
//        String strlookup = "ejb:/" + appName + "/" + TendaEJB.class.getSimpleName() + "!" + ITenda.class.getName();
//            
//        Properties jndiProperties = new Properties();
//
//        jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY,  wildFlyInitialContextFactory);
//        
//        Context context = new InitialContext(jndiProperties);
//
//        return (ITenda) context.lookup(strlookup);
//    }
//    
//    /***
//     * Connexió a un EJB amb @remote via local (entre components del mateix servidor)
//     * @return
//     * @throws NamingException 
//     */
//    public static ITenda tendaEJBLocalLookup() throws NamingException
//    {
//        String strlookup = "java:jboss/exported/" + appName + "/" + TendaEJB.class.getSimpleName() + "!" + ITenda.class.getName();
//            
//        Properties jndiProperties = new Properties();
//
//        jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY,  wildFlyInitialContextFactory);
//        
//        Context context = new InitialContext(jndiProperties);
//
//        return (ITenda) context.lookup(strlookup);
//    }
}

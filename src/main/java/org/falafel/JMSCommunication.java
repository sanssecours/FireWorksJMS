package org.falafel;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * .
 */
public class JMSCommunication {
    /** Get the Logger for the current class. */
    private static final Logger LOGGER =
            Logger.getLogger(JMSCommunication.class.getName());

    private static final String CONNECTION_FACTORY =
            "jms/RemoteConnectionFactory";
    private static final String USERNAME = "fireworks";
    private static final String PASSWORD = "fireworks";
    private static final String INITIAL_CONTEXT_FACTORY =
            "org.jboss.naming.remote.client.InitialContextFactory";
    private static final String PROVIDER_URL =
            "http-remoting://127.0.0.1:8080";

    private Context namingContext;
    private ConnectionFactory connectionFactory;

    public JMSCommunication () {
        try {
            // Set up the namingContext for the JNDI lookup
            final Properties env = new Properties();
            env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
            env.put(Context.PROVIDER_URL, PROVIDER_URL);
            env.put(Context.SECURITY_PRINCIPAL, USERNAME);
            env.put(Context.SECURITY_CREDENTIALS, PASSWORD);
            namingContext = new InitialContext(env);

            connectionFactory = (ConnectionFactory)
                    namingContext.lookup(CONNECTION_FACTORY);
        } catch (NamingException e) {
            LOGGER.severe("Could not create properties");
        }
    }

    /**
     * Closes the communication with the server.
     */
    public void closeCommunication () {
        if (namingContext != null) {
            try {
                namingContext.close();
            } catch (NamingException e) {
                LOGGER.severe("Could not could not close naming context ");
            }
        }
    }

    public void sendMessage(Rocket msgRocket, String queue) {
        try {
            Destination destination = (Destination) namingContext.lookup(
                    queue);

            try (JMSContext context = connectionFactory.createContext(
                    USERNAME, PASSWORD)) {
                context.createProducer().send(destination, msgRocket);
            }
        } catch (RuntimeException e) {
            LOGGER.severe("Could not write in queue");
        } catch (NamingException e) {
            LOGGER.severe("Could not find destination");
        }
    }

    public void sendMessage(Material msgMaterial, String queue) {
        try {
            Destination destination = (Destination) namingContext.lookup(
                    queue);

            try (JMSContext context = connectionFactory.createContext(
                    USERNAME, PASSWORD)) {
                context.createProducer().send(destination, msgMaterial);
            }
        } catch (RuntimeException e) {
            LOGGER.severe("Could not write in queue");
        } catch (NamingException e) {
            LOGGER.severe("Could not find destination");
        }
    }

    public Object receiveMessage(String queue) {
        Object text = null;
        try {
            Destination destination = (Destination) namingContext.lookup(
                    queue);

            try (JMSContext context = connectionFactory.createContext(
                    USERNAME, PASSWORD)) {
                JMSConsumer consumer = context.createConsumer(destination);
                text = consumer.receiveBody(Object.class, 5000);
            }
        } catch (RuntimeException e) {
            LOGGER.severe("Could not write in queue");
        } catch (NamingException e) {
            LOGGER.severe("Could not create properties");
        }
        return text;
    }
}

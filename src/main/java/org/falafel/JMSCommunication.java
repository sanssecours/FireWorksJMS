package org.falafel;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

import static org.falafel.Utility.CONNECTION_FACTORY;
import static org.falafel.Utility.USERNAME;
import static org.falafel.Utility.PASSWORD;
import static org.falafel.Utility.INITIAL_CONTEXT_FACTORY;
import static org.falafel.Utility.PROVIDER_URL;

/**
 * Receive and send messages via JMS.
 */
public class JMSCommunication {
    /** Get the Logger for the current class. */
    private static final Logger LOGGER =
            Logger.getLogger(JMSCommunication.class.getName());
    /** The time used to wait for one message from a certain queue. */
    private static final int WAIT_TIME_MESSAGE_MS = 5000;

    /** The naming context used to lookup the JMS queues. */
    private Context namingContext;
    /** The connection factory used for the JMS communication. */
    private ConnectionFactory connectionFactory;

    /** Create a new JMS communication. */
    public JMSCommunication() {
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
    public final void closeCommunication() {
        if (namingContext != null) {
            try {
                namingContext.close();
            } catch (NamingException e) {
                LOGGER.severe("Could not could not close naming context ");
            }
        }
    }

    /**
     * Send a Integer value to a certain queue.
     *
     * @param msgId
     *          The integer value that should be stored in the queue.
     * @param queue
     *          The name of the queue where the integer value should be stored.
     */
    public final void sendMessage(final Integer msgId, final String queue) {
        try {
            Destination destination = (Destination) namingContext.lookup(
                    queue);

            try (JMSContext context = connectionFactory.createContext(
                    USERNAME, PASSWORD)) {
                context.createProducer().send(destination, msgId);
            }
        } catch (NamingException e) {
            LOGGER.severe("Could not lookup destination!");
        }
    }

    /**
     * Send a Rocket object to a certain queue.
     *
     * @param msgRocket
     *          The rocket that should be stored in the queue.
     * @param queue
     *          The name of the queue where the rocket should be stored.
     */
    public final void sendMessage(final Rocket msgRocket, final String queue) {
        try {
            Destination destination = (Destination) namingContext.lookup(
                    queue);

            try (JMSContext context = connectionFactory.createContext(
                    USERNAME, PASSWORD)) {
                context.createProducer().send(destination, msgRocket);
            }
        } catch (NamingException e) {
            LOGGER.severe("Could not lookup destination!");
        }
    }

    /**
     * Send a Material object to a certain queue.
     *
     * @param msgMaterial
     *          The material that should be stored in the queue.
     * @param queue
     *          The name of the queue where the material should be stored.
     */
    public final void sendMessage(final Material msgMaterial,
                                  final String queue) {
        try {
            Destination destination = (Destination) namingContext.lookup(
                    queue);

            try (JMSContext context = connectionFactory.createContext(
                    USERNAME, PASSWORD)) {
                context.createProducer().send(destination, msgMaterial);
            }
        } catch (NamingException e) {
            LOGGER.severe("Could not lookup destination!");
        }
    }

    /**
     * Send a RocketPackage object to a certain queue.
     *
     * @param msgRocketPackage
     *          The rocket package that should be stored in the queue.
     * @param queue
     *          The name of the queue where the rocket package should be stored.
     */
    public final void sendMessage(final RocketPackage msgRocketPackage,
                                  final String queue) {
        try {
            Destination destination = (Destination) namingContext.lookup(
                    queue);

            try (JMSContext context = connectionFactory.createContext(
                    USERNAME, PASSWORD)) {
                context.createProducer().send(destination, msgRocketPackage);
            }
        } catch (NamingException e) {
            LOGGER.severe("Could not lookup destination!");
        }
    }

    /**
     * Receive an object from a certain queue.
     *
     * @param queue
     *          The name of the queue from which the object should be received.
     * @return
     *          The object stored in the queue or null if no message could be
     *          received in time.
     */
    public final Object receiveMessage(final String queue) {
        Object text = null;
        try {
            Destination destination = (Destination) namingContext.lookup(
                    queue);

            try (JMSContext context = connectionFactory.createContext(
                    USERNAME, PASSWORD)) {
                JMSConsumer consumer = context.createConsumer(destination);
                text = consumer.receiveBody(Object.class, WAIT_TIME_MESSAGE_MS);
            }
        } catch (NamingException e) {
            LOGGER.severe("Could not lookup destination!");
        }
        return text;
    }

    /**
     * Read all messages in a certain queue.
     *
     * @param queue
     *          The name of the queue from which the objects should be received.
     * @return
     *          A ArrayList containing all messages stored in the specified
     *          queue.
     */
    public final ArrayList<Object> readMessagesInQueue(final String queue) {
        ArrayList<Object> messages = new ArrayList<>();
        try (JMSContext context = connectionFactory.createContext(
                USERNAME, PASSWORD)) {
            Queue destinationQueue = (Queue) namingContext.lookup(queue);
            QueueBrowser browser = context.createBrowser(destinationQueue);
            Enumeration enumeration = browser.getEnumeration();
            while (enumeration.hasMoreElements()) {
                Object object = ((ObjectMessage)
                        enumeration.nextElement()).getObject();
                messages.add(object);
            }
            browser.close();
        } catch (NamingException e) {
            LOGGER.severe("Could not lookup destination!");
        } catch (JMSException e) {
            LOGGER.severe("Could not get enumeration!");
        }
        return messages;
    }
}

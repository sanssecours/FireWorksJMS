/**
 * This code is a simplified version of the one found in
 * HelloWorldJMSClient.java at https://github.com/wildfly/quickstart. It is
 * therefore licensed under the Apache License, Version 2.0 (the "License");
 */
package org.falafel;

import java.util.Properties;
import java.util.logging.Logger;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class FireWorks {
    private static final Logger log =
            Logger.getLogger(FireWorks.class.getName());

    private static final String CONNECTION_FACTORY =
            "jms/RemoteConnectionFactory";
    private static final String DESTINATION = "jms/queue/fireworks";
    private static final String USERNAME = "fireworks";
    private static final String PASSWORD = "fireworks";
    private static final String INITIAL_CONTEXT_FACTORY =
            "org.jboss.naming.remote.client.InitialContextFactory";
    private static final String PROVIDER_URL =
            "http-remoting://127.0.0.1:8080";

    public static void main(String[] args) {

        Context namingContext = null;

        try {
            String userName = USERNAME;
            String password = PASSWORD;

            // Set up the namingContext for the JNDI lookup
            final Properties env = new Properties();
            env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
            env.put(Context.PROVIDER_URL,
                    System.getProperty(Context.PROVIDER_URL, PROVIDER_URL));
            env.put(Context.SECURITY_PRINCIPAL, userName);
            env.put(Context.SECURITY_CREDENTIALS, password);
            namingContext = new InitialContext(env);

            // Perform the JNDI lookups
            String connectionFactoryString = CONNECTION_FACTORY;
            ConnectionFactory connectionFactory =
                    (ConnectionFactory) namingContext.lookup(
                            connectionFactoryString);
            String destinationString = DESTINATION;
            Destination destination = (Destination) namingContext.lookup(
                    destinationString);

            String content = "Hello, FireWorks!";

            try (JMSContext context = connectionFactory.createContext(
                    userName, password)) {
                log.info("Sending message: " + content);
                context.createProducer().send(destination, content);
                JMSConsumer consumer = context.createConsumer(destination);
                String text = consumer.receiveBody(String.class, 5000);
                log.info("Received message: " + text);
            }
        } catch (NamingException e) {
            log.severe(e.getMessage());
        } finally {
            if (namingContext != null) {
                try {
                    namingContext.close();
                } catch (NamingException e) {
                    log.severe(e.getMessage());
                }
            }
        }
    }
}


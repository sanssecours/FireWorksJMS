package org.falafel;

/**
 * This class provides various utility functions.
 */
public final class Utility {

    /** Create an empty Utility instance. */
    private Utility() { }

    /** The connection factory to the JMS provider. */
    public static final String CONNECTION_FACTORY =
            "jms/RemoteConnectionFactory";
    /** The user name used to connect to the JMS provider. */
    public static final String USERNAME = "fireworks";
    /** The password for {@code USERNAME}.*/
    public static final String PASSWORD = "fireworks";
    /** The address of the initial context factory. */
    public static final String INITIAL_CONTEXT_FACTORY =
            "org.jboss.naming.remote.client.InitialContextFactory";
    /** The address of the JMS provider. */
    public static final String PROVIDER_URL = "http-remoting://127.0.0.1:8080";
    /** The number of IDs initially stored in the queues. */
    public static final int IDS_QUEUES_INIT = 10;

    /**
     * Let the current thread sleep for some time.
     *
     * @param sleepTime
     *          The amount of time that the current thread should sleep in ms.
     *
     */
    public static void sleep(final int sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            System.err.println("I was interrupted while trying to sleep. "
                    + "How rude!");
        }
    }
}

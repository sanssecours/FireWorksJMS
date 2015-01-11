package org.falafel;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;
import static org.falafel.Utility.CONNECTION_FACTORY;
import static org.falafel.Utility.IDS_QUEUES_INIT;
import static org.falafel.Utility.INITIAL_CONTEXT_FACTORY;
import static org.falafel.Utility.PASSWORD;
import static org.falafel.Utility.PROVIDER_URL;
import static org.falafel.Utility.USERNAME;

/**
 * This class represents a logistic worker.
 */
public final class Logistic implements MessageListener {

    /** Constant for how many rockets are in one package. */
    private static final int PACKAGE_SIZE = 5;
    /** Constant for how long the shutdown hook is waiting. */
    private static final int WAIT_TIME_TO_SHUTDOWN = 5000;
    /** Collected class A rockets. */
    private static ArrayList<Rocket> rocketsClassA = new ArrayList<>();
    /** Collected class B rockets. */
    private static ArrayList<Rocket> rocketsClassB = new ArrayList<>();
    /** Get the Logger for the current class. */
    private static final Logger LOGGER = getLogger(Logistic.class.getName());
    /** Flag to tell if the program is shutdown. */
    private static boolean shutdown = false;

    /** Start/Stop flag of the benchmark test. */
    private static boolean startTest = false;
    /** Tester id. */
    private static int packerId;

    /**
     * Create the quality tester singleton.
     */
    private Logistic() {
        initListeners();
    }

    /**
     * Start the quality tester process.
     *
     * @param arguments A list containing the command line arguments.
     */
    public static void main(final String[] arguments) {
        Logistic.addShutdownHook();
        System.out.println("Leave the factory with Ctrl + C");
        Rocket rocket;
        Purchase purchase;
        JMSCommunication communicator = new JMSCommunication();

        if (arguments.length != 1) {
            System.err.println("Usage: Logistic <Id> !");
            return;
        }
        try {
            packerId = Integer.parseInt(arguments[0]);
        } catch (Exception e) {
            System.err.println("Please supply a valid id!");
            return;
        }
        new Logistic();

        LOGGER.info("Logistician " + packerId + " ready to pack!");

        while (!startTest) {
            Utility.sleep(BenchmarkTest.WAIT_TIME_BENCHMARK);
        }

        while (!shutdown && startTest) {
            // get tested rocket
            rocket = (Rocket) communicator.receiveMessage(
                    QueueDestinations.ROCKET_TESTED_QUEUE);
            if (rocket == null) {
                LOGGER.info("Could not get enough rockets!");
                // put not packed rockets back
                for (Rocket returnRocket : rocketsClassA) {
                    returnRocket.setPackerId(0);
//                    returnRocket.setReadyForCollection(false);
                    communicator.sendMessage(returnRocket,
                            QueueDestinations.ROCKET_TESTED_QUEUE);
                    communicator.sendMessage(returnRocket,
                            QueueDestinations.GUI_QUEUE);
                }
                rocketsClassA.clear();
                for (Rocket returnRocket : rocketsClassB) {
                    returnRocket.setPackerId(0);
//                    returnRocket.setReadyForCollection(false);
                    communicator.sendMessage(returnRocket,
                            QueueDestinations.ROCKET_TESTED_QUEUE);
                    communicator.sendMessage(returnRocket,
                            QueueDestinations.GUI_QUEUE);
                }
                rocketsClassB.clear();
                continue;
            }

            rocket.setPackerId(packerId);

            purchase = rocket.getPurchase();
            switch (rocket.getTestResult()) {
                case A:
                    if (purchase == null) {
                        rocketsClassA.add(rocket);
                    } else {
                        communicator.sendMessage(rocket,
                                QueueDestinations.GUI_QUEUE);
                    }
                    break;
                case B:
                    if (purchase != null) {
                        rocket.setPurchase(null);
                    }
                    rocketsClassB.add(rocket);
                    break;
                case Bad:
                    if (purchase != null) {
                        rocket.setPurchase(null);
                    }
                    communicator.sendMessage(rocket,
                            QueueDestinations.ROCKET_TRASHED_QUEUE);
                    communicator.sendMessage(rocket,
                            QueueDestinations.GUI_QUEUE);
                    break;
                default:
                    LOGGER.severe("Logistician has found wrong "
                            + "quality class!");
                    return;
            }

            if (rocket.getPurchase() == null && purchase != null) {
                communicator.sendMessage(purchase,
                        QueueDestinations.PURCHASE_CURRENT_QUEUE);
            }

            if (rocketsClassA.size() == PACKAGE_SIZE) {
                // get an id and write a new on in the queue
                Integer packageId;
                packageId = (Integer) communicator.receiveMessage(
                            QueueDestinations.ID_PACKET_QUEUE);
                if (packageId == null) {
                    LOGGER.severe("Could not get a package id!");
                    // put not packet rockets back
                    for (Rocket returnRocket : rocketsClassA) {
                        returnRocket.setPackerId(0);
//                        returnRocket.setReadyForCollection(false);
                        communicator.sendMessage(returnRocket,
                                QueueDestinations.ROCKET_TESTED_QUEUE);
                        communicator.sendMessage(returnRocket,
                                QueueDestinations.GUI_QUEUE);
                    }
                    rocketsClassA.clear();
                    continue;
                }
                communicator.sendMessage(packageId + IDS_QUEUES_INIT,
                        QueueDestinations.ID_PACKET_QUEUE);

                RocketPackage rocketPackage = new
                        RocketPackage(packageId, rocketsClassA);
                communicator.sendMessage(rocketPackage,
                        QueueDestinations.ROCKET_SHIPPED_QUEUE);
                communicator.sendMessage(rocketPackage,
                        QueueDestinations.GUI_QUEUE);
                rocketsClassA.clear();
            }
            if (rocketsClassB.size() == PACKAGE_SIZE) {
                // get an id and write a new on in the queue
                Integer packageId;
                packageId = (Integer) communicator.receiveMessage(
                        QueueDestinations.ID_PACKET_QUEUE);
                if (packageId == null) {
                    LOGGER.severe("Could not get a package id!");
                    // put not packet rockets back
                    for (Rocket returnRocket : rocketsClassB) {
                        returnRocket.setPackerId(0);
//                        returnRocket.setReadyForCollection(false);
                        communicator.sendMessage(returnRocket,
                                QueueDestinations.ROCKET_TESTED_QUEUE);
                        communicator.sendMessage(returnRocket,
                                QueueDestinations.GUI_QUEUE);
                    }
                    rocketsClassB.clear();
                    continue;
                }
                communicator.sendMessage(packageId + IDS_QUEUES_INIT,
                        QueueDestinations.ID_PACKET_QUEUE);

                RocketPackage rocketPackage = new
                        RocketPackage(packageId, rocketsClassB);
                communicator.sendMessage(rocketPackage,
                        QueueDestinations.ROCKET_SHIPPED_QUEUE);
                communicator.sendMessage(rocketPackage,
                        QueueDestinations.GUI_QUEUE);
                rocketsClassB.clear();
            }
        }

        // put not packed rockets back
        for (Rocket returnRocket : rocketsClassA) {
            returnRocket.setPackerId(0);
            communicator.sendMessage(returnRocket,
                    QueueDestinations.ROCKET_TESTED_QUEUE);
            communicator.sendMessage(returnRocket,
                    QueueDestinations.GUI_QUEUE);
        }
        rocketsClassA.clear();
        for (Rocket returnRocket : rocketsClassB) {
            returnRocket.setPackerId(0);
            communicator.sendMessage(returnRocket,
                    QueueDestinations.ROCKET_TESTED_QUEUE);
            communicator.sendMessage(returnRocket,
                    QueueDestinations.GUI_QUEUE);
        }
        rocketsClassB.clear();

        communicator.closeCommunication();
    }

    /**
     * adds a shutdown hook (called before shutdown).
     */
    private static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("I'm packing my stuff together.");
                shutdown = true;
                Utility.sleep(WAIT_TIME_TO_SHUTDOWN);
                System.out.println("I'm going home.");
            }
        });
    }

    /**
     * Set the listener for the benchmark.
     */
    private void initListeners() {
        // Set up the namingContext for the JNDI lookup
        final Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
        env.put(Context.PROVIDER_URL, PROVIDER_URL);
        env.put(Context.SECURITY_PRINCIPAL, USERNAME);
        env.put(Context.SECURITY_CREDENTIALS, PASSWORD);
        Context namingContext;

        try {
            namingContext = new InitialContext(env);

            ConnectionFactory connectionFactory = (ConnectionFactory)
                    namingContext.lookup(CONNECTION_FACTORY);

            Destination destination = (Destination) namingContext.lookup(
                    QueueDestinations.BENCHMARK_LOGISTIC_QUEUE);

            JMSConsumer consumer = connectionFactory.createContext(
                    USERNAME, PASSWORD).createConsumer(
                    destination);

            consumer.setMessageListener(this);
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Listener to start/stop the benchmark test.
     *
     * @param message
     *          Signal to start/stop the test.
     */
    @Override
    public void onMessage(final Message message) {
        startTest = !startTest;

        if (startTest) {
            LOGGER.severe("Tester " + packerId + ": Starts the Benchmark");
        } else {
            LOGGER.severe("Tester " + packerId + ": Stops the Benchmark");
        }
    }
}

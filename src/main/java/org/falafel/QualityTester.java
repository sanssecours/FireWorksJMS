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
import static org.falafel.Utility.INITIAL_CONTEXT_FACTORY;
import static org.falafel.Utility.PASSWORD;
import static org.falafel.Utility.PROVIDER_URL;
import static org.falafel.Utility.USERNAME;


/**
 * This class represents the quality tester who decides if a rocket is defect or
 * not. The criteria for a defect rocket are if:
 *      more than one effect charge is faulty
 *      it contains less than 120g of the propellant charge
 */
public final class QualityTester implements MessageListener {
    /** Constant for how long the shutdown hook is waiting. */
    private static final int WAIT_TIME_TO_SHUTDOWN = 5000;
    /** Constant for the minimum.*/
    private static final int MINIMAL_PROPELLANT = 120;
    /** Constant for the minimum propellant to be class A.*/
    private static final Integer MINIMAL_PROP_CLASS_A = 130;
    /** Get the Logger for the current class. */
    private static final Logger LOGGER = getLogger(
            QualityTester.class.getName());
    /** Flag to tell if the program is shutdown. */
    private static boolean shutdown = false;

    /** Start/Stop flag of the benchmark test. */
    private static boolean startTest = false;
    /** Tester id. */
    private static int testerId;

    /**
     * Create the quality tester singleton.
     */
    private QualityTester() {
        initListeners();
    }

    /**
     * Start the quality tester process.
     *
     * @param arguments A list containing the command line arguments.
     */
    public static void main(final String[] arguments) {
        QualityTester.addShutdownHook();
        System.out.println("Leave the factory with Ctrl + C");
        Rocket rocket;
        ArrayList<Effect> effects;
        JMSCommunication communicator = new JMSCommunication();

        if (arguments.length != 1) {
            System.err.println("Usage: QualityTester <Id>!");
            return;
        }
        try {
            testerId = Integer.parseInt(arguments[0]);
        } catch (Exception e) {
            System.err.println("Please supply valid id!");
            return;
        }
        new QualityTester();

        LOGGER.info("Quality tester " + testerId + " ready to test!");

        while (!startTest) {
            Utility.sleep(BenchmarkTest.WAIT_TIME_BENCHMARK);
        }

        while (!shutdown && startTest) {
            rocket = (Rocket) communicator.receiveMessage(
                    QueueDestinations.ROCKET_PRODUCED_QUEUE);
            if (rocket == null) {
                LOGGER.info("Could not get a rocket!");
                continue;
            }

            effects = rocket.getEffects();

            int defectCount = 0;
            for (Effect effect : effects) {
                if (effect.getStatus()) {
                    defectCount++;
                }
            }
            if (defectCount > 1 || rocket.getPropellantQuantity()
                    < MINIMAL_PROPELLANT) {
                rocket.setQualityClassBad();
            } else if (defectCount == 0 && rocket.getPropellantQuantity()
                    >= MINIMAL_PROP_CLASS_A) {
                rocket.setQualityClassA();
            } else {
                rocket.setQualityClassB();
            }

            rocket.setTester(testerId);

            communicator.sendMessage(rocket,
                    QueueDestinations.ROCKET_TESTED_QUEUE);
            communicator.sendMessage(rocket,
                    QueueDestinations.GUI_QUEUE);
        }
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
                    QueueDestinations.BENCHMARK_TESTER_QUEUE);

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
            LOGGER.severe("Tester " + testerId + ": Starts the Benchmark");
        } else {
            LOGGER.severe("Tester " + testerId + ": Stops the Benchmark");
        }
    }
}

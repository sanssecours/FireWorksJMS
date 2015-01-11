package org.falafel;


import java.util.ArrayList;
import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;


/**
 * This class represents the quality tester who decides if a rocket is defect or
 * not. The criteria for a defect rocket are if:
 *      more than one effect charge is faulty
 *      it contains less than 120g of the propellant charge
 */
public final class QualityTester {
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

    /**
     * Create the quality tester singleton.
     */
    private QualityTester() {
    }

    /**
     * Start the quality tester process.
     *
     * @param arguments A list containing the command line arguments.
     */
    public static void main(final String[] arguments) {
        QualityTester.addShutdownHook();
        System.out.println("Leave the factory with Ctrl + C");
        int testerId;
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

        LOGGER.info("Quality tester " + testerId + " ready to test!");

        ArrayList<Object> benchmark = new ArrayList<>();
        while (benchmark.isEmpty()) {
            benchmark = communicator.readMessagesInQueue(
                    QueueDestinations.BENCHMARK_QUEUE);
        }
        LOGGER.severe("Tester " + testerId + ": Starts the Benchmark");
        while (!shutdown) {
            benchmark.clear();
            benchmark = communicator.readMessagesInQueue(
                    QueueDestinations.BENCHMARK_QUEUE);

            if (benchmark.isEmpty()) {
                LOGGER.severe("Tester " + testerId + ": Benchmark stopped!");
                shutdown = true;
                break;
            }

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
}

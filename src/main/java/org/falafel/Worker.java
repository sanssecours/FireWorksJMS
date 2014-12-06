package org.falafel;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import static java.util.logging.Logger.getLogger;
import static org.falafel.QueueDestinations.STORAGE_CASING_QUEUE;
import static org.falafel.QueueDestinations.STORAGE_EFFECT_QUEUE;
import static org.falafel.QueueDestinations.STORAGE_OPENED_PROP_QUEUE;
import static org.falafel.Utility.sleep;
import static org.falafel.QueueDestinations.STORAGE_WOOD_QUEUE;

/**
 * This class represents a worker. A worker collects materials and uses them
 * to create a rocket.
 */
public final class Worker {

    /** Constant for the lower bound of the working time per rocket. */
    private static final int LOWER_BOUND = 1000;
    /** Constant for the upper bound of the working time per rocket. */
    private static final int UPPER_BOUND = 2000;
    /** Constant for how long the shutdown hook is waiting. */
    private static final int WAIT_TIME_TO_SHUTDOWN = 5000;
    /** How many effect charges are needed to build a rocket. */
    private static final int NUMBER_EFFECTS_NEEDED = 3;
    /** Get the Logger for the current class. */
    private static final Logger LOGGER = getLogger(Worker.class.getName());

    /** Specifies if we want to terminate the program. This variable will be
     *  set to true after we press CTR-C. */
    private static boolean shutdown = false;

    /** Create the worker singleton. */
    private Worker() { }

    /**
     * Start the worker process.
     *
     * @param arguments
     *          A list containing the command line arguments.
     *
     */
    public static void main(final String[] arguments) {
        int workerId;

        Casing casing;
        ArrayList<Effect> effects = new ArrayList<>();
        JMSCommunication communication = new JMSCommunication();
        Propellant propellant;
        Random randomGenerator = new Random();
        Wood wood;

        if (arguments.length != 1) {
            System.err.println("Usage: worker <Id>!");
            return;
        }

        try {
            workerId = Integer.parseInt(arguments[0]);
        } catch (Exception e) {
            System.err.println("Please supply a valid id!");
            return;
        }

        Worker.addShutdownHook();

        LOGGER.info("Worker " + workerId + " ready to work!");

        while (!shutdown) {
            /** Get materials */
            wood = (Wood) communication.receiveMessage(STORAGE_WOOD_QUEUE);
            LOGGER.info("Took material: " + wood);
            casing = (Casing) communication.receiveMessage(
                    STORAGE_CASING_QUEUE);
            LOGGER.info("Took material: " + casing);
            IntStream.range(0, NUMBER_EFFECTS_NEEDED).forEach(
                effect -> effects.add((Effect) communication.receiveMessage(
                        STORAGE_EFFECT_QUEUE)));
            LOGGER.info("Took material: " + effects);
            propellant = (Propellant) communication.receiveMessage(
                    STORAGE_OPENED_PROP_QUEUE);
            LOGGER.info("Took material: " + propellant);

            // The time needed to produce a rocket
            int waitingTime = randomGenerator.nextInt(
                    UPPER_BOUND - LOWER_BOUND) + LOWER_BOUND;
            sleep(waitingTime);
        }

    }

    /**
     * Add a shutdown hook.
     *
     * This method will be called when the worker is terminated.
     *
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

package org.falafel;

import java.util.Random;
import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;
import static org.falafel.Utility.sleep;

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
        Worker.addShutdownHook();

        int workerId;
        Random randomGenerator = new Random();

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

        LOGGER.info("Worker " + workerId + " ready to work!");

        // The time needed to produce a rocket
        int waitingTime = randomGenerator.nextInt(
                UPPER_BOUND - LOWER_BOUND) + LOWER_BOUND;
        sleep(waitingTime);

        while (!shutdown) {
            sleep(WAIT_TIME_TO_SHUTDOWN);
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

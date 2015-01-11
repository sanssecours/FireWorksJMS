package org.falafel;

import java.util.logging.Logger;


/**
 * Thread to control the benchmark test.
 */
public class BenchmarkTest extends Thread {

    /** Length of the Test in ms. */
    private static final int TEST_TIME = 60000;
    /** Constant for how long the threads should sleep while waiting for the
     * benchmark to start. */
    public static final int WAIT_TIME_BENCHMARK = 500;
    /** Get the Logger for the current class. */
    private static final Logger LOGGER = Logger.getLogger(
            BenchmarkTest.class.getName());

    /**
     * Create the benchmark test with the URI of the mozart space.
     *
     */
    public BenchmarkTest() { }

    /**
     * Start the benchmark test.
     */
    public final void run() {
        JMSCommunication communication = new JMSCommunication();

        communication.sendMessage(1,
                QueueDestinations.BENCHMARK_WORKER_1001_QUEUE);
        communication.sendMessage(1,
                QueueDestinations.BENCHMARK_WORKER_1002_QUEUE);
        communication.sendMessage(1,
                QueueDestinations.BENCHMARK_TESTER_QUEUE);
        communication.sendMessage(1,
                QueueDestinations.BENCHMARK_LOGISTIC_QUEUE);

        LOGGER.severe("Start of the Benchmark!");
        try {
            Thread.sleep(TEST_TIME);
        } catch (InterruptedException e) {
            System.out.println("Benchmark test sleep disturbed!");
        }

        communication.sendMessage(1,
                QueueDestinations.BENCHMARK_WORKER_1001_QUEUE);
        communication.sendMessage(1,
                QueueDestinations.BENCHMARK_WORKER_1002_QUEUE);
        communication.sendMessage(1,
                QueueDestinations.BENCHMARK_TESTER_QUEUE);
        communication.sendMessage(1,
                QueueDestinations.BENCHMARK_LOGISTIC_QUEUE);

        LOGGER.severe("End of the Benchmark!");
    }
}

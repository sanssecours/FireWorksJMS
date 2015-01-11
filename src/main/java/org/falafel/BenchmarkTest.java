package org.falafel;

import java.util.logging.Logger;


/**
 * Thread to control the benchmark test.
 */
public class BenchmarkTest extends Thread {

    /** Length of the Test in ms. */
    private static final int TEST_TIME = 60000;
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

        communication.sendMessage(1, QueueDestinations.BENCHMARK_QUEUE);

        LOGGER.severe("Start of the Benchmark!");
        try {
            Thread.sleep(TEST_TIME);
        } catch (InterruptedException e) {
            System.out.println("Benchmark test sleep disturbed!");
        }

        communication.receiveMessage(QueueDestinations.BENCHMARK_QUEUE);

        LOGGER.severe("End of the Benchmark!");
    }
}

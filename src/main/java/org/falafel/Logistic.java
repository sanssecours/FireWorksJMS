package org.falafel;

import java.util.ArrayList;
import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;
import static org.falafel.Utility.IDS_QUEUES_INIT;

/**
 * This class represents a logistic worker.
 */
public final class Logistic {

    /** Specifies how long a logistic worker waits until he tries to get
     *  new rockets after he was unable to get them the last time. */
    private static final int WAIT_TIME_LOGISTIC_MS = 2000;
    /** Constant for how many rockets are in one package. */
    private static final int PACKAGE_SIZE = 5;
    /** Constant for how long the shutdown hook is waiting. */
    private static final int WAIT_TIME_TO_SHUTDOWN = 5000;
    /** Collected functioning rockets. */
    private static ArrayList<Rocket> functioningRockets = new ArrayList<>();
    /** Get the Logger for the current class. */
    private static final Logger LOGGER = getLogger(Logistic.class.getName());
    /** Flag to tell if the program is shutdown. */
    private static boolean shutdown = false;

    /**
     * Create the quality tester singleton.
     */
    private Logistic() {
    }

    /**
     * Start the quality tester process.
     *
     * @param arguments A list containing the command line arguments.
     */
    public static void main(final String[] arguments) {
        Logistic.addShutdownHook();
        System.out.println("Leave the factory with Ctrl + C");
        int packerId;
        Rocket rocket;
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

        LOGGER.info("Logistician " + packerId + " ready to pack!");

        while (!shutdown) {
            // get tested rocket
            rocket = (Rocket) communicator.receiveMessage(
                    QueueDestinations.ROCKET_TESTED_QUEUE);
            if (rocket == null) {
                LOGGER.info("Could not get enough rockets!");
                // put not packed rockets back
                for (Rocket returnRocket : functioningRockets) {
                    returnRocket.setPackerId(0);
//                    returnRocket.setReadyForCollection(false);
                    communicator.sendMessage(returnRocket,
                            QueueDestinations.ROCKET_TESTED_QUEUE);
                    communicator.sendMessage(returnRocket,
                            QueueDestinations.GUI_QUEUE);
                }
                functioningRockets.clear();
                Utility.sleep(WAIT_TIME_LOGISTIC_MS);
                continue;
            }

            rocket.setPackerId(packerId);
//            rocket.setReadyForCollection(true);

            // trash rocket if its defect
//            if (rocket.getTestResult()) {
//                communicator.sendMessage(rocket,
//                        QueueDestinations.ROCKET_TRASHED_QUEUE);
//                communicator.sendMessage(rocket,
//                        QueueDestinations.GUI_QUEUE);
//            } else {
//                functioningRockets.add(rocket);
//            }

            if (functioningRockets.size() == PACKAGE_SIZE) {
                // get an id and write a new on in the queue
                Integer packageId;
                packageId = (Integer) communicator.receiveMessage(
                            QueueDestinations.ID_PACKET_QUEUE);
                if (packageId == null) {
                    LOGGER.severe("Could not get a package id!");
                    // put not packet rockets back
                    for (Rocket returnRocket : functioningRockets) {
                        returnRocket.setPackerId(0);
//                        returnRocket.setReadyForCollection(false);
                        communicator.sendMessage(returnRocket,
                                QueueDestinations.ROCKET_TESTED_QUEUE);
                        communicator.sendMessage(returnRocket,
                                QueueDestinations.GUI_QUEUE);
                    }
                    functioningRockets.clear();
                    Utility.sleep(WAIT_TIME_LOGISTIC_MS);
                    continue;
                }
                communicator.sendMessage(packageId + IDS_QUEUES_INIT,
                        QueueDestinations.ID_PACKET_QUEUE);

                RocketPackage rocketPackage = new
                        RocketPackage(packageId, functioningRockets);
                communicator.sendMessage(rocketPackage,
                        QueueDestinations.ROCKET_SHIPPED_QUEUE);
                communicator.sendMessage(rocketPackage,
                        QueueDestinations.GUI_QUEUE);
                functioningRockets.clear();
            }
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

package org.falafel;

import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;

/**
 * This class represents a supplier. Suppliers deliver certain
 * {@code Materials} to the firework factory.
 *
 */
public class WritePurchasesToCurrentQueue extends Thread {

    /** Get the Logger for the current class. */
    private static final Logger LOGGER = getLogger(
            WritePurchasesToCurrentQueue.class.getName());
    /** The purchase order which is written to the space. */
    private final Purchase purchase;

    /**
     * Create a new Supplier with a given id.
     *
     * @param purchase
     *          The purchase order which is written to the space.
     */
    WritePurchasesToCurrentQueue(final Purchase purchase) {
        super();
        this.purchase = purchase;
    }

    /**
     * Start the writer.
     */
    public final void run() {
        JMSCommunication communication = new JMSCommunication();

        for (int index = 0;
             index < purchase.getNumberRocketsProperty().intValue(); index++) {
            communication.sendMessage(purchase,
                    QueueDestinations.PURCHASE_CURRENT_QUEUE);
        }
        communication.closeCommunication();
    }
}

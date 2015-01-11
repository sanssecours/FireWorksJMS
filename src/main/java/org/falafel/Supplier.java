package org.falafel;

import java.util.logging.Logger;
import java.util.Random;


/**
 * This class represents a supplier. Suppliers deliver certain
 * {@code Materials} to the firework factory.
 *
 */
public class Supplier extends Thread {

    /** Constant for the division by 100. */
    private static final double HUNDRED = 100.0;
    /** Save the (unique) identifier for this supplier. */
    private final int id;
    /** Get the Logger for the current class. */
    private static final Logger LOGGER =
            Logger.getLogger(Supplier.class.getName());
    /** The order which the supplier shipped. */
    private final SupplyOrder order;
    /** Save the (unique) identifier for the materials in this order. */
    private final int materialId;
    /**
     * Create a new Supplier with a given id.
     *
     * @param identifier
     *          The (unique) identifier for this supplier
     * @param order
     *          The order the supplier should provide
     * @param startId
     *          Saves the first identifier of the ids that should be used for
     *          the materials in the order.
     */
    public Supplier(final int identifier, final SupplyOrder order,
                    final int startId) {
        super();
        id = identifier;
        this.order = order;
        materialId = startId;
    }

    /**
     * Start the supplier.
     */
    public final void run() {
        JMSCommunication communicator = new JMSCommunication();

        int functioningElements = (int) Math.ceil(
                order.getQuantity() * order.getQuality() / HUNDRED);
        boolean defect;
        Material newEntry;
        String destinationStorage;

        String orderType = order.getType();
        String orderSupplier = order.getSupplierName();
        String casing = MaterialType.Casing.toString();
        String effect = MaterialType.Effect.toString();
        String propellant = MaterialType.Propellant.toString();

        System.out.println("Supplier " + id + " active!");

        Random randomGenerator = new Random();

        for (int index = 0; index < order.getQuantity(); index++) {

            if (orderType.equals(casing)) {
                newEntry = new Casing(materialId, orderSupplier, id);
                destinationStorage = QueueDestinations.STORAGE_CASING_QUEUE;
            } else if (orderType.equals(effect)) {
                defect = index >= functioningElements;
                switch (order.getColor()) {
                    case Blue:
                        newEntry = new Effect(materialId, orderSupplier, id,
                                defect, EffectColor.Blue);
                        destinationStorage =
                                QueueDestinations.STORAGE_BLUE_EFFECT_QUEUE;
                        break;
                    case Green:
                        newEntry = new Effect(materialId, orderSupplier, id,
                                defect, EffectColor.Green);
                        destinationStorage =
                                QueueDestinations.STORAGE_GREEN_EFFECT_QUEUE;
                        break;
                    case Red:
                        newEntry = new Effect(materialId, orderSupplier, id,
                                defect, EffectColor.Red);
                        destinationStorage =
                                QueueDestinations.STORAGE_RED_EFFECT_QUEUE;
                        break;
                    default:
                        LOGGER.severe("Wrong effect color in order!");
                        return;
                }
            } else if (orderType.equals(propellant)) {
                newEntry = new Propellant(materialId, orderSupplier, id,
                        Propellant.CLOSED);
                destinationStorage =
                        QueueDestinations.STORAGE_CLOSED_PROP_QUEUE;
            } else {
                newEntry = new Wood(materialId, orderSupplier, id);
                destinationStorage = QueueDestinations.STORAGE_WOOD_QUEUE;
            }

            newEntry.setID(materialId + index);

            // Send to resource queues
            communicator.sendMessage(newEntry, destinationStorage);

            // Send to GUI
            communicator.sendMessage(newEntry, QueueDestinations.GUI_QUEUE);


            LOGGER.info("Supplier " + id + " Wrote entry to queue "
                    + orderType);
        }
        communicator.closeCommunication();
    }
}

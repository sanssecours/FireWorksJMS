package org.falafel;

import java.util.logging.Logger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Random;


/**
 * This class represents a supplier. Suppliers deliver certain
 * {@code Materials} to the firework factory.
 *
 */
public class Supplier extends Thread {

    /** Constant for the lower bound of the loading time per element. */
    private static final int LOWERBOUND = 1000;
    /** Constant for the upper bound of the loading time per element. */
    private static final int UPPERBOUND = 2000;
    /** Constant for the division by 100. */
    private static final double HUNDRED = 100.0;
    /** Save the (unique) identifier for this supplier. */
    private final int id;
    /** Get the Logger for the current class. */
    private static final Logger LOGGER =
            Logger.getLogger(Supplier.class.getName());
    /** The resource identifier for the space. */
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

        int functioningElements = (int) Math.ceil(
                order.getQuantity() * order.getQuality() / HUNDRED);
        boolean defect;

        ArrayList<Material> result;
        Material newEntry;

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
            } else if (orderType.equals(effect)) {
                defect = index >= functioningElements;
                newEntry = new Effect(materialId, orderSupplier, id, defect);
            } else if (orderType.equals(propellant)) {
                newEntry = new Propellant(materialId, orderSupplier, id,
                        Propellant.CLOSED);
            } else {
                newEntry = new Wood(materialId, orderSupplier, id);
            }

            int waitingTime = randomGenerator.nextInt(
                    UPPERBOUND - LOWERBOUND) + LOWERBOUND;
            try {
                Thread.sleep(waitingTime);
            } catch (InterruptedException e) {
                LOGGER.severe("I was interrupted while trying to sleep. "
                        + "How rude!");
            }

            newEntry.setID(materialId + index);

            // write in queue


            LOGGER.info("Supplier " + id + " Wrote entry to container "
                    + orderType);
        }
    }
}

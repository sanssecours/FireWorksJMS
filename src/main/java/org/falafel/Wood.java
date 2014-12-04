package org.falafel;

/**
 * This class represents a piece of wood used to create a rocket.
 */
public class Wood extends Material {

    /**
     * Create a new material with the given attributes.
     *
     * @param identifier
     *          A integer value that (uniquely) identifies this material.
     * @param supplierName
     *          The name of the supplier
     * @param supplierId
     *          A integer value that (uniquely) identifies the supplier.
     *
     */
    public Wood(final int identifier, final String supplierName,
                final int supplierId) {
        super(identifier, supplierName, supplierId);
    }

    /**
     * Return the string representation of the wood.
     *
     * @return A string containing properties of this wood
     */
    public final String toString() {
        return "Wood: " + super.toString();
    }
}

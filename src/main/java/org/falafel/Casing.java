package org.falafel;

/**
 * This class represents a casing used to create a rocket.
 */
public class Casing extends Material {

    /**
     * Create a new casing with the given attributes.
     *
     * @param identifier
     *          A integer value that (uniquely) identifies this material.
     * @param supplierName
     *          The name of the supplier
     * @param supplierId
     *          A integer value that (uniquely) identifies the supplier.
     *
     */
    public Casing(final int identifier, final String supplierName,
                  final int supplierId) {
        super(identifier, supplierName, supplierId);
    }

    /**
     * Return the string representation of the casing.
     *
     * @return A string containing properties of this casing
     */
    public final String toString() {
        return "Casing: " + super.toString();
    }
}

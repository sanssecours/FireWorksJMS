package org.falafel;

/**
 * A class representing an effect charge.
 */
public class Effect extends Material {

    /** Save if this effect is defect. */
    private boolean defect;
    /** Save the color of the effect. */
    private EffectColor color;

    /**
     * Create new effect with the given attributes.
     *
     * @param identifier
     *          A integer value that (uniquely) identifies this material.
     * @param supplierName
     *          The name of the supplier
     * @param supplierId
     *          A integer value that (uniquely) identifies the supplier.
     * @param defect
     *          A boolean value specifying if this effect is defect or not
     */
    public Effect(final int identifier, final String supplierName,
                  final int supplierId, final boolean defect,
                  final EffectColor color) {
        super(identifier, supplierName, supplierId);
        this.defect = defect;
        this.color = color;
    }

    /**
     * Return the defect status of the effect.
     *
     * @return A boolean of this effect charges defect status
     */
    public final Boolean getStatus() {
        return defect;
    }

    /**
     * Getter for the color of th effect charge.
     *
     * @return the color of the effect charge
     */
    public final EffectColor getColor() {
        return color;
    }

    /**
     * Return the string representation of the effect.
     *
     * @return A string containing properties of this effect charge
     */
    public final String toString() {
        return "Effect: " + super.toString() + " -- Defect: " + defect;
    }
}

package org.falafel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *  Class to save the orders.
 */
public class SupplyOrder {

    /** Defines the name of the supplier. */
    private StringProperty supplierName;
    /** Defines the type of the material the supplier provides. */
    private StringProperty type;
    /** Defines how much material the supplier provides. */
    private StringProperty quantityStringProperty;
    /** Defines how much mof the supplied material is broken. */
    private StringProperty qualityStringProperty;
    /** Defines which color the supplied material is. */
    private StringProperty colorStringProperty;

    /** The quantity of the current material. */
    private Integer quantity;
    /** The quality of the current material. */
    private Integer quality;
    /** The color of the current material. */
    private EffectColor color;

    /**
     * Create a new order with the specified attributes.
     *
     * @param name
     *          The name of the supplier
     * @param type
     *          The type of the supplied material
     * @param color
     *          The color of the supplied material
     * @param quantity
     *          The quantity of the supplied material
     * @param quality
     *          The quality of the supplied material
     */
    public SupplyOrder(final String name, final String type,
                       final EffectColor color, final int quantity,
                       final int quality) {
        supplierName = new SimpleStringProperty(name);
        this.type = new SimpleStringProperty(type);
        this.quantity = quantity;
        this.quantityStringProperty =
                new SimpleStringProperty(Integer.toString(quantity));
        this.quality = quality;
        this.qualityStringProperty =
                new SimpleStringProperty(Integer.toString(quality));
        this.color = color;
        this.colorStringProperty = new SimpleStringProperty(color.toString());
    }

    //CHECKSTYLE:OFF
    /** Create a new predefined order. */
    public SupplyOrder() {
        this("Name", "Wood", EffectColor.Blue, 1, 100);
    }
    //CHECKSTYLE:ON

    /**
     * Get the name of the supplier.
     *
     * @return A string containing the name of the supplier.
     */
    public final String getSupplierName() {
        return supplierName.get();
    }

    /**
     * Set the name of the supplier.
     *
     * @param name
     *          The name which should be used for the supplier.
     */
    public final void setSupplierName(final String name) {
        this.supplierName.set(name);
    }

    /**
     * Get the supplier name.
     *
     * @return Returns the string property of the supplier.
     */
    public final StringProperty supplierNameProperty() {
        return supplierName;
    }

    /**
     * Get the type of material provided by the supplier.
     *
     * @return The string property for the type
     */
    public final StringProperty typeProperty() {
        return type;
    }

    /**
     * Get the material for the order.
     *
     * @return A string containing the type of the supplied material
     */
    public final String getType() {
        return type.get();
    }

    /**
     * Set the material for the order.
     *
     * @param type
     *          The type which should be used for the order
     */
    public final void setType(final String type) {
        this.type.set(type);
    }

    /**
     * Get the quantity of the material in this order as string property.
     *
     * @return A string property representing the quantity of material for this
     *         order.
     */
    public final StringProperty quantityProperty() {
        return quantityStringProperty;
    }

    /**
     * Get the quantity of the material in this order.
     *
     * @return A integer representing the quantity of material for this
     *         order.
     */
    public final int getQuantity() {
        return quantity;
    }

    /**
     * Set the quantity of the material.
     *
     * @param quantity
     *          The quantity of the specified material
     */
    public final void setQuantity(final int quantity) {
        this.quantity = quantity;
        this.quantityStringProperty.set(this.quantity.toString());
    }

    /**
     * Get the quality of the material as string property.
     *
     * @return The quality of the material
     */
    public final StringProperty qualityProperty() {
        return qualityStringProperty;
    }

    /**
     * Get the color of the material as string property.
     *
     * @return The color of the material
     */
    public final StringProperty colorProperty() {
        return colorStringProperty;
    }

    /**
     * Get the quality of the material.
     *
     * @return The quality of the current material
     */
    public final int getQuality() {
        return quality;
    }

    /**
     * Set the quality of the material for the order.
     *
     * @param quality
     *          The quality for the material
     *
     */
    public final void setQuality(final int quality) {
        this.quality = quality;
        this.qualityStringProperty.set(this.quality.toString());
    }

    /**
     * Get the effect color of the order.
     *
     * @return color of the order
     */
    public final EffectColor getColor() {
        return color;
    }

    /**
     * Set the effect color of the order.
     *
     * @param newColor the new color of the order
     */
    public final void setColor(final String newColor) {
        colorStringProperty.set(newColor);
        if (newColor.equals(EffectColor.Blue.toString())) {
            color = EffectColor.Blue;
        } else if (newColor.equals(EffectColor.Green.toString())) {
            color = EffectColor.Green;
        } else {
            color = EffectColor.Red;
        }
    }
    /**
     * Get the string representation for the order.
     *
     * @return A string representing the order
     */
    public final String toString() {
        return "Order: Supplier: " +  supplierName.get() + " -- Type: "
                + type.get() + " -- Color: " + getColor()
                + " -- Quantity: " + getQuantity()
                + " -- Quality: " + getQuality();
    }
}

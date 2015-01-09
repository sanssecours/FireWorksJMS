package org.falafel;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

/**
 * .
 */
public class Rocket implements Serializable {

    /** The different classes of the rocket. */
    public enum QualityClass { A, B, NotSet, Bad }
    /** The identification of this rocket. */
    private Integer id;
    /** The id of the package that contains this rocket. */
    private Integer packageId = 0;
    /** The wood used to construct this rocket. */
    private Wood wood;
    /** The casing used to construct this rocket. */
    private Casing casing;
    /** The list of effects used to construct this rocket. */
    private ArrayList<Effect> effects;
    /** The propellants together with the amounts of them used to create this
     *  rocket. */
    private HashMap<Propellant, Integer> propellants;
    /** The whole amount of propellant used to create this rocket. */
    private Integer propellantQuantity;
    /** The id of the worker that put together this rocket. */
    private Integer workerId;
    /** The id of the tester that checked this rocket. */
    private Integer testerId = 0;
    /** The id of the logistician that boxed this rocket. */
    private Integer packerId = 0;
    /** This value specifies if this rocket is defect or not. */
    //private Boolean testResult = false;
    /** The id of the purchase order. */
    private Purchase purchase = null;
    /** The class of the rocket. */
    private QualityClass qualityClass = QualityClass.NotSet;

    /**
     * Create a new rocket with the given arguments.
     *
     * @param rocketId
     *          The identifier for the rocket
     * @param wood
     *          The wood used to construct the new rocket
     * @param casing
     *          The casing used to construct the new rocket
     * @param effects
     *          A list of effects used to create the new rocket
     * @param propellants
     *          The propellants used to create the new rocket
     * @param propellantQuantity
     *          The amount of propellant contained in the new rocket
     * @param workerId
     *          The id of the worker that created the new rocket
     */
    public Rocket(final Integer rocketId,
                    final Wood wood,
                    final Casing casing,
                    final ArrayList<Effect> effects,
                    final HashMap<Propellant, Integer> propellants,
                    final Integer propellantQuantity,
                    final Integer workerId) {
        id = rocketId;
        this.wood = wood;
        this.casing = casing;
        this.effects = effects;
        this.propellants = propellants;
        this.propellantQuantity = propellantQuantity;
        this.workerId = workerId;
    }

    /**
     * Create a new rocket with the given arguments.
     *
     * @param rocketId
     *          The identifier for the rocket
     * @param wood
     *          The wood used to construct the new rocket
     * @param casing
     *          The casing used to construct the new rocket
     * @param effects
     *          A list of effects used to create the new rocket
     * @param propellants
     *          The propellants used to create the new rocket
     * @param propellantQuantity
     *          The amount of propellant contained in the new rocket
     * @param workerId
     *          The id of the worker that created the new rocket
     * @param purchase
     *          The purchase for which the rockets is produced
     */
    public Rocket(final Integer rocketId,
                  final Wood wood,
                  final Casing casing,
                  final ArrayList<Effect> effects,
                  final HashMap<Propellant, Integer> propellants,
                  final Integer propellantQuantity,
                  final Integer workerId,
                  final Purchase purchase) {
        this(rocketId, wood, casing, effects, propellants, propellantQuantity,
                workerId);
        this.purchase = purchase;
    }
    /**
     * Returns the id of the rocket.
     *
     * @return Returns the ids of the rocket as IntegerProperty.
     */
    public final IntegerProperty getIdProperty() {
        return new SimpleIntegerProperty(id);
    }
    /**
     * Returns the id of the package in which the rocket is placed.
     *
     * @return Returns the ids of the package as IntegerProperty.
     */
    public final IntegerProperty getPackageIdProperty() {
        return new SimpleIntegerProperty(packageId);
    }
    /**
     * Returns the id of the built in casing.
     *
     * @return Returns the id of the casing as a IntegerProperty.
     */
    public final IntegerProperty getCasingIdProperty() {
        return new SimpleIntegerProperty(casing.getID());
    }
    /**
     * Returns the id of the built in packages as a string containing the used
     * ids.
     *
     * @return Returns the ids of the used propellant packages as
     *          StringProperty.
     */
    public final StringProperty getPropellantPackageIdProperty() {
        ArrayList<String> returnString = new ArrayList<>();
        for (Propellant propellant : propellants.keySet()) {
            returnString.add(Integer.toString(propellant.getID()));
            returnString.add(Integer.toString(propellants.get(propellant)));
        }
        return new SimpleStringProperty(returnString.toString());
    }
    /**
     * Returns the id of the built in wood.
     *
     * @return Returns the IntegerProperty of the built in wood.
     */
    public final IntegerProperty getWoodIdProperty() {
        return new SimpleIntegerProperty(wood.getID());
    }
    /**
     * Returns the id of the built in effects as a string containing the used
     * ids.
     *
     * @return Returns the ids of the used effects as StringProperty.
     */
    public final StringProperty getEffectIdProperty() {
        ArrayList<String> returnString = new ArrayList<>();
        for (Effect effect : effects) {
            returnString.add(Integer.toString(effect.getID()));
            returnString.add(effect.getColor().toString());
            if (testerId == 0) {
                returnString.add("not tested");
            } else if (effect.getStatus()) {
                returnString.add("defect");
            } else {
                returnString.add("ok");
            }
        }
        return new SimpleStringProperty(returnString.toString());
    }
    /**
     * Returns the quantity used in the rocket.
     *
     * @return Returns the propellant quantity as IntegerProperty.
     */
    public final IntegerProperty getPropellantQuantityProperty() {
        return new SimpleIntegerProperty(propellantQuantity);
    }
    /**
     * Returns the the result of the quality test.
     *
     * @return Returns the test result of the quality test as StringProperty.
     */
    public final StringProperty getTestResultProperty() {
        return new SimpleStringProperty(qualityClass.toString());
    }
    /**
     * Returns the the id of the worker who built the rocket.
     *
     * @return Returns the worker id as IntegerProperty.
     */
    public final IntegerProperty getWorkerIdProperty() {
        return new SimpleIntegerProperty(workerId);
    }
    /**
     * Returns the the id of the quality tester who tested the rocket.
     *
     * @return Returns the tester id as IntegerProperty.
     */
    public final IntegerProperty getTesterIdProperty() {
        return new SimpleIntegerProperty(testerId);
    }
    /**
     * Returns the the id of the logistician who packed up the rocket.
     *
     * @return Returns the logistician id as IntegerProperty.
     */
    public final IntegerProperty getPackerIdProperty() {
        return new SimpleIntegerProperty(packerId);
    }
    /**
     * Returns the the id of the supplier who delivered the used wood.
     *
     * @return Returns the supplier of the wood id as IntegerProperty.
     */
    public final IntegerProperty getSupplierWoodIdProperty() {
        return new SimpleIntegerProperty(wood.getSupplierId());
    }
    /**
     * Returns the the id of the supplier who delivered the used casing.
     *
     * @return Returns the supplier of the casing id as IntegerProperty.
     */
    public final IntegerProperty getSupplierCasingIdProperty() {
        return new SimpleIntegerProperty(casing.getSupplierId());
    }
    /**
     * Returns the the id of the suppliers who delivered the used propellant
     * packages.
     *
     * @return Returns the suppliers of the propellant charges ids as
     *          StringProperty.
     */
    public final StringProperty getSupplierPropellantIdProperty() {
        HashSet<String> returnString = new HashSet<>();
        for (Propellant propellant : propellants.keySet()) {
            returnString.add(Integer.toString(propellant.getSupplierId()));
        }
        return new SimpleStringProperty(returnString.toString());
    }
    /**
     * Returns the the id of the suppliers who delivered the used effect charges
     * packages.
     *
     * @return Returns the suppliers of the effect charges ids as
     * StringProperty.
     */
    public final StringProperty getSupplierEffectIdProperty() {
        TreeSet<String> returnString = new TreeSet<>();
        for (Effect effect : effects) {
            returnString.add(Integer.toString(effect.getSupplierId()));
        }
        return new SimpleStringProperty(returnString.toString());
    }
    /**
     * Set the id of the Rocket.
     *
     * @param newId integer value which is set for the rocket id
     */
    public final void setNewRocketId(final Integer newId) {
        id = newId;
    }
    /**
     * returns the id of the rocket.
     *
     * @return the id of the rocket
     */
    public final Integer getRocketId() {
        return id;
    }
    /**
     * Return the effect charges of the rocket.
     *
     * @return the array list with the effect charger
     */
    public final ArrayList<Effect> getEffects() {
        return effects;
    }
    /**
     * Set the result of quality test to class A.
     */
    public final void setQualityClassA() {
        qualityClass = QualityClass.A;
    }
    /**
     * Set the result of quality test to class B.
     */
    public final void setQualityClassB() {
        qualityClass = QualityClass.B;
    }
    /**
     * Set the result of quality test to class B.
     */
    public final void setQualityClassBad() {
        qualityClass = QualityClass.Bad;
    }
    /**
     * Return the quantity of the propellant charges of the rocket.
     *
     * @return the integer value of the propellant charge
     */
    public final Integer getPropellantQuantity() {
        return propellantQuantity;
    }
    /**
     * returning the result of the quality test.
     *
     * @return boolean value of the test result
     */
    public final QualityClass getTestResult() {
        return qualityClass;
    }
    /**
     * sets the id of the quality tester who tested the rocket.
     *
     * @param testerId of the quality tester
     */
    public final void setTester(final int testerId) {
        this.testerId = testerId;
    }
    /**
     * Set the id of the worker who packed the rocket in logistics.
     *
     * @param packerId the id for the packer
     */
    public final void setPackerId(final Integer packerId) {
        this.packerId = packerId;
    }
    /**
     * Set the id of the package containing the rocket.
     *
     * @param id of the package
     */
    public final void setPackageId(final Integer id) {
        packageId = id;
    }

    /**
     * Get the IntegerProperty of the purchase order or 0 if its a random
     * rocket.
     *
     * @return purchase id if the rocket is part of a purchase
     */
    public final IntegerProperty getPurchaseIdProperty() {
        if (purchase == null) {
            return new SimpleIntegerProperty(0);
        } else {
            return purchase.getPurchaseId();

        }
    }
    /**
     * Get the IntegerProperty of the purchase order or 0 if its a random
     * rocket.
     *
     * @return purchase id if the rocket is part of a purchase
     */
    public final IntegerProperty getPurchaseBuyerIdProperty() {
        if (purchase == null) {
            return new SimpleIntegerProperty(0);
        } else {
            return purchase.getBuyerId();

        }
    }

    /**
     * Get the purchase of the rocket.
     *
     * @return the purchase or null if the rocket was not built for a purchase
     */
    public final Purchase getPurchase() {
        return purchase;
    }

    /**
     * Set the purchase of the rocket.
     *
     * @param purchase
     *          the new purchase of the rocket.
     */
    public final void setPurchase(final Purchase purchase) {
        this.purchase = purchase;
    }

    /**
     * Return the string representation of the rocket.
     *
     * @return A string containing the rocket
     */
    public final String toString() {
        return "Rocket Id: " + id;
    }

    /**
     * Get the id of the logistician who packed the rocket.
     *
     * @return the packer id.
     */
    public final Integer getPackerId() {
        return packerId;
    }
}

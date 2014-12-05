package org.falafel;

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

    Integer id;
    Integer packageId = 0;
    Wood wood;
    Casing casing;
    ArrayList<Effect> effects;
    HashMap<Propellant, Integer> propellants;
    Integer propellantQuantity;
    Integer workerId;
    Integer testerId = 0;
    Integer packerId = 0;
    Boolean testResult = false;
    Boolean readyForCollection = false;

    public Rocket (Integer rocketId,
                   Wood wood,
                   Casing casing,
                   ArrayList<Effect> effects,
                   HashMap<Propellant, Integer> propellants,
                   Integer propellantQuantity,
                   Integer workerId) {
        id = rocketId;
        this.wood = wood;
        this.casing = casing;
        this.effects = effects;
        this.propellants = propellants;
        this.propellantQuantity = propellantQuantity;
        this.workerId = workerId;
    }
    /**
     * Returns the id of the rocket.
     *
     * @return Returns the ids of the rocket as StringProperty.
     */
    public final StringProperty getIdProperty() {
        return new SimpleStringProperty(id.toString());
    }
    /**
     * Returns the id of the package in which the rocket is placed.
     *
     * @return Returns the ids of the package as StringProperty.
     */
    public final StringProperty getPackageIdProperty() {
        return new SimpleStringProperty(packageId.toString());
    }
    /**
     * Returns the id of the built in casing.
     *
     * @return Returns the id of the casing as a StringProperty.
     */
    public final StringProperty getCasingIdProperty() {
        return new SimpleStringProperty(Integer.toString(casing.getID()));
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
     * @return Returns the string property of the built in wood.
     */
    public final StringProperty getWoodIdProperty() {
        return new SimpleStringProperty(Integer.toString(wood.getID()));
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
            returnString.add(Boolean.toString(effect.getStatus()));
        }
        return new SimpleStringProperty(returnString.toString());
    }
    /**
     * Returns the quantity used in the rocket.
     *
     * @return Returns the propellant quantity as StringProperty.
     */
    public final StringProperty getPropellantQuantityProperty() {
        return new SimpleStringProperty(propellantQuantity.toString());
    }
    /**
     * Returns the the result of the quality test.
     *
     * @return Returns the test result of the quality test as StringProperty.
     */
    public final StringProperty getTestResultProperty() {
        return new SimpleStringProperty(Boolean.toString(testResult));
    }
    /**
     * Returns the the id of the worker who built the rocket.
     *
     * @return Returns the worker id as StringProperty.
     */
    public final StringProperty getWorkerIdProperty() {

        return new SimpleStringProperty(workerId.toString());
    }
    /**
     * Returns the the id of the quality tester who tested the rocket.
     *
     * @return Returns the tester id as StringProperty.
     */
    public final StringProperty getTesterIdProperty() {

        return new SimpleStringProperty(testerId.toString());
    }
    /**
     * Returns the the id of the logistician who packed up the rocket.
     *
     * @return Returns the logistician id as StringProperty.
     */
    public final StringProperty getPackerIdProperty() {
        return new SimpleStringProperty(packerId.toString());
    }
    /**
     * Returns the the id of the supplier who delivered the used wood.
     *
     * @return Returns the supplier of the wood id as StringProperty.
     */
    public final StringProperty getSupplierWoodIdProperty() {
        return new SimpleStringProperty(Integer.toString(wood.getSupplierId()));
    }
    /**
     * Returns the the id of the supplier who delivered the used casing.
     *
     * @return Returns the supplier of the casing id as StringProperty.
     */
    public final StringProperty getSupplierCasingIdProperty() {
        return new SimpleStringProperty(Integer.toString(
                casing.getSupplierId()));
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
     * Set the result of quality test.
     *
     * @param result of the quality test as boolean
     */
    public final void setTestResult(final boolean result) {
        testResult = result;
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
     * @return bollean value of the test result
     */
    public final Boolean getTestResult() {
        return testResult;
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
     * Get the id of the worker who packed the rocket in logistics.
     *
     * @return packerId the id for the packer of the rocket
     */
    public final Integer getPackerId() {
        return packerId;
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
     * Set if the rocket is finished and ready to collect.
     *
     * @param readyForCollection flag which tells that the rocket is finished
     */
    public final void setReadyForCollection(final boolean readyForCollection) {
        this.readyForCollection = readyForCollection;
    }
    /**
     * Return the string representation of the rocket.
     *
     * @return A string containing the rocket
     */
    public final String toString() {
        return "Rocket Id: " + id;
    }

}

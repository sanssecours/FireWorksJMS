package org.falafel;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Stores the information of a package of rockets.
 */
public class RocketPackage implements Serializable {
    /** A list of all rockets contained in this package. */
    private ArrayList<Rocket> rockets;
    /** The identifier of this package. */
    private Integer id;

    /**
     * Create a new rocket with the given attributes.
     *
     * @param id
     *          The identifier for the rocket package
     * @param packedRockets
     *          A list of all rockets that should be saved into this package.
     */
    public RocketPackage(final Integer id,
                         final ArrayList<Rocket> packedRockets) {
        rockets = new ArrayList<>();
        for (Rocket rocket : packedRockets) {
            rocket.setPackageId(id);
            rockets.add(rocket);
        }
        this.id = id;
    }

    /**
     * Get all rockets contained in this package.
     *
     * @return A list of all rockets contained in this package.
     */
    public final ArrayList<Rocket> getRockets() {
        return rockets;
    }

    /**
     * Get the identifier for this rocket package.
     *
     * @return The id of this rocket package
     */
    public final Integer getId() {
        return id;
    }

    /**
     * Get the string representation for this rocket package.
     *
     * @return A string representing attributes of this rocket package.
     */
    @Override
    public final String toString() {
        return "RocketPackage{id = " + id + " rockets = " + rockets + '}';
    }
}

package org.falafel;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Stores the information of a package of rockets.
 */
public class RocketPackage implements Serializable {
    private ArrayList<Rocket> rockets;
    private Integer id;

    public RocketPackage (Integer id, ArrayList<Rocket> packedRockets) {
        rockets = new ArrayList<>();
        for(Rocket rocket : packedRockets) {
            rocket.setPackageId(id);
            rockets.add(rocket);
        }
        this.id = id;
    }

    public ArrayList<Rocket> getRockets() {
        return rockets;
    }

    public Integer getId() {
        return id;
    }

    @Override
    public String toString() {
        return "RocketPackage{id = " + id + " rockets = " + rockets + '}';
    }
}

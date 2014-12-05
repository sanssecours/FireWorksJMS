package org.falafel;

/** Different types of Material provided by Suppliers. */
public class QueueDestinations {
    public static final String ID_ROCKET_QUEUE =
            "jms/queue/ids/rockets";
    public static final String ID_PACKET_QUEUE =
            "jms/queue/ids/packets";

    public static final String STORAGE_CLOSED_PROP_QUEUE =
            "jms/queue/storage/propellants/closed";
    public static final String STORAGE_OPENED_PROP_QUEUE =
            "jms/queue/storage/propellants/opened";
    public static final String STORAGE_WOOD_QUEUE =
            "jms/queue/storage/wood";
    public static final String STORAGE_CASING_QUEUE =
            "jms/queue/storage/casings";
    public static final String STORAGE_EFFECT_QUEUE =
            "jms/queue/storage/effects";

    public static final String GUI_QUEUE =
            "jms/queue/gui";

    public static final String ROCKET_PRODUCED_QUEUE =
            "jms/queue/rockets/produced";

    public static final String ROCKET_TESTED_QUEUE =
            "jms/queue/rockets/tested";

    public static final String ROCKET_TRASHED_QUEUE =
            "jms/queue/rockets/trashed";

    public static final String ROCKET_SHIPPED_QUEUE =
            "jms/queue/rockets/shipped";
}

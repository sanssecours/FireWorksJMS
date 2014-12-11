package org.falafel;

/** Addresses for the different queues. */
public final class QueueDestinations {

    /** Create empty Queue destinations class. */
    private QueueDestinations() { }

    /** Queue for the rockets ids. */
    public static final String ID_ROCKET_QUEUE = "jms/queue/ids/rockets";
    /** Queue for the package ids. */
    public static final String ID_PACKET_QUEUE = "jms/queue/ids/packets";

    /** Queue for the closed propellants. */
    public static final String STORAGE_CLOSED_PROP_QUEUE =
            "jms/queue/storage/propellants/closed";
    /** Queue for the opened propellants. */
    public static final String STORAGE_OPENED_PROP_QUEUE =
            "jms/queue/storage/propellants/opened";
    /** Queue to store the ordered wood. */
    public static final String STORAGE_WOOD_QUEUE =
            "jms/queue/storage/wood";
    /** Queue to store the ordered casings. */
    public static final String STORAGE_CASING_QUEUE =
            "jms/queue/storage/casings";
    /** Queue to store the ordered effects. */
    public static final String STORAGE_EFFECT_QUEUE =
            "jms/queue/storage/effects";

    /** Queue for GUI updates. */
    public static final String GUI_QUEUE = "jms/queue/gui";

    /** Queue that stores the produced rockets. */
    public static final String ROCKET_PRODUCED_QUEUE =
            "jms/queue/rockets/produced";

    /** Queue that stores the tested rockets. */
    public static final String ROCKET_TESTED_QUEUE =
            "jms/queue/rockets/tested";

    /** Queue that stores the trashed rockets. */
    public static final String ROCKET_TRASHED_QUEUE =
            "jms/queue/rockets/trashed";

    /** Queue that stores the shipped rockets. */
    public static final String ROCKET_SHIPPED_QUEUE =
            "jms/queue/rockets/shipped";
}

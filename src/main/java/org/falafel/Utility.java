package org.falafel;

/**
 * This class provides various utility functions.
 */
public final class Utility {

    /** Create an empty Utility instance. */
    private Utility() { }

    /**
     * Let the current thread sleep for some time.
     *
     * @param sleepTime
     *          The amount of time that the current thread should sleep in ms.
     *
     */
    public static void sleep(final int sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            System.err.println("I was interrupted while trying to sleep. "
                    + "How rude!");
        }
    }
}

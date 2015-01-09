package org.falafel;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;
import static org.falafel.Utility.sleep;
import static org.falafel.Utility.CONNECTION_FACTORY;
import static org.falafel.Utility.USERNAME;
import static org.falafel.Utility.PASSWORD;
import static org.falafel.Utility.INITIAL_CONTEXT_FACTORY;
import static org.falafel.Utility.PROVIDER_URL;

/**
 * This class represents a worker. A worker collects materials and uses them
 * to create a rocket.
 */
public final class Worker {

    /** Constant for the lower bound of the working time per rocket. */
    private static final int LOWER_BOUND = 1000;
    /** Constant for the upper bound of the working time per rocket. */
    private static final int UPPER_BOUND = 2000;
    /** Constant for the lower bound of the propellant quantity. */
    private static final int LOWER_QUANTITY = 115;
    /** Constant for the upper bound of the propellant quantity. */
    private static final int UPPER_QUANTITY = 145;
    /** Constant for how long the shutdown hook is waiting. */
    private static final int WAIT_TIME_TO_SHUTDOWN = 5000;
    /** How many effect charges are needed to build a rocket. */
    private static final int NUMBER_EFFECTS_NEEDED = 3;
    /** Specifies how many ids are stored in the queue for ids at the program
     *  start. */
    private static final int NUMBER_INITIAL_IDS = 10;
    /** Specifies how long a worker waits until he tries to get new material
     *  after he failed the last time. */
    private static final int WAIT_TIME_WORKER_MS = 2000;
    /** The time used to wait for one message from a certain queue. */
    private static final int WAIT_TIME_MESSAGE_MS = 5000;
    /** Get the Logger for the current class. */
    private static final Logger LOGGER = getLogger(Worker.class.getName());

    /** The naming context used to lookup the JMS queues. */
    private static Context namingContext;

    /** Specifies if we want to terminate the program. This variable will be
     *  set to true after we press CTR-C. */
    private static boolean shutdown = false;


    /** Create the worker singleton. */
    private Worker() { }

    /**
     * Start the worker process.
     *
     * @param arguments
     *          A list containing the command line arguments.
     *
     */
    public static void main(final String[] arguments) {
        int workerId;
        Wood wood;
        Casing casing;
        long startTime = 0;
        long endTime = 0;
        ArrayList<Effect> effects = new ArrayList<>();
        HashMap<Propellant, Integer> propellantsWithQuantity;
        Integer rocketId;
        JMSCommunication communicator = new JMSCommunication();
        Random randomGenerator = new Random();

        if (arguments.length != 1) {
            System.err.println("Usage: worker <Id>!");
            return;
        }

        try {
            workerId = Integer.parseInt(arguments[0]);
        } catch (Exception e) {
            System.err.println("Please supply a valid id!");
            return;
        }

        // Setup communication
        ConnectionFactory connectionFactory;
        try {
            // Set up the namingContext for the JNDI lookup
            final Properties env = new Properties();
            env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
            env.put(Context.PROVIDER_URL, PROVIDER_URL);
            env.put(Context.SECURITY_PRINCIPAL, USERNAME);
            env.put(Context.SECURITY_CREDENTIALS, PASSWORD);
            namingContext = new InitialContext(env);

            connectionFactory = (ConnectionFactory)
                    namingContext.lookup(CONNECTION_FACTORY);
        } catch (NamingException e) {
            LOGGER.severe("Could not create properties");
            return;
        }
        Destination destinationWood;
        Destination destinationEffectBlue;
        Destination destinationEffectGreen;
        Destination destinationEffectRed;
        Destination destinationCasing;
        Destination destinationOpenedPropellant;
        Destination destinationClosedPropellant;
        Destination destinationRocketId;
        try {
            destinationWood = (Destination) namingContext.lookup(
                    QueueDestinations.STORAGE_WOOD_QUEUE);
            destinationEffectBlue = (Destination) namingContext.lookup(
                    QueueDestinations.STORAGE_BLUE_EFFECT_QUEUE);
            destinationEffectGreen = (Destination) namingContext.lookup(
                    QueueDestinations.STORAGE_GREEN_EFFECT_QUEUE);
            destinationEffectRed = (Destination) namingContext.lookup(
                    QueueDestinations.STORAGE_RED_EFFECT_QUEUE);
            destinationCasing = (Destination) namingContext.lookup(
                    QueueDestinations.STORAGE_CASING_QUEUE);
            destinationOpenedPropellant = (Destination) namingContext.lookup(
                    QueueDestinations.STORAGE_OPENED_PROP_QUEUE);
            destinationClosedPropellant = (Destination) namingContext.lookup(
                    QueueDestinations.STORAGE_CLOSED_PROP_QUEUE);
            destinationRocketId = (Destination) namingContext.lookup(
                    QueueDestinations.ID_ROCKET_QUEUE);
        } catch (NamingException e) {
            LOGGER.severe("Could not create queue destinations");
            return;
        }

        Worker.addShutdownHook();

        LOGGER.info("Worker " + workerId + " ready to work!");

        workerLoop:
        while (!shutdown) {
            int propellantQuantity;
            effects.clear();
            /* Get materials */
            try (JMSContext context = connectionFactory.createContext(
                    USERNAME, PASSWORD, JMSContext.SESSION_TRANSACTED)) {
                JMSConsumer consumerWood = context.createConsumer(
                        destinationWood);
                wood = consumerWood.receiveBodyNoWait(Wood.class);
                if (wood == null) {
                    context.rollback();
                    LOGGER.info("could not get wood");
                    Utility.sleep(WAIT_TIME_WORKER_MS);
                    continue;
                }

                JMSConsumer consumerCasing = context.createConsumer(
                        destinationCasing);
                casing = consumerCasing.receiveBodyNoWait(Casing.class);
                if (casing == null) {
                    context.rollback();
                    LOGGER.info("could not get casings");
                    Utility.sleep(WAIT_TIME_WORKER_MS);
                    continue;
                }

                ArrayList<EffectColor> randomColors = new ArrayList<>(
                        Arrays.asList(EffectColor.Blue, EffectColor.Green,
                                EffectColor.Red));
                JMSConsumer consumerEffect;
                while (randomColors.size() > 0 && effects.size()
                                                    < NUMBER_EFFECTS_NEEDED) {
                    int randomColor = randomGenerator.nextInt(
                            randomColors.size());
                    switch (randomColors.get(randomColor)) {
                        case Blue:
                            consumerEffect = context.createConsumer(
                                    destinationEffectBlue);
                            break;
                        case Green:
                            consumerEffect = context.createConsumer(
                                    destinationEffectGreen);
                            break;
                        case Red:
                            consumerEffect = context.createConsumer(
                                    destinationEffectRed);
                            break;
                        default:
                            consumerEffect = context.createConsumer(
                                destinationEffectBlue);
                    }

                    Effect effect = consumerEffect.receiveBodyNoWait(Effect.class);
                    if (effect != null) {
                        effects.add(effect);
                    } else {
                        randomColors.remove(randomColor);
                    }
                }

                if (effects.size() != NUMBER_EFFECTS_NEEDED) {
                    context.rollback();
                    LOGGER.info("could not get all effects. Got: " + effects);
                    Utility.sleep(WAIT_TIME_WORKER_MS);
                    continue;
                }

                startTime = System.currentTimeMillis();
                propellantQuantity = randomGenerator.nextInt(
                        UPPER_QUANTITY - LOWER_QUANTITY) + LOWER_QUANTITY;
                int quantity = 0;
                int missingQuantity = propellantQuantity;
                propellantsWithQuantity  = new HashMap<>();

                while (quantity < propellantQuantity) {
                    JMSConsumer consumerOpenedPropellant =
                            context.createConsumer(destinationOpenedPropellant);
                    Propellant propellant =
                            consumerOpenedPropellant.receiveBodyNoWait(
                                    Propellant.class);
                    if (propellant != null) {
                        int currentQuantity = propellant.getQuantity();
                        if (currentQuantity >= missingQuantity) {
                            // Done with rocket
                            quantity = quantity + missingQuantity;
                            currentQuantity = missingQuantity;
                        } else {
                            // We still need to open a new propellant
                            // after the current one
                            quantity = quantity + currentQuantity;
                            missingQuantity = missingQuantity
                                    - currentQuantity;
                        }
                        propellantsWithQuantity.put(propellant,
                                currentQuantity);
                    } else {
                        JMSConsumer consumerClosedPropellant =
                                context.createConsumer(
                                        destinationClosedPropellant);
                        Propellant closedPropellant =
                                consumerClosedPropellant.receiveBodyNoWait(
                                        Propellant.class);
                        if (closedPropellant != null) {
                            quantity = quantity + missingQuantity;
                            propellantsWithQuantity.put(closedPropellant,
                                    missingQuantity);
                        } else {
                            context.rollback();
                            LOGGER.info("Could not get all propellant. Got: "
                                    + propellantsWithQuantity);
                            Utility.sleep(WAIT_TIME_WORKER_MS);
                            continue workerLoop;
                        }
                    }
                }

                // get an id for the rocket from the queue
                JMSConsumer consumerRocketId = context.createConsumer(
                        destinationRocketId);
                rocketId = consumerRocketId.receiveBodyNoWait(Integer.class);
                if (rocketId == null) {
                    context.rollback();
                    LOGGER.severe("Could not get an rocket id!");
                    Utility.sleep(WAIT_TIME_WORKER_MS);
                    continue;
                }
                context.commit();

                endTime = System.currentTimeMillis();
            }

            // write a new rocket id in the queue for the taken one
            communicator.sendMessage(rocketId + NUMBER_INITIAL_IDS,
                    QueueDestinations.ID_ROCKET_QUEUE);

            //write to GUI what was taken
            casing.setInStorage(false);
            communicator.sendMessage(casing, QueueDestinations.GUI_QUEUE);
            wood.setInStorage(false);
            communicator.sendMessage(wood, QueueDestinations.GUI_QUEUE);
            for (Effect effect : effects) {
                effect.setInStorage(false);
                communicator.sendMessage(effect, QueueDestinations.GUI_QUEUE);
            }
            for (Propellant propellant : propellantsWithQuantity.keySet()) {
                propellant.setInStorage(false);
                communicator.sendMessage(propellant,
                        QueueDestinations.GUI_QUEUE);
            }

            long duration = (endTime - startTime);
            System.out.println("Time for collectiing resources: "
                    + duration);
            // The time needed to produce a rocket
            int waitingTime = randomGenerator.nextInt(
                    UPPER_BOUND - LOWER_BOUND) + LOWER_BOUND;
            sleep(waitingTime);

            // Worker produces rocket
            Rocket producedRocket = new Rocket(rocketId, wood, casing, effects,
                    propellantsWithQuantity, propellantQuantity ,
                    workerId);

            communicator.sendMessage(producedRocket,
                    QueueDestinations.GUI_QUEUE);
            communicator.sendMessage(producedRocket,
                    QueueDestinations.ROCKET_PRODUCED_QUEUE);

            // write the used propellant package back if it still contains
            // propellant
            for (Propellant propellant : propellantsWithQuantity.keySet()) {
                int originalQuantity = propellant.getQuantity();
                propellant.setQuantity(originalQuantity
                        - propellantsWithQuantity.get(propellant));
                if (propellant.getQuantity() > 0) {
                    propellant.setInStorage(true);
                    communicator.sendMessage(propellant,
                            QueueDestinations.GUI_QUEUE);
                    communicator.sendMessage(propellant,
                            QueueDestinations.STORAGE_OPENED_PROP_QUEUE);
                }
            }
        }
        communicator.closeCommunication();
    }

    /**
     * Add a shutdown hook.
     *
     * This method will be called when the worker is terminated.
     *
     */
    private static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("I'm packing my stuff together.");
                shutdown = true;
                Utility.sleep(WAIT_TIME_TO_SHUTDOWN);
                System.out.println("I'm going home.");
                try {
                    namingContext.close();
                } catch (NamingException e) {
                    LOGGER.severe("Could not close naming context");
                }
            }
        });
    }
}

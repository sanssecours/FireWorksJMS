package org.falafel;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;
import static org.falafel.Utility.CONNECTION_FACTORY;
import static org.falafel.Utility.USERNAME;
import static org.falafel.Utility.PASSWORD;
import static org.falafel.Utility.INITIAL_CONTEXT_FACTORY;
import static org.falafel.Utility.PROVIDER_URL;

/**
 * This class represents a worker. A worker collects materials and uses them
 * to create a rocket.
 */
public final class Worker implements MessageListener {

    /** Constant for the lower bound of the propellant quantity. */
    private static final int LOWER_QUANTITY = 115;
    /** Constant for the upper bound of the propellant quantity. */
    private static final int UPPER_QUANTITY = 145;
    /** Constant for how long the shutdown hook is waiting. */
    private static final int WAIT_TIME_TO_SHUTDOWN = 5000;
    /** Constant for how long the transaction is waiting. */
    private static final int WAIT_TIME_OUT = 20;
    /** Constant for how long the transaction is waiting. */
    private static final int WAIT_TIME_BENCHMARK = 500;
    /** How many effect charges are needed to build a rocket. */
    private static final int NUMBER_EFFECTS_NEEDED = 3;
    /** Specifies how many ids are stored in the queue for ids at the program
     *  start. */
    private static final int NUMBER_INITIAL_IDS = 10;
    /** Get the Logger for the current class. */
    private static final Logger LOGGER = getLogger(Worker.class.getName());

    /** The naming context used to lookup the JMS queues. */
    private static Context namingContext;

    /** Worker id. */
    private static int workerId;

    /** Specifies if we want to terminate the program. This variable will be
     *  set to true after we press CTR-C. */
    private static boolean shutdown = false;
    /** Start/Stop flag of the benchmark test. */
    private static boolean startTest = false;


    /** Create the worker singleton. */
    Worker() {
        initListeners(workerId);
    }

    /**
     * Start the worker process.
     *
     * @param arguments
     *          A list containing the command line arguments.
     *
     */
    public static void main(final String[] arguments) {
        Wood wood;
        Casing casing;
        ArrayList<Effect> effects = new ArrayList<>();
        HashMap<Propellant, Integer> propellantsWithQuantity;
        Integer rocketId;
        Purchase purchase;
        boolean gotPurchase;
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

        new Worker();

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
        Destination destinationPurchase;
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
            destinationPurchase = (Destination) namingContext.lookup(
                    QueueDestinations.PURCHASE_CURRENT_QUEUE);
        } catch (NamingException e) {
            LOGGER.severe("Could not create queue destinations");
            return;
        }

        Worker.addShutdownHook();

        LOGGER.info("Worker " + workerId + " ready to work!");

        while (!startTest) {
            Utility.sleep(WAIT_TIME_BENCHMARK);
        }

        workerLoop:
        while (!shutdown && startTest) {
            int propellantQuantity;
            gotPurchase = false;
            effects.clear();
            /* Get materials */
            try (JMSContext context = connectionFactory.createContext(
                    USERNAME, PASSWORD, JMSContext.SESSION_TRANSACTED)) {
                JMSConsumer consumerWood = context.createConsumer(
                        destinationWood);
                wood = consumerWood.receiveBody(Wood.class, WAIT_TIME_OUT);
                if (wood == null) {
                    context.rollback();
                    LOGGER.info("could not get wood");
                    continue;
                }

                JMSConsumer consumerCasing = context.createConsumer(
                        destinationCasing);
                casing = consumerCasing.receiveBody(Casing.class,
                        WAIT_TIME_OUT);
                if (casing == null) {
                    context.rollback();
                    LOGGER.info("could not get casings");
                    continue;
                }
                ArrayList<EffectColor> randomColors = new ArrayList<>(
                        Arrays.asList(EffectColor.Blue, EffectColor.Green,
                                EffectColor.Red));
                JMSConsumer consumerBlue = context.createConsumer(
                        destinationEffectBlue);
                JMSConsumer consumerGreen = context.createConsumer(
                        destinationEffectGreen);
                JMSConsumer consumerRed = context.createConsumer(
                        destinationEffectRed);

                JMSConsumer consumerPurchase = context.createConsumer(
                        destinationPurchase);
                purchase = consumerPurchase.receiveBody(Purchase.class,
                        WAIT_TIME_OUT);

                if (purchase != null) {
                    gotPurchase = true;
                    Collection<EffectColor> colors =
                            purchase.getEffectColors();
                    for (EffectColor color : colors) {
                        Effect effect;
                        switch (color) {
                            case Blue:
                                effect = consumerBlue.receiveBody(Effect.class,
                                        WAIT_TIME_OUT);
                                break;
                            case Green:
                                effect = consumerGreen.receiveBody(
                                        Effect.class, WAIT_TIME_OUT);
                                break;
                            case Red:
                                effect = consumerRed.receiveBody(Effect.class,
                                        WAIT_TIME_OUT);
                                break;
                            default:
                                LOGGER.severe("Worker: wrong effect color in"
                                        + "purchase");
                                return;
                        }
                        if (effect != null) {
                            effects.add(effect);
                        } else {
                            gotPurchase = false;
                            break;
                        }
                    }
                }

                while (randomColors.size() > 0 && effects.size()
                                                    < NUMBER_EFFECTS_NEEDED) {
                    Effect effect;
                    gotPurchase = false;
                    int randomColor = randomGenerator.nextInt(
                            randomColors.size());
                    switch (randomColors.get(randomColor)) {
                        case Blue:
                            effect = consumerBlue.receiveBody(Effect.class,
                                    WAIT_TIME_OUT);
                            break;
                        case Green:
                            effect = consumerGreen.receiveBody(Effect.class,
                                    WAIT_TIME_OUT);
                            break;
                        case Red:
                            effect = consumerRed.receiveBody(Effect.class,
                                    WAIT_TIME_OUT);
                            break;
                        default:
                            LOGGER.severe("Worker: wrong effect color!");
                            return;
                    }

                    if (effect != null) {
                        effects.add(effect);
                    } else {
                        randomColors.remove(randomColor);
                    }
                }

                if (effects.size() != NUMBER_EFFECTS_NEEDED) {
                    context.rollback();
                    LOGGER.info("could not get all effects. Got: " + effects);
                    continue;
                }

                propellantQuantity = randomGenerator.nextInt(
                        UPPER_QUANTITY - LOWER_QUANTITY) + LOWER_QUANTITY;
                int quantity = 0;
                int missingQuantity = propellantQuantity;
                propellantsWithQuantity  = new HashMap<>();

                while (quantity < propellantQuantity) {
                    JMSConsumer consumerOpenedPropellant =
                            context.createConsumer(destinationOpenedPropellant);
                    Propellant propellant =
                            consumerOpenedPropellant.receiveBody(
                                    Propellant.class, WAIT_TIME_OUT);
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
                                consumerClosedPropellant.receiveBody(
                                        Propellant.class, WAIT_TIME_OUT);
                        if (closedPropellant != null) {
                            quantity = quantity + missingQuantity;
                            propellantsWithQuantity.put(closedPropellant,
                                    missingQuantity);
                        } else {
                            context.rollback();
                            LOGGER.info("Could not get all propellant. Got: "
                                    + propellantsWithQuantity);
                            continue workerLoop;
                        }
                    }
                }

                // get an id for the rocket from the queue
                JMSConsumer consumerRocketId = context.createConsumer(
                        destinationRocketId);
                rocketId = consumerRocketId.receiveBody(Integer.class,
                        WAIT_TIME_OUT);
                if (rocketId == null) {
                    context.rollback();
                    LOGGER.severe("Could not get an rocket id!");
                    continue;
                }
                context.commit();

            }

            // write a new rocket id in the queue for the taken one
            communicator.sendMessage(rocketId + NUMBER_INITIAL_IDS,
                    QueueDestinations.ID_ROCKET_QUEUE);

            if (!gotPurchase) {
                communicator.sendMessage(purchase,
                        QueueDestinations.PURCHASE_CURRENT_QUEUE);
            }

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

            Rocket producedRocket;
            // Worker produces rocket
            if (gotPurchase) {
                producedRocket = new Rocket(rocketId, wood, casing,
                        effects, propellantsWithQuantity, propellantQuantity ,
                        workerId, purchase);
            } else {
                producedRocket = new Rocket(rocketId, wood, casing,
                        effects, propellantsWithQuantity, propellantQuantity ,
                        workerId);
            }

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

    /**
     * Set the listener for the benchmark.
     *
     * @param workerId
     *          sets to which queue the worker should listen.
     */
    private void initListeners(final int workerId) {
        // Set up the namingContext for the JNDI lookup
        final Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
        env.put(Context.PROVIDER_URL, PROVIDER_URL);
        env.put(Context.SECURITY_PRINCIPAL, USERNAME);
        env.put(Context.SECURITY_CREDENTIALS, PASSWORD);
        Context namingContext;


        String benchmarkQueue;
        //CHECKSTYLE:OFF
        if (workerId == 1001) {
            benchmarkQueue = QueueDestinations.BENCHMARK_WORKER_1001_QUEUE;
        } else {
            benchmarkQueue = QueueDestinations.BENCHMARK_WORKER_1002_QUEUE;
        }
        //CHECKSTYLE:ON

        try {
            namingContext = new InitialContext(env);

            ConnectionFactory connectionFactory = (ConnectionFactory)
                    namingContext.lookup(CONNECTION_FACTORY);

            Destination destination = (Destination) namingContext.lookup(
                    benchmarkQueue);

            JMSConsumer consumer = connectionFactory.createContext(
                    USERNAME, PASSWORD).createConsumer(
                    destination);

            consumer.setMessageListener(this);
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Listener to start/stop the benchmark test.
     *
     * @param message
     *          Signal to start/stop the test.
     */
    @Override
    public void onMessage(final Message message) {
        startTest = !startTest;

        if (startTest) {
            LOGGER.severe("Worker " + workerId + ": Starts the Benchmark");
        } else {
            LOGGER.severe("Worker " + workerId + ": Stops the Benchmark");
        }
    }
}

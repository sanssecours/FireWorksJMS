package org.falafel;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;
import static org.falafel.Utility.sleep;

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
    /** Specifies how long a worker waits until he tries to get new material
     *  after he failed the last time. */
    private static final int WAIT_TIME_WORKER_MS = 2000;
    /** Get the Logger for the current class. */
    private static final Logger LOGGER = getLogger(Worker.class.getName());

    private static final String CONNECTION_FACTORY =
            "jms/RemoteConnectionFactory";
    private static final String USERNAME = "fireworks";
    private static final String PASSWORD = "fireworks";
    private static final String INITIAL_CONTEXT_FACTORY =
            "org.jboss.naming.remote.client.InitialContextFactory";
    private static final String PROVIDER_URL =
            "http-remoting://127.0.0.1:8080";
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
        Wood wood = null;
        Casing casing = null;
        ArrayList<Effect> effects = new ArrayList<>();
        HashMap<Propellant, Integer> propellantsWithQuantity =
                new HashMap<>();
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
        Destination destinationEffect;
        Destination destinationCasing;
        Destination destinationOpenedPropellant;
        Destination destinationClosedPropellant;
        try {
            destinationWood = (Destination) namingContext.lookup(
                    QueueDestinations.STORAGE_WOOD_QUEUE);
            destinationEffect = (Destination) namingContext.lookup(
                    QueueDestinations.STORAGE_EFFECT_QUEUE);
            destinationCasing = (Destination) namingContext.lookup(
                    QueueDestinations.STORAGE_CASING_QUEUE);
            destinationOpenedPropellant = (Destination) namingContext.lookup(
                    QueueDestinations.STORAGE_OPENED_PROP_QUEUE);
            destinationClosedPropellant = (Destination) namingContext.lookup(
                    QueueDestinations.STORAGE_CLOSED_PROP_QUEUE);
        } catch (NamingException e) {
            LOGGER.severe("Could not create queue destinations");
            return;
        }

        Worker.addShutdownHook();

        LOGGER.info("Worker " + workerId + " ready to work!");

        workerLoop:
        while (!shutdown) {
            int propellantQuantity = 0;
            effects.clear();
            /* Get materials */
            try (JMSContext context = connectionFactory.createContext(
                    USERNAME, PASSWORD, JMSContext.SESSION_TRANSACTED)) {
                JMSConsumer consumerWood = context.createConsumer(
                        destinationWood);
                wood = consumerWood.receiveBodyNoWait(Wood.class);
                if (wood == null) {
                    context.rollback();
                    LOGGER.severe("could not get wood");
                    Utility.sleep(WAIT_TIME_WORKER_MS);
                    continue;
                }
                System.out.println("Got: " + wood);

                JMSConsumer consumerCasing = context.createConsumer(
                        destinationCasing);
                casing = consumerCasing.receiveBodyNoWait(Casing.class);
                if (casing == null) {
                    context.rollback();
                    LOGGER.severe("could not get casings");
                    Utility.sleep(WAIT_TIME_WORKER_MS);
                    continue;
                }
                System.out.println("Got: " + casing);

                JMSConsumer consumerEffect = context.createConsumer(
                        destinationEffect);
                for (int index = 0; index < NUMBER_EFFECTS_NEEDED; index++) {
                    Effect effect = consumerEffect.receiveBodyNoWait(
                            Effect.class);
                    if (effect != null) {
                        effects.add(effect);
                    }
                }
                if (effects.size() != NUMBER_EFFECTS_NEEDED) {
                    context.rollback();
                    LOGGER.severe("could not get effect got: " + effects);
                    Utility.sleep(WAIT_TIME_WORKER_MS);
                    continue;
                }
                System.out.println("Got: " + effects);

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
                    if(propellant != null) {
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
                            LOGGER.severe("could not get propellant got: "
                                    + propellantsWithQuantity);
                            Utility.sleep(WAIT_TIME_WORKER_MS);
                            continue workerLoop;
                        }
                    }
                }
                context.commit();
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
            // The time needed to produce a rocket
            int waitingTime = randomGenerator.nextInt(
                    UPPER_BOUND - LOWER_BOUND) + LOWER_BOUND;
            sleep(waitingTime);

            // get an id and write a new on in the queue
            Integer rocketId;
            do {
                rocketId = (Integer) communicator.receiveMessage(
                        QueueDestinations.ID_ROCKET_QUEUE);
            } while(rocketId == null);
            communicator.sendMessage(rocketId + 10,
                    QueueDestinations.ID_ROCKET_QUEUE);

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

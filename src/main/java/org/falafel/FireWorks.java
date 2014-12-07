/**
 * This code is a simplified version of the one found in
 * HelloWorldJMSClient.java at https://github.com/wildfly/quickstart. It is
 * therefore licensed under the Apache License, Version 2.0 (the "License");
 */
package org.falafel;

import java.util.*;
import java.util.logging.Logger;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

import static org.falafel.MaterialType.Casing;
import static org.falafel.MaterialType.Effect;
import static org.falafel.MaterialType.Propellant;
import static org.falafel.MaterialType.Wood;

public class FireWorks extends Application implements MessageListener {
    private static final Logger LOGGER =
            Logger.getLogger(FireWorks.class.getName());

    private static final String CONNECTION_FACTORY =
            "jms/RemoteConnectionFactory";
    private static final String USERNAME = "fireworks";
    private static final String PASSWORD = "fireworks";
    private static final String INITIAL_CONTEXT_FACTORY =
            "org.jboss.naming.remote.client.InitialContextFactory";
    private static final String PROVIDER_URL =
            "http-remoting://127.0.0.1:8080";

    /** The running id for the suppliers. */
    private static int supplierId = 1;
    /** The running id for the materials. */
    private static int materialId = 1;

    /**  The data as an observable list for SupplyOrder. */
    private static ObservableList<SupplyOrder> order =
            FXCollections.observableArrayList();

    /**  The data as an observable list for rockets. */
    private static ObservableList<Rocket> rockets =
            FXCollections.observableArrayList();
    /**  The data as an observable list for the trashed rockets. */
    private static ObservableList<Rocket> trashedRocketsList =
            FXCollections.observableArrayList();
    /** The data as an observable list for the rockets which are already packed.
     * */
    private static ObservableList<Rocket> packedRocketsList =
            FXCollections.observableArrayList();

    /** Specify the different choices a supplier can provide. */
    private static final ObservableList<String> TYPES_CHOICE_LIST =
            FXCollections.observableArrayList(Casing.toString(),
                    Effect.toString(),
                    Propellant.toString(),
                    Wood.toString());

    /** Save data shown in the rocket table. */
    @FXML
    private TableColumn<Rocket, String> effectIdColumn, propellantIdColumn,
            testResultColumn,  supplierPropellantIdColumn,
            supplierEffectIdColumn;
    /** Save data shown in the rocket table. */
    @FXML
    private TableColumn<Rocket, Number> rocketIdColumn, casingIdColumn,
            packageIdColumn, woodIdColumn, propellantQuantityColumn,
            workerIdColumn, testerIdColumn, supplierWoodIdColumn,
            supplierCasingIdColumn, packerIdColumn;

    /** Save handler of the rocket table. */
    @FXML
    private TableView<Rocket> rocketTable = new TableView<>();


    /** Saves data shown in the supplier table. */
    @FXML
    private TableView<SupplyOrder> supplyTable;
    /** Saves data shown in the name column of the supplier table. */
    @FXML
    private TableColumn<SupplyOrder, String> supplierNameColumn;
    /** Saves data shown in the type column of the supplier table. */
    @FXML
    private TableColumn<SupplyOrder, String> orderedTypeColumn;
    /** Saves data shown in the quantity column of the supplier table. */
    @FXML
    private TableColumn<SupplyOrder, String> orderedQuantityColumn;
    /** Saves data shown in the quality column of the supplier table. */
    @FXML
    private TableColumn<SupplyOrder, String> orderedQualityColumn;
    /** Label for the current number of elements in the effect container. */
    @FXML
    private Label effectCounterLabel;
    /** Saves data shown in the effectCounterLabel. */
    private static Integer effectCounter = 0;
    /** Saves data shown in the effectCounterLabel. */
    private static StringProperty effectCounterProperty =
            new SimpleStringProperty(effectCounter.toString());
    /** Label for the current number of elements in the casing container. */
    @FXML
    private Label casingsCounterLabel;
    /** Saves data shown in the casingsCounterLabel. */
    private static Integer casingsCounter = 0;
    /** Saves data shown in the casingsCounterLabel. */
    private static StringProperty casingsCounterProperty =
            new SimpleStringProperty(casingsCounter.toString());
    /** Label for the current number of elements in the wood container. */
    @FXML
    private Label woodCounterLabel;
    /** Saves data shown in the woodCounterLabel. */
    private static Integer woodCounter = 0;
    /** Saves data shown in the woodCounterLabel. */
    private static StringProperty woodCounterProperty =
            new SimpleStringProperty(woodCounter.toString());
    /** Label for the current number of elements in the propellant container. */
    @FXML
    private Label propellantCounterLabel;
    /** Saves data shown in the propellantCounterLabel. */
    private static Integer propellantCounter = 0;
    /** Saves data shown in the propellantCounterLabel. */
    private static StringProperty propellantCounterProperty =
            new SimpleStringProperty(propellantCounter.toString());

    /** Label for the current number of open propellant charges in the
     * container. */
    @FXML
    private Label numberOpenPropellantLabel;
    /** Saves data shown in the numberOpenPropellant. */
    private static Integer numberOpenPropellantCounter = 0;
    /** Saves data shown in the numberOpenPropellant. */
    private static StringProperty numberOpenPropellantCounterProperty =
            new SimpleStringProperty(numberOpenPropellantCounter.toString());
    /** Label for the current quantity in grams of open propellant charges in
     * the container. */
    @FXML
    private Label quantityOpenPropellantLabel;
    /** Saves data shown in the quantityOpenPropellant. */
    private static Integer quantityOpenPropellantCounter = 0;
    /** Saves data shown in the quantityOpenPropellant. */
    private static StringProperty quantityOpenPropellantCounterProperty =
            new SimpleStringProperty(quantityOpenPropellantCounter.toString());

    @FXML
    private Label numberRocketsLabel;
    private static StringProperty numberRocketsProperty =
            new SimpleStringProperty("0");
    private static StringProperty numberShippedRocketsProperty =
            new SimpleStringProperty("0");
    private static StringProperty numberTrashedRocketsProperty =
            new SimpleStringProperty("0");
    private JMSConsumer consumer;


    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        // initialize rocket table
        rocketIdColumn.setCellValueFactory(
                cellData -> cellData.getValue().getIdProperty());
        packageIdColumn.setCellValueFactory(
                cellData -> cellData.getValue().getPackageIdProperty());
        casingIdColumn.setCellValueFactory(
                cellData -> cellData.getValue().getCasingIdProperty());
        propellantIdColumn.setCellValueFactory(
                cellData
                        -> cellData.getValue().getPropellantPackageIdProperty());
        woodIdColumn.setCellValueFactory(
                cellData -> cellData.getValue().getWoodIdProperty());
        effectIdColumn.setCellValueFactory(
                cellData -> cellData.getValue().getEffectIdProperty());
        propellantQuantityColumn.setCellValueFactory(
                cellData
                        -> cellData.getValue().getPropellantQuantityProperty());
        testResultColumn.setCellValueFactory(
                cellData -> cellData.getValue().getTestResultProperty());
        workerIdColumn.setCellValueFactory(
                cellData -> cellData.getValue().getWorkerIdProperty());
        testerIdColumn.setCellValueFactory(
                cellData -> cellData.getValue().getTesterIdProperty());
        packerIdColumn.setCellValueFactory(
                cellData -> cellData.getValue().getPackerIdProperty());
        supplierWoodIdColumn.setCellValueFactory(
                cellData -> cellData.getValue().getSupplierWoodIdProperty());
        supplierCasingIdColumn.setCellValueFactory(
                cellData -> cellData.getValue().getSupplierCasingIdProperty());
        supplierPropellantIdColumn.setCellValueFactory(
                cellData
                        -> cellData.getValue().getSupplierPropellantIdProperty());
        supplierEffectIdColumn.setCellValueFactory(
                cellData -> cellData.getValue().getSupplierEffectIdProperty());

        // initialize current warehouse labels
        casingsCounterLabel.textProperty().bind(casingsCounterProperty);
        effectCounterLabel.textProperty().bind(effectCounterProperty);
        propellantCounterLabel.textProperty().bind(propellantCounterProperty);
        woodCounterLabel.textProperty().bind(woodCounterProperty);
        numberOpenPropellantLabel.textProperty().bind(
                numberOpenPropellantCounterProperty);
        quantityOpenPropellantLabel.textProperty().bind(
                quantityOpenPropellantCounterProperty);
        numberRocketsLabel.textProperty().bind(numberRocketsProperty);

        //  initialize supplier table
        supplierNameColumn.setCellValueFactory(
                cellData -> cellData.getValue().supplierNameProperty());
        supplierNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        supplierNameColumn.isEditable();

        orderedTypeColumn.setCellValueFactory(
                cellData -> cellData.getValue().typeProperty());
        orderedTypeColumn.setCellFactory(
                ComboBoxTableCell.forTableColumn(TYPES_CHOICE_LIST));
        orderedTypeColumn.isEditable();

        orderedQuantityColumn.setCellValueFactory(
                cellData -> cellData.getValue().quantityProperty());
        orderedQuantityColumn.setCellFactory(
                TextFieldTableCell.forTableColumn());
        orderedQuantityColumn.isEditable();

        orderedQualityColumn.setCellValueFactory(
                cellData -> cellData.getValue().qualityProperty());
        orderedQualityColumn.setCellFactory(
                TextFieldTableCell.forTableColumn());
        orderedQualityColumn.isEditable();

        //CHECKSTYLE:OFF
        /*
        HashMap<Propellant, Integer> propellants = new HashMap<>();
        propellants.put(new Propellant(4, "hugo", 1, org.falafel.Propellant.CLOSED), 100);
        propellants.put(new Propellant(5, "hugo", 3, org.falafel.Propellant.CLOSED), 130);
        ArrayList<Effect> effects = new ArrayList<>();
        effects.add(new Effect(3, "Hannes", 3, false));
        rockets.add(new Rocket(1, new Wood(1, "hugo", 100), new Casing(2, "Rene", 2),
                effects, propellants, 130, 434));
        */
        order.add(new SupplyOrder("Hulk", Casing.toString(), 50, 100));
        order.add(new SupplyOrder("Iron Man", Wood.toString(), 50, 100));
        order.add(new SupplyOrder("Captain America", Effect.toString(), 50, 100));
        order.add(new SupplyOrder("Batman", Effect.toString(), 50, 80));
        order.add(new SupplyOrder("Thor", Effect.toString(), 50, 60));
        order.add(new SupplyOrder("Seaman", Propellant.toString(), 20, 100));
        order.add(new SupplyOrder("Hawk", Propellant.toString(), 30, 100));
        //CHECKSTYLE:ON

        supplyTable.isEditable();
        supplyTable.setItems(order);

        rocketTable.setItems(rockets);
        numberRocketsProperty.set(Integer.toString(rockets.size()));
    }

    /**
     * Start suppliers to fill the containers with Material.
     *
     * @param event
     *          The action event sent by JavaFx when the user interface
     *          element for this method is invoked.
     *
     */
    @FXML
    private void startSuppliers(final ActionEvent event) {
        SupplyOrder nextOrder;
        Supplier supplier;

        while (!order.isEmpty()) {
            nextOrder = order.remove(0);
            LOGGER.info(nextOrder.toString());
            supplier = new Supplier(supplierId, nextOrder, materialId);
            supplier.start();
            supplierId++;
            materialId = materialId + nextOrder.getQuantity();
        }

        System.out.println("No new order!");
    }

    /**
     * Updates the casing/effect/wood/closed propellant counters in the GUI.
     *
     * @param material
     *          Material of which the counter should be updated
     */
    public static void changeCounterLabels(Material material) {
        Platform.runLater(() -> {
            int difference;
            if (material.getInStorage()) {
                difference = 1;
            } else {
                difference = -1;
            }
            if (material instanceof Casing) {
                casingsCounter = casingsCounter + difference;
                casingsCounterProperty.set(casingsCounter.toString());
            }
            if (material instanceof Effect) {
                effectCounter = effectCounter + difference;
                effectCounterProperty.set(effectCounter.toString());
            }
            if (material instanceof Propellant) {
                Propellant propellant = (Propellant) material;
                if (propellant.getQuantity() == 500) {
                    propellantCounter = propellantCounter + difference;
                    propellantCounterProperty.set(
                            propellantCounter.toString());
                } else {
                    changeOpenedPropellantLabels(propellant);
                }
            }
            if (material instanceof Wood) {
                woodCounter = woodCounter + difference;
                woodCounterProperty.set(woodCounter.toString());
            }
        });
    }

    /**
     * Updates the opened propellant counters in the GUI.
     *
     * @param propellant
     *          the opened propellant which has changed
     */
    private static void changeOpenedPropellantLabels(Propellant propellant) {
        Platform.runLater(() -> {
            if (propellant.getInStorage()) {
                numberOpenPropellantCounter = numberOpenPropellantCounter + 1;
                quantityOpenPropellantCounter = quantityOpenPropellantCounter
                        + propellant.getQuantity();
            } else {
                numberOpenPropellantCounter = numberOpenPropellantCounter - 1;
                quantityOpenPropellantCounter = quantityOpenPropellantCounter
                        - propellant.getQuantity();
            }
            numberOpenPropellantCounterProperty.set(
                    numberOpenPropellantCounter.toString());
            quantityOpenPropellantCounterProperty.set(
                    quantityOpenPropellantCounter.toString());
        });
    }

    /**
     * Updates the rocket in the shipped/trashed/rocket table.
     *
     * @param updatedRocket which has been tested
     */
    public static void updateOfARocketInRocketsTable(
            final Rocket updatedRocket) {
        Platform.runLater(() -> {
            boolean rocketInTable = false;
            for (int index = 0; index < rockets.size(); index++) {
                Rocket rocket = rockets.get(index);
                int id = rocket.getRocketId();
                int newId = updatedRocket.getRocketId();
                if (id == newId) {
                    rockets.set(index, updatedRocket);
                    rocketInTable = true;
                    break;
                }
            }
            if (!rocketInTable) {
                rockets.add(updatedRocket);
                numberRocketsProperty.set(Integer.toString(rockets.size()));
            }
            if (updatedRocket.getPackerId() != 0) {
                if (updatedRocket.getTestResult()) {
                    trashedRocketsList.add(updatedRocket);
                    numberTrashedRocketsProperty.set(Integer.toString(
                            trashedRocketsList.size()));
                } else {
                    packedRocketsList.add(updatedRocket);
                    numberShippedRocketsProperty.set(Integer.toString(
                            packedRocketsList.size()));
                }
            }
        });
    }

    /**
     * This method will be invoked when we create a new order.
     *
     * @param actionEvent
     *          The action event sent by JavaFx when the user interface
     *          element for this method is invoked.
     */
    @SuppressWarnings("unused")
    public final void newOrder(final ActionEvent actionEvent) {
        order.add(new SupplyOrder());
    }

    /**
     * This method will be called when the shipped tab is pressed.
     *
     * @param event
     *          The event sent by JavaFx when the user interface
     *          element for this method is invoked.
     */
    public final void displayShippedRocketsTab(final Event event) {
        rocketTable.setItems(packedRocketsList);
        numberRocketsLabel.textProperty().bind(numberShippedRocketsProperty);
    }

    /**
     * This method will be called when the trashed tab is pressed.
     *
     * @param event
     *          The event sent by JavaFx when the user interface
     *          element for this method is invoked.
     */
    public final void displayTrashedRocketsTab(final Event event) {
        rocketTable.setItems(trashedRocketsList);
        numberRocketsLabel.textProperty().bind(numberTrashedRocketsProperty);
    }

    /**
     * This method will be called when the produced tab is pressed.
     *
     * @param event
     *          The event sent by JavaFx when the user interface
     *          element for this method is invoked.
     */
    public final void displayProducedRocketsTab(final Event event) {
        rocketTable.setItems(rockets);
        numberRocketsLabel.textProperty().bind(numberRocketsProperty);
    }

    /**
     * This method will be invoked when a new supplier name is set.
     *
     * @param stCellEditEvent
     *          The cell edit event sent by JavaFx when the user interface
     *          element for this method is invoked.
     */
    public final void setSupplierName(
            final TableColumn.CellEditEvent<SupplyOrder, String>
                    stCellEditEvent) {
        stCellEditEvent.getTableView().getItems().get(
                stCellEditEvent.getTablePosition().getRow()).setSupplierName(
                stCellEditEvent.getNewValue());
    }

    /**
     * This method will be invoked when a new type for the material is set.
     *
     * @param stCellEditEvent
     *          The cell edit event sent by JavaFx when the user interface
     *          element for this method is invoked.
     */
    public final void setType(
            final TableColumn.CellEditEvent<SupplyOrder, String>
                    stCellEditEvent) {
        stCellEditEvent.getTableView().getItems().get(
                stCellEditEvent.getTablePosition().getRow()).setType(
                stCellEditEvent.getNewValue());
    }

    /**
     * This method will be invoked when a new quantity for a material is set.
     *
     * @param stCellEditEvent
     *          The cell edit event sent by JavaFx when the user interface
     *          element for this method is invoked.
     */
    public final void setQuantity(
            final TableColumn.CellEditEvent<SupplyOrder, String>
                    stCellEditEvent) {
        SupplyOrder newValue = stCellEditEvent.getTableView().getItems().get(
                stCellEditEvent.getTablePosition().getRow());
        try {
            newValue.setQuantity(Integer.parseInt(
                    stCellEditEvent.getNewValue()));
        } catch (NumberFormatException e) {
            System.out.println("Not a number!");
        }
    }
    /**
     * This method will be invoked when the quality of a material is changed.
     *
     * @param stCellEditEvent
     *          The cell edit event sent by JavaFx when the user interface
     *          element for this method is invoked.
     */
    public final void setQuality(
            final TableColumn.CellEditEvent<SupplyOrder, String>
                    stCellEditEvent) {
        SupplyOrder newValue = stCellEditEvent.getTableView().getItems().get(
                stCellEditEvent.getTablePosition().getRow());
        try {
            newValue.setQuality(Integer.parseInt(
                    stCellEditEvent.getNewValue()));
        } catch (NumberFormatException e) {
            System.out.println("Not a number!");
        }

    }

    /**
     * Close the program.
     */
    private void close() {
        //consumer.close();
        System.out.println("Goodbye!");
    }

    public static void main(String[] arguments) {
        launch(arguments);
    }

    private void initListeners() {
        System.out.println("Set listeners to queues");
        try {
            // Set up the namingContext for the JNDI lookup
            final Properties env = new Properties();
            env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
            env.put(Context.PROVIDER_URL, PROVIDER_URL);
            env.put(Context.SECURITY_PRINCIPAL, USERNAME);
            env.put(Context.SECURITY_CREDENTIALS, PASSWORD);
            Context namingContext = new InitialContext(env);

            ConnectionFactory connectionFactory = (ConnectionFactory)
                    namingContext.lookup(CONNECTION_FACTORY);

            Destination destination = (Destination) namingContext.lookup(
                    QueueDestinations.GUI_QUEUE);

            consumer =
                    connectionFactory.createContext(
                            USERNAME, PASSWORD).createConsumer(
                            destination);

            consumer.setMessageListener(this);

            JMSCommunication communicator = new JMSCommunication();
            // read the content of the storage queues
            for(Object object : communicator.readMessagesInQueue(
                    QueueDestinations.STORAGE_CASING_QUEUE)) {
                changeCounterLabels((Casing) object);
            }
            for(Object object : communicator.readMessagesInQueue(
                    QueueDestinations.STORAGE_EFFECT_QUEUE)) {
                changeCounterLabels((Effect) object);
            }
            for(Object object : communicator.readMessagesInQueue(
                    QueueDestinations.STORAGE_CLOSED_PROP_QUEUE)) {
                changeCounterLabels((Propellant) object);
            }
            for(Object object : communicator.readMessagesInQueue(
                    QueueDestinations.STORAGE_OPENED_PROP_QUEUE)) {
                changeCounterLabels((Propellant) object);
            }
            for(Object object : communicator.readMessagesInQueue(
                    QueueDestinations.STORAGE_WOOD_QUEUE)) {
                changeCounterLabels((Wood) object);
            }

            // read the content of the rocket queues
            for(Object object : communicator.readMessagesInQueue(
                    QueueDestinations.ROCKET_PRODUCED_QUEUE)) {
                updateOfARocketInRocketsTable((Rocket) object);
            }
            for(Object object : communicator.readMessagesInQueue(
                    QueueDestinations.ROCKET_TESTED_QUEUE)) {
                updateOfARocketInRocketsTable((Rocket) object);
            }
            for(Object object : communicator.readMessagesInQueue(
                    QueueDestinations.ROCKET_SHIPPED_QUEUE)) {
                updateOfARocketInRocketsTable((Rocket) object);
            }
            for(Object object : communicator.readMessagesInQueue(
                    QueueDestinations.ROCKET_TRASHED_QUEUE)) {
                updateOfARocketInRocketsTable((Rocket) object);
            }

            // Check with ids are in the queues and fill them so that each
            // contains 10 ids
            TreeSet<Integer> ids = new TreeSet<>();
            for(Object object : communicator.readMessagesInQueue(
                    QueueDestinations.ID_ROCKET_QUEUE)) {
                ids.add((Integer) object);
            }
            int first;
            int last;
            try {
                first = ids.first();
                last = ids.last();
            } catch (NoSuchElementException e) {
                LOGGER.info("Rocket ids queue empty.");
                first = 1;
                last = 0;
            }
            while ((last-first) < 9 ) {
                last++;
                communicator.sendMessage(last,
                        QueueDestinations.ID_ROCKET_QUEUE);
            }
            for(Object object : communicator.readMessagesInQueue(
                    QueueDestinations.ID_PACKET_QUEUE)) {
                ids.add((Integer) object);
            }
            try {
                first = ids.first();
                last = ids.last();
            } catch (NoSuchElementException e) {
                LOGGER.info("Packet ids queue empty.");
                first = 1;
                last = 0;
            }
            while ((last-first) < 9 ) {
                last++;
                communicator.sendMessage(last,
                        QueueDestinations.ID_PACKET_QUEUE);
            }
        } catch (NamingException e) {
            LOGGER.severe("Could not create properties");
        }

    }

    @Override
    public final void start(final Stage primaryStage) throws Exception {
        initListeners();
        Parent root = FXMLLoader.load(
                getClass().getResource("/FireWorks.fxml"));

        primaryStage.setTitle("Fireworks Factory");
        primaryStage.setOnCloseRequest(event -> close());
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (((ObjectMessage) message).getObject() instanceof Wood) {
                changeCounterLabels(
                        (Wood) ((ObjectMessage) message).getObject());
            } else if (((ObjectMessage) message).getObject()
                    instanceof Effect) {
                changeCounterLabels(
                        (Effect) ((ObjectMessage) message).getObject());
            } else if (((ObjectMessage) message).getObject()
                    instanceof Propellant) {
                Propellant propellant = (Propellant)
                        ((ObjectMessage) message).getObject();
                changeCounterLabels(
                        (Propellant) ((ObjectMessage) message).getObject());
            } else if (((ObjectMessage) message).getObject()
                    instanceof Casing) {
                changeCounterLabels(
                        (Casing) ((ObjectMessage) message).getObject());
            }else if (((ObjectMessage) message).getObject() instanceof Rocket) {
                updateOfARocketInRocketsTable(
                        (Rocket) ((ObjectMessage) message).getObject());
            } else {
                LOGGER.severe("Wrong message in queue");
            }
        } catch (JMSException e) {
            LOGGER.severe("Problems with GUI queue");
        }
    }
}


/**
 * This code is a simplified version of the one found in
 * HelloWorldJMSClient.java at https://github.com/wildfly/quickstart. It is
 * therefore licensed under the Apache License, Version 2.0 (the "License");
 */
package org.falafel;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
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

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.TreeSet;
import java.util.logging.Logger;

import static org.falafel.MaterialType.Casing;
import static org.falafel.MaterialType.Effect;
import static org.falafel.MaterialType.Propellant;
import static org.falafel.MaterialType.Wood;
import static org.falafel.Propellant.FULL;
import static org.falafel.Utility.CONNECTION_FACTORY;
import static org.falafel.Utility.IDS_QUEUES_INIT;
import static org.falafel.Utility.INITIAL_CONTEXT_FACTORY;
import static org.falafel.Utility.PASSWORD;
import static org.falafel.Utility.PROVIDER_URL;
import static org.falafel.Utility.USERNAME;

/**
 * Main class for the project. This class provides an interface to start
 * suppliers and keep an eye on the progress of the production in the firework
 * factory.
 */
public class FireWorks extends Application implements MessageListener {
    /** The Logger for the current class. */
    private static final Logger LOGGER =
            Logger.getLogger(FireWorks.class.getName());

    /** The running id for the suppliers. */
    private static int supplierId = 1;
    /** The running id for the materials. */
    private static int materialId = 1;

    /**  The data as an observable list for SupplyOrder. */
    private static ObservableList<SupplyOrder> order =
            FXCollections.observableArrayList();
    /**  The data as an observable list for purchases. */
    private static ObservableList<Purchase> purchases =
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
    /** HashMap for the rockets in a purchase  */
    /** The rocket counters for the different purchases. */
    private static HashMap<Integer, HashMap<Integer, RocketPackage>>
            orderedRockets = new HashMap<>();
    /** Specify the different choices for the color of an effect. */
    private static final ObservableList<String> EFFECT_COLOR_CHOICE_LIST =
            FXCollections.observableArrayList(EffectColor.Blue.toString(),
                    EffectColor.Green.toString(), EffectColor.Red.toString());

    /** Table for the purchase orders. */
    @FXML
    private TableView<Purchase> purchaseTable;
    /** Save data shown in the purchase table. */
    @FXML
    private TableColumn<Purchase, Number> purchaseBuyerIdColumn,
            purchaseIdColumn, purchaseNumberOrderedColumn,
            purchaseNumberProducedColumn;
    /** Save data shown in the purchase table. */
    @FXML
    private TableColumn<Purchase, String> purchaseStatusColumn,
            purchaseStorageAddressColumn, purchaseEffectColors;

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
            supplierCasingIdColumn, packerIdColumn, purchaseIdRocketColumn,
            buyerIdColumn;

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
    /** Saves data shown in the color column of the supplier table. */
    @FXML
    private TableColumn<SupplyOrder, String> orderedColorColumn;

    /** Label for the current number of the blue effects in the container.*/
    @FXML
    private Label blueEffectCounterLabel;
    /** Saves data shown in the blueEffectCounterLabel. */
    private static Integer blueEffectCounter = 0;
    /** Saves data shown in the blueEffectCounterLabel. */
    private static IntegerProperty blueEffectCounterProperty =
            new SimpleIntegerProperty(blueEffectCounter);
    /** Label for the current number of the green effects in the container.*/
    @FXML
    private Label greenEffectCounterLabel;
    /** Saves data shown in the greenEffectCounterLabel. */
    private static Integer greenEffectCounter = 0;
    /** Saves data shown in the greenEffectCounterLabel. */
    private static IntegerProperty greenEffectCounterProperty =
            new SimpleIntegerProperty(greenEffectCounter);
    /** Label for the current number of the red effects in the container.*/
    @FXML
    private Label redEffectCounterLabel;
    /** Saves data shown in the redEffectCounterLabel. */
    private static Integer redEffectCounter = 0;
    /** Saves data shown in the redEffectCounterLabel. */
    private static IntegerProperty redEffectCounterProperty =
            new SimpleIntegerProperty(redEffectCounter);

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

    /** Displays the number of rockets in the current rocket table. */
    @FXML
    private Label numberRocketsLabel;
    /** The number of produced rockets. */
    @FXML
    private static IntegerProperty numberRocketsProperty =
            new SimpleIntegerProperty(0);
    /** The number of shipped rockets. */
    @FXML
    private static IntegerProperty numberShippedRocketsProperty =
            new SimpleIntegerProperty(0);
    /** The number of trashed rockets. */
    @FXML
    private static IntegerProperty numberTrashedRocketsProperty =
            new SimpleIntegerProperty(0);

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    @SuppressWarnings("unused")
    private void initialize() {
        // initialize purchase table
        purchaseBuyerIdColumn.setCellValueFactory(
                cellData -> cellData.getValue().getBuyerId());
        purchaseIdColumn.setCellValueFactory(
                cellData -> cellData.getValue().getPurchaseId());
        purchaseStatusColumn.setCellValueFactory(
                cellData -> cellData.getValue().getStatusProperty());
        purchaseNumberProducedColumn.setCellValueFactory(
                cellData ->
                        cellData.getValue().getNumberFinishedRocketsProperty());
        purchaseNumberOrderedColumn.setCellValueFactory(
                cellData -> cellData.getValue().getNumberRocketsProperty());
        purchaseEffectColors.setCellValueFactory(
                cellData -> cellData.getValue().getEffectColorsProperty());
        purchaseStorageAddressColumn.setCellValueFactory(
                cellData -> cellData.getValue().getBuyerURI());

        // initialize rocket table
        rocketIdColumn.setCellValueFactory(
                cellData -> cellData.getValue().getIdProperty());
        packageIdColumn.setCellValueFactory(
                cellData -> cellData.getValue().getPackageIdProperty());
        casingIdColumn.setCellValueFactory(
                cellData -> cellData.getValue().getCasingIdProperty());
        propellantIdColumn.setCellValueFactory(
                cellData ->
                        cellData.getValue().getPropellantPackageIdProperty());
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
                cellData ->
                        cellData.getValue().getSupplierPropellantIdProperty());
        supplierEffectIdColumn.setCellValueFactory(
                cellData -> cellData.getValue().getSupplierEffectIdProperty());
        buyerIdColumn.setCellValueFactory(
                cellData -> cellData.getValue().getPurchaseBuyerIdProperty());
        purchaseIdRocketColumn.setCellValueFactory(
                cellData -> cellData.getValue().getPurchaseIdProperty());

        // initialize current warehouse labels
        casingsCounterLabel.textProperty().bind(casingsCounterProperty);
        blueEffectCounterLabel.textProperty().bind(
                Bindings.convert(blueEffectCounterProperty));
        greenEffectCounterLabel.textProperty().bind(
                Bindings.convert(greenEffectCounterProperty));
        redEffectCounterLabel.textProperty().bind(
                Bindings.convert(redEffectCounterProperty));
        propellantCounterLabel.textProperty().bind(propellantCounterProperty);
        woodCounterLabel.textProperty().bind(woodCounterProperty);
        numberOpenPropellantLabel.textProperty().bind(
                numberOpenPropellantCounterProperty);
        quantityOpenPropellantLabel.textProperty().bind(
                quantityOpenPropellantCounterProperty);
        numberRocketsLabel.textProperty().bind(
                Bindings.convert(numberRocketsProperty));

        //  initialize supplier table
        supplierNameColumn.setCellValueFactory(
                cellData -> cellData.getValue().supplierNameProperty());
        supplierNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        orderedTypeColumn.setCellValueFactory(
                cellData -> cellData.getValue().typeProperty());
        orderedTypeColumn.setCellFactory(
                ComboBoxTableCell.forTableColumn(TYPES_CHOICE_LIST));

        orderedColorColumn.setCellValueFactory(
                cellData -> cellData.getValue().colorProperty());
        orderedColorColumn.setCellFactory(
                ComboBoxTableCell.forTableColumn(EFFECT_COLOR_CHOICE_LIST));

        orderedQuantityColumn.setCellValueFactory(
                cellData -> cellData.getValue().quantityProperty());
        orderedQuantityColumn.setCellFactory(
                TextFieldTableCell.forTableColumn());

        orderedQualityColumn.setCellValueFactory(
                cellData -> cellData.getValue().qualityProperty());
        orderedQualityColumn.setCellFactory(
                TextFieldTableCell.forTableColumn());

        //CHECKSTYLE:OFF
        order.add(new SupplyOrder("Hulk", Casing.toString(), EffectColor.Blue,
                50, 100));
        order.add(new SupplyOrder("Iron Man", Wood.toString(), EffectColor.Blue,
                50, 100));
        order.add(new SupplyOrder("Captain America", Effect.toString(),
                EffectColor.Blue, 50, 100));
        order.add(new SupplyOrder("Batman", Effect.toString(), EffectColor.Red,
                50, 100));
        order.add(new SupplyOrder("Thor", Effect.toString(), EffectColor.Green,
                50, 100));
        order.add(new SupplyOrder("Seaman", Propellant.toString(),
                EffectColor.Green, 50, 100));
        order.add(new SupplyOrder("Hawk", Propellant.toString(),
                EffectColor.Red, 50, 100));

        supplyTable.isEditable();
        supplyTable.setItems(order);

        rocketTable.setItems(rockets);
        numberRocketsProperty.set(rockets.size());
        purchaseTable.setItems(purchases);
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
    @SuppressWarnings("unused")
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
    public static void changeCounterLabels(final Material material) {
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
                Effect effect = (Effect) material;
                switch (effect.getColor()) {
                    case Blue:
                        blueEffectCounter = blueEffectCounter + difference;
                        blueEffectCounterProperty.set(blueEffectCounter);
                        break;
                    case Green:
                        greenEffectCounter = greenEffectCounter + difference;
                        greenEffectCounterProperty.set(greenEffectCounter);
                        break;
                    case Red:
                        redEffectCounter = redEffectCounter + difference;
                        redEffectCounterProperty.set(redEffectCounter);
                        break;
                    default:
                        LOGGER.severe("Wrong color in effect!");
                }
            }
            if (material instanceof Propellant) {
                Propellant propellant = (Propellant) material;
                if (propellant.getQuantity() == FULL) {
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
    private static void changeOpenedPropellantLabels(
            final Propellant propellant) {
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
                numberRocketsProperty.set(rockets.size());
            }
            if (updatedRocket.getPackerId() != 0) {
                if (updatedRocket.getPurchase() == null) {
                    switch (updatedRocket.getTestResult()) {
                        case Bad:
                            trashedRocketsList.add(updatedRocket);
                            numberTrashedRocketsProperty.set(
                                    trashedRocketsList.size());
                            break;
                        default:
                            packedRocketsList.add(updatedRocket);
                            numberShippedRocketsProperty.set(
                                    packedRocketsList.size());
                    }
                } else {
                    Purchase purchase = updatedRocket.getPurchase();
                    for (int i = 0; i < purchases.size(); i++) {
                        Purchase tempPurchase = purchases.get(i);
                        if (tempPurchase.getBuyerId().intValue()
                                == purchase.getBuyerId().intValue()
                                && tempPurchase.getPurchaseId().intValue()
                                == purchase.getPurchaseId().intValue()) {
                            tempPurchase.addFinishedRockets(1);

                            orderedRockets.get(tempPurchase.getBuyerId().
                                    intValue()).get(tempPurchase.getPurchaseId().
                                    intValue()).addRocketToPackage(
                                    updatedRocket);
                            if (tempPurchase.isPurchaseFinished()) {
                                RocketPackage tempList =
                                        orderedRockets.get(tempPurchase.
                                                getBuyerId().intValue()).get(
                                                tempPurchase.getPurchaseId().
                                                intValue());
                                tempPurchase.setStatusToFinished();
                                System.out.println("OrderedRocketList"
                                        + tempList.getRockets());
                            }
                            purchases.set(i, tempPurchase);
                        }
                    }

                }
            }
        });
    }

    private void collectOrderedRocketsFromQueue(final RocketPackage rocketPackage) {
        Platform.runLater(() -> {
            for (Rocket rocket : rocketPackage.getRockets()) {
                Integer purchaseId = rocket.getPurchase().getPurchaseId().intValue();
                Integer buyerId = rocket.getPurchase().getBuyerId().intValue();
                orderedRockets.get(buyerId).get(purchaseId).addRocketToPackage(
                        rocket);
                rockets.add(rocket);
                numberRocketsProperty.set(rockets.size());
            }
        });
    }

    /**
     * Update the purchase table und the list of purchases currently active.
     *
     * @param purchase the new purchase.
     */
    private void addPurchaseToList(final Purchase purchase) {
        Platform.runLater(() -> {
            purchases.add(purchase);
            if (orderedRockets.containsKey(purchase.getBuyerId().intValue())) {
                orderedRockets.get(purchase.getBuyerId().intValue()).put(
                        purchase.getPurchaseId().intValue(),
                        new RocketPackage(0, new ArrayList<>()));
            } else {
                HashMap<Integer, RocketPackage> temp = new HashMap<>();
                temp.put(purchase.getPurchaseId().intValue(),
                        new RocketPackage(0, new ArrayList<>()));
                orderedRockets.put(purchase.getBuyerId().intValue(),
                        temp);
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
        JMSCommunication communication = new JMSCommunication();
        Purchase purchase = new Purchase(1, 1, 5, EffectColor.Blue,
                EffectColor.Green, EffectColor.Red, URI.create("buyerAddress"));
        communication.sendMessage(purchase, QueueDestinations.GUI_QUEUE);
    }

    /**
     * This method will be called when the shipped tab is pressed.
     *
     * @param event
     *          The event sent by JavaFx when the user interface
     *          element for this method is invoked.
     */
    @SuppressWarnings("unused")
    public final void displayShippedRocketsTab(final Event event) {
        rocketTable.setItems(packedRocketsList);
        numberRocketsLabel.textProperty().bind(
                Bindings.convert(numberShippedRocketsProperty));
    }

    /**
     * This method will be called when the trashed tab is pressed.
     *
     * @param event
     *          The event sent by JavaFx when the user interface
     *          element for this method is invoked.
     */
    @SuppressWarnings("unused")
    public final void displayTrashedRocketsTab(final Event event) {
        rocketTable.setItems(trashedRocketsList);
        numberRocketsLabel.textProperty().bind(
                Bindings.convert(numberTrashedRocketsProperty));
    }

    /**
     * This method will be called when the produced tab is pressed.
     *
     * @param event
     *          The event sent by JavaFx when the user interface
     *          element for this method is invoked.
     */
    @SuppressWarnings("unused")
    public final void displayProducedRocketsTab(final Event event) {
        rocketTable.setItems(rockets);
        numberRocketsLabel.textProperty().bind(
                Bindings.convert(numberRocketsProperty));
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
     * This method will be invoked when the color of a material is changed.
     *
     * @param stCellEditEvent
     *          The cell edit event sent by JavaFx when the user interface
     *          element for this method is invoked.
     */
    public final void setColor(
            final TableColumn.CellEditEvent<SupplyOrder, String>
                    stCellEditEvent) {
        stCellEditEvent.getTableView().getItems().get(
                stCellEditEvent.getTablePosition().getRow()).setColor(
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

        JMSCommunication communicator = new JMSCommunication();
        for (Purchase purchase : purchases) {
            communicator.sendMessage(purchase,
                    QueueDestinations.PURCHASE_ORDER_QUEUE);
        }
        for (Integer buyerId : orderedRockets.keySet()) {
            for (Integer purchaseId : orderedRockets.get(buyerId).keySet()) {
                communicator.sendMessage(
                        orderedRockets.get(buyerId).get(purchaseId),
                        QueueDestinations.ROCKET_ORDERED_QUEUE);
            }
        }
        System.out.println("Goodbye!");
    }

    /**
     * Start the fireworks factory.
     *
     * @param arguments A list containing the command line arguments.
     */
    public static void main(final String[] arguments) {
        launch(arguments);
    }

    /**
     * Read old data from queues and initialize queues for the Ids.
     */
    private void initListeners() {
        System.out.println("Set listeners to queues");
        try {
            JMSCommunication communicator = new JMSCommunication();
            // read the content of the storage queues
            for (Object object : communicator.readMessagesInQueue(
                    QueueDestinations.STORAGE_CASING_QUEUE)) {
                changeCounterLabels((Casing) object);
            }
            for (Object object : communicator.readMessagesInQueue(
                    QueueDestinations.STORAGE_BLUE_EFFECT_QUEUE)) {
                changeCounterLabels((Effect) object);
            }
            for (Object object : communicator.readMessagesInQueue(
                    QueueDestinations.STORAGE_GREEN_EFFECT_QUEUE)) {
                changeCounterLabels((Effect) object);
            }
            for (Object object : communicator.readMessagesInQueue(
                    QueueDestinations.STORAGE_RED_EFFECT_QUEUE)) {
                changeCounterLabels((Effect) object);
            }
            for (Object object : communicator.readMessagesInQueue(
                    QueueDestinations.STORAGE_CLOSED_PROP_QUEUE)) {
                changeCounterLabels((Propellant) object);
            }
            for (Object object : communicator.readMessagesInQueue(
                    QueueDestinations.STORAGE_OPENED_PROP_QUEUE)) {
                changeCounterLabels((Propellant) object);
            }
            for (Object object : communicator.readMessagesInQueue(
                    QueueDestinations.STORAGE_WOOD_QUEUE)) {
                changeCounterLabels((Wood) object);
            }

            // read the content of the rocket queues
            for (Object object : communicator.readMessagesInQueue(
                    QueueDestinations.ROCKET_PRODUCED_QUEUE)) {
                updateOfARocketInRocketsTable((Rocket) object);
            }
            for (Object object : communicator.readMessagesInQueue(
                    QueueDestinations.ROCKET_TESTED_QUEUE)) {
                updateOfARocketInRocketsTable((Rocket) object);
            }
            for (Object object : communicator.readMessagesInQueue(
                    QueueDestinations.ROCKET_SHIPPED_QUEUE)) {
                RocketPackage rocketPackage = (RocketPackage) object;
                for (Rocket rocket : rocketPackage.getRockets()) {
                    updateOfARocketInRocketsTable(rocket);
                }
            }
            for (Object object : communicator.readMessagesInQueue(
                    QueueDestinations.ROCKET_TRASHED_QUEUE)) {
                updateOfARocketInRocketsTable((Rocket) object);
            }

            for (Object object : communicator.receiveCompleteQueue(
                    QueueDestinations.PURCHASE_ORDER_QUEUE)) {
                addPurchaseToList((Purchase) object);
            }
            for (Object object : communicator.receiveCompleteQueue(
                    QueueDestinations.ROCKET_ORDERED_QUEUE)) {
                collectOrderedRocketsFromQueue((RocketPackage) object);
            }

            // Check with ids are in the queues and fill them so that each
            // contains 10 ids
            TreeSet<Integer> ids = new TreeSet<>();
            for (Object object : communicator.readMessagesInQueue(
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
            while ((last - first) < IDS_QUEUES_INIT - 1) {
                last++;
                communicator.sendMessage(last,
                        QueueDestinations.ID_ROCKET_QUEUE);
            }
            for (Object object : communicator.readMessagesInQueue(
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
            while ((last - first) < IDS_QUEUES_INIT - 1) {
                last++;
                communicator.sendMessage(last,
                        QueueDestinations.ID_PACKET_QUEUE);
            }

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

            JMSConsumer consumer = connectionFactory.createContext(
                    USERNAME, PASSWORD).createConsumer(
                    destination);

            consumer.setMessageListener(this);

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
    public final void onMessage(final Message message) {
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
                changeCounterLabels(
                        (Propellant) ((ObjectMessage) message).getObject());
            } else if (((ObjectMessage) message).getObject()
                    instanceof Casing) {
                changeCounterLabels(
                        (Casing) ((ObjectMessage) message).getObject());
            } else if (((ObjectMessage) message).getObject()
                    instanceof Rocket) {
                updateOfARocketInRocketsTable(
                        (Rocket) ((ObjectMessage) message).getObject());
            } else if (((ObjectMessage) message).getObject()
                    instanceof RocketPackage) {
                ArrayList<Rocket> packedRockets = ((RocketPackage)
                        ((ObjectMessage) message).getObject()).getRockets();
                for (Rocket rocket : packedRockets) {
                    updateOfARocketInRocketsTable(rocket);
                }
            } else if (((ObjectMessage) message).getObject()
                    instanceof Purchase) {
                Purchase purchase = (Purchase)
                        ((ObjectMessage) message).getObject();
                WritePurchasesToCurrentQueue writer
                        = new WritePurchasesToCurrentQueue(purchase);
                writer.start();
                addPurchaseToList(purchase);
            } else {
                LOGGER.severe("Wrong message in queue");
            }
        } catch (JMSException e) {
            LOGGER.severe("Problems with GUI queue");
        }
    }

    /**
     * Remove all suppliers from the supplier table.
     *
     * @param actionEvent
     *          The event sent by JavaFx when the user interface
     *          element for this method is invoked.
     *
     */
    @SuppressWarnings("unused")
    public final void clearOrder(final ActionEvent actionEvent) {
        order.clear();
        JMSCommunication communication = new JMSCommunication();
        ArrayList<Object> queuData =
                communication.receiveCompleteQueue(QueueDestinations.ROCKET_ORDERED_QUEUE);
        System.out.println("CurrentPurchaseQueue: " + queuData);
    }
}


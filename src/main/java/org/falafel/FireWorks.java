/**
 * This code is a simplified version of the one found in
 * HelloWorldJMSClient.java at https://github.com/wildfly/quickstart. It is
 * therefore licensed under the Apache License, Version 2.0 (the "License");
 */
package org.falafel;

import java.util.Properties;
import java.util.logging.Logger;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
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

import static java.util.Arrays.asList;
import static org.falafel.MaterialType.Casing;
import static org.falafel.MaterialType.Effect;
import static org.falafel.MaterialType.Propellant;
import static org.falafel.MaterialType.Wood;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FireWorks extends Application {
    private static final Logger LOGGER =
            Logger.getLogger(FireWorks.class.getName());

    private static final String CONNECTION_FACTORY =
            "jms/RemoteConnectionFactory";
    private static final String DESTINATION = "jms/queue/gui/propellants/closed";
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
    private TableColumn<Rocket, String> casingIdColumn, packageIdColumn,
            rocketIdColumn, woodIdColumn, propellantIdColumn,
            propellantQuantityColumn, effectIdColumn, testResultColumn,
            workerIdColumn, testerIdColumn, supplierWoodIdColumn,
            supplierCasingColumn, supplierPropellantIdColumn,
            supplierEffectIdColumn, packerIdColumn;
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
        supplierCasingColumn.setCellValueFactory(
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
        rockets.add(new Rocket(1, new Wood(1, "hugo", 1), new Casing(2, "Rene", 2),
                effects, propellants, 130, 434));
        */
        order.add(new SupplyOrder("Hulk", Casing.toString(), 5, 100));
        order.add(new SupplyOrder("Iron Man", Wood.toString(), 5, 100));
        order.add(new SupplyOrder("Captain America", Effect.toString(), 5, 100));
        order.add(new SupplyOrder("Batman", Effect.toString(), 5, 80));
        order.add(new SupplyOrder("Thor", Effect.toString(), 5, 60));
        order.add(new SupplyOrder("Seaman", Propellant.toString(), 2, 100));
        order.add(new SupplyOrder("Hawk", Propellant.toString(), 3, 100));
        //CHECKSTYLE:ON

        supplyTable.isEditable();
        supplyTable.setItems(order);

        rocketTable.setItems(rockets);
        numberRocketsProperty.set(Integer.toString(rockets.size()));
    }

    /**
     * Updates the counters in the GUI.
     *
     * @param containerId
     *          ID of the changed container
     * @param difference
     *          The value that should be added or subtracted from the element
     *          with the identifier {@code containerId}.
     *
     */
    public static void changeCounterLabels(final String containerId,
                                           final int difference) {
        Platform.runLater(() -> {
            // je nach queue wird der counter verändert
        });
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
     * Close the containers and the space.
     */
    private static void close() {
        System.out.println("Goodbye!");
    }
    public static void main(String[] arguments) {
        launch(arguments);
    }

    @Override
    public final void start(final Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(
                getClass().getResource("/FireWorks.fxml"));

        primaryStage.setTitle("Fireworks Factory");
        primaryStage.setOnCloseRequest(event -> close());
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}


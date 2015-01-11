package org.falafel;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import static java.util.Arrays.asList;
import static java.util.logging.Logger.getLogger;
import static org.falafel.EffectColor.Blue;
import static org.falafel.EffectColor.Green;
import static org.falafel.EffectColor.Red;

/**
 * This class represents a buyer of rockets.
 *
 * A buyer orders rockets from the factory and stores them into his space after
 * the rockets were produced.
 */
public final class Buyer extends Application implements MessageListener {

    /** Get the Logger for the current class. */
    private static final Logger LOGGER = getLogger(Buyer.class.getName());

    /** The unique identification of this buyer. */
    private static Integer buyerId;
    /** The URI of this buyer. */
    private static URI buyerURI;

    /** The file where the buyer stores purchased items. */
    private static String storageLocationPurchases;

    /** The data stored in the table for new purchases. */
    private static ObservableList<Purchase> purchases =
            FXCollections.observableArrayList();
    /** The data stored in the table for purchased items. */
    private static ObservableList<Purchase> purchased =
            FXCollections.observableArrayList();

    /** Specify the different effect colors a buyer can purchase. */
    private static final ObservableList<String> EFFECT_CHOICES =
            FXCollections.observableArrayList(Red.toString(),
                    Blue.toString(), Green.toString());

    /** The tables for new and existing purchases. */
    @FXML
    private TableView<Purchase> purchaseTableView, newPurchaseTableView;

    /** The columns for purchase properties that are numbers. */
    @FXML
    private TableColumn<Purchase, Number>
            idPurchaseColumn,
            quantityPurchaseColumn;

    /** The columns for purchase properties that can not be represented by
     *  simple numbers. */
    @FXML
    private TableColumn<Purchase, String>
            statusPurchaseColumn,
            color1PurchaseColumn,
            color2PurchaseColumn,
            color3PurchaseColumn,
            newQuantityPurchaseColumn,
            newColor1PurchaseColumn,
            newColor2PurchaseColumn,
            newColor3PurchaseColumn;

    /**
     * Start the buyer.
     *
     * @param arguments A list containing the command line arguments.
     */
    public static void main(final String[] arguments) {

        final int numberArguments = 1;

        if (arguments.length != numberArguments) {
            System.err.println("Usage: buyer <Id>");
            return;
        }
        try {
            buyerId = Integer.parseInt(arguments[0]);
            buyerURI = URI.create("jms/queue/buyer/" + buyerId);
        } catch (Exception e) {
            System.err.println("Please supply valid command line arguments!");
            System.exit(1);
        }

        initStorage();
        initJMS();
        launch(arguments);
    }

    /**
     * Initialize the data for the graphical user interface.
     */
    @SuppressWarnings("unused")
    @FXML
    private void initialize() {
        newQuantityPurchaseColumn.setCellValueFactory(
                cellData -> Bindings.convert(
                        cellData.getValue().getNumberRocketsProperty()));
        newQuantityPurchaseColumn.setCellFactory(
                TextFieldTableCell.forTableColumn());

        newColor1PurchaseColumn.setCellValueFactory(
                cellData -> cellData.getValue().getFirstColorProperty());
        newColor1PurchaseColumn.setCellFactory(
                ComboBoxTableCell.forTableColumn(EFFECT_CHOICES));

        newColor2PurchaseColumn.setCellValueFactory(
                cellData -> cellData.getValue().getSecondColorProperty());
        newColor2PurchaseColumn.setCellFactory(
                ComboBoxTableCell.forTableColumn(EFFECT_CHOICES));

        newColor3PurchaseColumn.setCellValueFactory(
                cellData -> cellData.getValue().getThirdColorProperty());
        newColor3PurchaseColumn.setCellFactory(
                ComboBoxTableCell.forTableColumn(EFFECT_CHOICES));

        statusPurchaseColumn.setCellValueFactory(
                cellData -> cellData.getValue().getStatusProperty());
        color1PurchaseColumn.setCellValueFactory(
                cellData -> cellData.getValue().getFirstColorProperty());
        color2PurchaseColumn.setCellValueFactory(
                cellData -> cellData.getValue().getSecondColorProperty());
        color3PurchaseColumn.setCellValueFactory(
                cellData -> cellData.getValue().getThirdColorProperty());
        idPurchaseColumn.setCellValueFactory(
                cellData -> cellData.getValue().getPurchaseId());
        quantityPurchaseColumn.setCellValueFactory(
                cellData -> cellData.getValue().getNumberRocketsProperty());

        newPurchaseTableView.setItems(purchases);
        purchaseTableView.setItems(purchased);
    }

    @Override
    public void start(final Stage primaryStage) throws IOException {

        final float minWidth = 620;
        final float minHeight = 112;

        primaryStage.setMinWidth(minWidth);
        primaryStage.setMinHeight(minHeight);

        Parent root = FXMLLoader.load(getClass().getResource("/Buyer.fxml"));
        primaryStage.setTitle("Buyer " + buyerId + "—" + buyerURI);
        primaryStage.setOnCloseRequest(event -> closeBuyer());
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    /**
     * Initialize the local storage for the buyer.
     */
    private static void initStorage() {
        String directoryStorageLocation
                = System.getProperty("java.io.tmpdir") + File.separator;
        String fileEnding = buyerId + ".ser";

        storageLocationPurchases
                = directoryStorageLocation + "rockets" + fileEnding;
        File fileRocketPackages = new File(storageLocationPurchases);

        /* Create storage if it does not exist already or is a directory */
        if (!fileRocketPackages.isFile()) {
            try (
                OutputStream file = new FileOutputStream(
                        storageLocationPurchases);
                OutputStream buffer = new BufferedOutputStream(file);
                ObjectOutput output = new ObjectOutputStream(buffer)
            ) {
                output.writeObject(new ArrayList<Purchase>());
            } catch (IOException ex) {
                LOGGER.severe("Could not create storage file for purchased "
                        + "items!");
            }
            LOGGER.info("Created new storage for purchases");
        }
        LOGGER.info("Initialized storage");
    }

    /**
     * Add the given purchases to the storage.
     *
     * @param purchases
     *          A list of purchases that should be added to the current
     *          purchases inside the storage
     */
    private static void addPurchasesToStorage(
            final Collection<Purchase> purchases) {

        /* Read old purchases */
        ArrayList<Purchase> storedPurchases = readPurchasesFromStorage();

        /* Add purchases */
        storedPurchases.addAll(purchases);

        /* Write all purchases */
        try (
            OutputStream file = new FileOutputStream(
                    storageLocationPurchases);
            OutputStream buffer = new BufferedOutputStream(file);
            ObjectOutput output = new ObjectOutputStream(buffer)
        ) {
            output.writeObject(storedPurchases);
        } catch (IOException ex) {
            LOGGER.severe("Could not write purchases to storage");
        }
    }

    /**
     * Read all purchases from the storage.
     *
     * @return
     *          A list containing all purchases in the storage
     */
    @SuppressWarnings("unchecked")
    private static ArrayList<Purchase> readPurchasesFromStorage() {

        ArrayList<Purchase> purchases = new ArrayList<>();

        try (
            InputStream file = new FileInputStream(storageLocationPurchases);
            InputStream buffer = new BufferedInputStream(file);
            ObjectInput input = new ObjectInputStream(buffer)
        ) {
            purchases = (ArrayList<Purchase>) input.readObject();

        } catch (ClassNotFoundException ex) {
            LOGGER.severe("Non compatible data in storage for purchased "
                    + "items!");
        } catch (IOException ex) {
            LOGGER.severe("Could no read purchase storage!");
            ex.printStackTrace();
        }

        return  purchases;
    }

    /**
     * Remove all purchases from the storage.
     */
    private static void removePurchasesFromStorage() {
        try (
                OutputStream file = new FileOutputStream(
                        storageLocationPurchases);
                OutputStream buffer = new BufferedOutputStream(file);
                ObjectOutput output = new ObjectOutputStream(buffer)
        ) {
            output.writeObject(new ArrayList<Purchase>());
        } catch (IOException ex) {
            LOGGER.severe("Could not create new storage file for purchased "
                    + "items!");
        }
        LOGGER.info("Cleared purchases from storage");

    }

    /**
     * Initialize the space.
     */
    private static void initJMS() {
        //CHECKSTYLE:OFF
        purchases.addAll(asList(
            new Purchase(buyerId, 1, Red, Green, Blue, buyerURI),
            new Purchase(buyerId, 5, Red, Blue, Blue, buyerURI),
            new Purchase(buyerId, 1, Green, Green, Green, buyerURI))
        );
        //CHECKSTYLE:ON
        purchased.addAll(readPurchasesFromStorage());
    }

    /** Close resources handled by this buyer. */
    private void closeBuyer() {
        LOGGER.info("Close buyer " + buyerId);
    }

    /**
     * Create a new purchase in the table for new purchases.
     *
     * @param actionEvent
     *          The action event sent by JavaFX when the user interface
     *          element for this method is invoked.
     */
    @SuppressWarnings("unused")
    public void newPurchase(final ActionEvent actionEvent) {
        purchases.add(new Purchase(buyerId, buyerURI));
    }

    /**
     * Remove all purchases from the table for new purchases.
     *
     * @param actionEvent
     *          The action event sent by JavaFX when the user interface
     *          element for this method is invoked.
     */
    @SuppressWarnings("unused")
    public void clearPurchase(final ActionEvent actionEvent) {
        purchases.clear();
    }

    /**
     * Order all purchases currently stored in the table for new purchases.
     *
     * @param actionEvent
     *          The action event sent by JavaFX when the user interface
     *          element for this method is invoked.
     */
    @SuppressWarnings("unused")
    public void orderPurchase(final ActionEvent actionEvent) {
        purchased.addAll(purchases);
        addPurchasesToStorage(purchases);
        purchases.clear();
    }

    /**
     * Remove all current purchases from the space of the buyer.
     *
     * @param actionEvent
     *          The action event sent by JavaFX when the user interface
     *          element for this method is invoked.
     */
    @SuppressWarnings("unused")
    public void removePurchases(final ActionEvent actionEvent) {
        removePurchasesFromStorage();
        purchased.clear();
    }

    /**
     * This method will be invoked when the quantity for a purchase is changed.
     *
     * @param stCellEditEvent
     *          The cell edit event sent by JavaFX when the user interface
     *          element for this method is invoked.
     */
    public void setQuantity(
            final CellEditEvent<Purchase, String> stCellEditEvent) {
        int row = stCellEditEvent.getTablePosition().getRow();
        stCellEditEvent.getTableView().getItems().get(row).setNumberRockets(
                stCellEditEvent.getNewValue());
    }

    /**
     * This method will be invoked when the first effect color for a purchase
     * is changed.
     *
     * @param stCellEditEvent
     *          The cell edit event sent by JavaFX when the user interface
     *          element for this method is invoked.
     */
    public void setFirstEffectColor(
            final CellEditEvent<Purchase, String> stCellEditEvent) {
        int row = stCellEditEvent.getTablePosition().getRow();

        stCellEditEvent.getTableView().getItems().get(row).setFirstEffectColor(
                stCellEditEvent.getNewValue());
    }

    /**
     * This method will be invoked when the second effect color for a purchase
     * is changed.
     *
     * @param stCellEditEvent
     *          The cell edit event sent by JavaFX when the user interface
     *          element for this method is invoked.
     */
    public void setSecondEffectColor(
            final CellEditEvent<Purchase, String> stCellEditEvent) {
        int row = stCellEditEvent.getTablePosition().getRow();

        stCellEditEvent.getTableView().getItems().get(row).setSecondEffectColor(
                stCellEditEvent.getNewValue());

    }

    /**
     * This method will be invoked when the third effect color for a purchase
     * is changed.
     *
     * @param stCellEditEvent
     *          The cell edit event sent by JavaFX when the user interface
     *          element for this method is invoked.
     */
    public void setThirdEffectColor(
            final CellEditEvent<Purchase, String> stCellEditEvent) {
        int row = stCellEditEvent.getTablePosition().getRow();

        stCellEditEvent.getTableView().getItems().get(row).setThirdEffectColor(
                stCellEditEvent.getNewValue());
    }

    /**
     * This method will be called when the buyer receives a message.
     *
     * @param message
     *          The message (from the fireworks factory)
     */
    @Override
    public void onMessage(final Message message) {
        try {
            Object receivedObject = ((ObjectMessage) message).getObject();
            if (receivedObject instanceof RocketPackage) {
                LOGGER.info("Received rocket package.");
            } else {
                LOGGER.severe("Got a message not containing a RocketPackage!");
            }
        } catch (JMSException e) {
            LOGGER.severe("Error while receiving a message!");
        }
    }
}

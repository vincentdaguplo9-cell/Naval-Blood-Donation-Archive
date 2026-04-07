package ui;

import dao.BloodUnitDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.BloodUnit;

import java.util.List;

// Blood inventory management window (JavaFX).
public class InventoryFrame extends Stage {
    private static InventoryFrame instance;
    private final BloodUnitDAO unitDAO;
    private final TableView<BloodUnit> unitTable;
    private final ComboBox<String> filterCombo;
    private final Pagination pagination;
    private static final int PAGE_SIZE = 20;
    private List<BloodUnit> currentUnits;

    public InventoryFrame() {
        unitDAO = new BloodUnitDAO();

        setTitle("Blood Inventory");

        unitTable = new TableView<>();
        UIStyle.applyTableStyle(unitTable);
        configureUnitTable();
        applyZebraRows();

        filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("All", "Available", "Issued", "Expired");
        filterCombo.getSelectionModel().select(0);
        filterCombo.setOnAction(e -> loadUnits());

        Button updateBtn = UIStyle.primaryButton("Update Status");
        updateBtn.setOnAction(e -> updateStatus());
        updateBtn.disableProperty().bind(unitTable.getSelectionModel().selectedItemProperty().isNull());

        Label filterLabel = new Label("Filter:");
        HBox topPanel = new HBox(8, filterLabel, filterCombo, updateBtn);
        topPanel.setAlignment(Pos.CENTER_LEFT);

        pagination = new Pagination(1, 0);
        pagination.setPageFactory(this::createPage);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + UIStyle.BG + ";");
        root.setPadding(new Insets(12));
        root.setTop(topPanel);
        root.setCenter(new javafx.scene.layout.VBox(10, unitTable, pagination));

        Scene scene = new Scene(root, 900, 500);
        setScene(scene);

        loadUnits();
    }

    public static void showWindow() {
        if (instance == null) {
            instance = new InventoryFrame();
            instance.setOnHidden(e -> instance = null);
        }
        instance.show();
        instance.toFront();
    }

    private void configureUnitTable() {
        TableColumn<BloodUnit, Integer> idCol = new TableColumn<>("Unit ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("unitId"));

        TableColumn<BloodUnit, Integer> donorCol = new TableColumn<>("Donor ID");
        donorCol.setCellValueFactory(new PropertyValueFactory<>("donorId"));

        TableColumn<BloodUnit, String> bloodCol = new TableColumn<>("Blood Type");
        bloodCol.setCellValueFactory(new PropertyValueFactory<>("bloodType"));

        TableColumn<BloodUnit, Integer> volumeCol = new TableColumn<>("Volume");
        volumeCol.setCellValueFactory(new PropertyValueFactory<>("volumeMl"));

        TableColumn<BloodUnit, java.sql.Date> collectionCol = new TableColumn<>("Collection Date");
        collectionCol.setCellValueFactory(new PropertyValueFactory<>("collectionDate"));

        TableColumn<BloodUnit, java.sql.Date> expiryCol = new TableColumn<>("Expiry Date");
        expiryCol.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));

        TableColumn<BloodUnit, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        unitTable.getColumns().addAll(idCol, donorCol, bloodCol, volumeCol, collectionCol, expiryCol, statusCol);
    }

    private void loadUnits() {
        String status = filterCombo == null ? "All" : filterCombo.getSelectionModel().getSelectedItem();
        if (status == null || status.equalsIgnoreCase("All")) {
            currentUnits = unitDAO.getAllUnits();
        } else {
            currentUnits = unitDAO.getUnitsByStatus(status);
        }
        int pageCount = Math.max(1, (int) Math.ceil((double) currentUnits.size() / PAGE_SIZE));
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(0);
        updatePage(0);
    }

    private void updateStatus() {
        BloodUnit unit = unitTable.getSelectionModel().getSelectedItem();
        if (unit == null) {
            new Alert(Alert.AlertType.WARNING, "Select a blood unit to update.").showAndWait();
            return;
        }
        javafx.scene.control.ChoiceDialog<String> confirm = new javafx.scene.control.ChoiceDialog<>(
                "Available",
                "Available",
                "Issued",
                "Expired"
        );
        confirm.setTitle("Confirm Status Update");
        confirm.setHeaderText("Select the new status for Unit ID " + unit.getUnitId());
        confirm.setContentText("Status:");

        java.util.Optional<String> chosen = confirm.showAndWait();
        if (!chosen.isPresent()) {
            return;
        }
        String chosenStatus = chosen.get();

        if (unitDAO.updateStatus(unit.getUnitId(), chosenStatus)) {
            new Alert(Alert.AlertType.INFORMATION, "Status updated.").showAndWait();
            loadUnits();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to update status.");
            alert.setHeaderText("Error");
            alert.showAndWait();
        }
    }

    private javafx.scene.layout.VBox createPage(Integer pageIndex) {
        updatePage(pageIndex);
        return new javafx.scene.layout.VBox();
    }

    private void updatePage(int pageIndex) {
        if (currentUnits == null) {
            unitTable.setItems(FXCollections.observableArrayList());
            return;
        }
        int fromIndex = pageIndex * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, currentUnits.size());
        ObservableList<BloodUnit> rows = FXCollections.observableArrayList(currentUnits.subList(fromIndex, toIndex));
        unitTable.setItems(rows);
    }

    private void applyZebraRows() {
        unitTable.setRowFactory(tv -> new javafx.scene.control.TableRow<BloodUnit>() {
            @Override
            protected void updateItem(BloodUnit item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (isSelected()) {
                    setStyle("");
                } else {
                    if (getIndex() % 2 == 0) {
                        setStyle("-fx-background-color: #ffffff;");
                    } else {
                        setStyle("-fx-background-color: #f5f7fa;");
                    }
                }
            }
        });
    }
}

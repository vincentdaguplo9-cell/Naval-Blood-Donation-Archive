package ui;

import dao.BloodUnitDAO;
import dao.InventoryTxDAO;
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
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.BloodUnit;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

// Blood inventory management window (JavaFX).
public class InventoryFrame extends Stage {
    private static InventoryFrame instance;
    private final BloodUnitDAO unitDAO;
    private final InventoryTxDAO invTxDAO;
    private final TableView<BloodUnit> unitTable;
    private final ComboBox<String> filterCombo;
    private final ComboBox<String> testFilterCombo;
    private final ComboBox<String> bloodTypeFilterCombo;
    private final Pagination pagination;
    private static final int PAGE_SIZE = 20;
    private List<BloodUnit> currentUnits;
    private String initialStatusFilter = "All";
    private String initialTestFilter = "All";
    private String initialBloodTypeFilter = "All";

    public InventoryFrame() {
        unitDAO = new BloodUnitDAO();
        invTxDAO = new InventoryTxDAO();

        setTitle("Blood Inventory");

        unitTable = new TableView<>();
        UIStyle.applyTableStyle(unitTable);
        configureUnitTable();
        applyZebraRows();

        filterCombo = new ComboBox<>();
        UIStyle.applyInputStyle(filterCombo);
        filterCombo.getItems().addAll("All", "AVAILABLE", "QUARANTINE", "ISSUED", "EXPIRED", "DISCARDED");
        filterCombo.getSelectionModel().select(0);
        filterCombo.setOnAction(e -> loadUnits());

        testFilterCombo = new ComboBox<>();
        UIStyle.applyInputStyle(testFilterCombo);
        testFilterCombo.getItems().addAll("All", "PENDING", "PASSED", "FAILED");
        testFilterCombo.getSelectionModel().select(0);
        testFilterCombo.setOnAction(e -> loadUnits());

        bloodTypeFilterCombo = new ComboBox<>();
        UIStyle.applyInputStyle(bloodTypeFilterCombo);
        bloodTypeFilterCombo.getItems().addAll("All", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
        bloodTypeFilterCombo.getSelectionModel().select(0);
        bloodTypeFilterCombo.setOnAction(e -> loadUnits());

        Button updateBtn = UIStyle.primaryButton("Update Status", "bloodbag");
        updateBtn.setOnAction(e -> updateStatus());
        updateBtn.disableProperty().bind(unitTable.getSelectionModel().selectedItemProperty().isNull());

        Button issueBtn = UIStyle.secondaryButton("Issue");
        issueBtn.setOnAction(e -> issueSelected());
        issueBtn.disableProperty().bind(unitTable.getSelectionModel().selectedItemProperty().isNull());

        Button locationBtn = UIStyle.secondaryButton("Set Location");
        locationBtn.setOnAction(e -> setLocation());
        locationBtn.disableProperty().bind(unitTable.getSelectionModel().selectedItemProperty().isNull());

        Button labBtn = UIStyle.secondaryButton("Lab Testing", "lab");
        labBtn.setOnAction(e -> LabTestingFrame.showWindow());

        Label filterLabel = UIStyle.formLabel("Status:");
        Label testLabel = UIStyle.formLabel("Test:");
        Label bloodTypeLabel = UIStyle.formLabel("Blood Type:");
        HBox topPanel = new HBox(8, filterLabel, filterCombo, testLabel, testFilterCombo, bloodTypeLabel, bloodTypeFilterCombo,
                updateBtn, issueBtn, locationBtn, labBtn);
        topPanel.setAlignment(Pos.CENTER_LEFT);

        pagination = new Pagination(1, 0);
        pagination.setPageFactory(this::createPage);

        unitTable.setPlaceholder(UIStyle.helperLabel("No blood units found. Adjust the filters or record a new donation."));

        BorderPane root = new BorderPane();
        root.setStyle(UIStyle.pageBackground());
        root.setPadding(new Insets(0));

        Button dashboardBtn = UIStyle.secondaryButton("Dashboard", "dashboard");
        dashboardBtn.setOnAction(e -> DashboardFrame.showWindow());
        HBox header = UIStyle.appHeader("Blood Inventory", dashboardBtn);
        javafx.scene.layout.VBox topWrap = new javafx.scene.layout.VBox(10, header, topPanel);
        topWrap.setPadding(new Insets(0, 0, 8, 0));
        UIStyle.applyPanelStyle(topPanel);
        topPanel.setPadding(new Insets(14, 16, 14, 16));

        VBox centerBox = new VBox(10, unitTable, pagination);
        centerBox.setPadding(new Insets(12));
        UIStyle.applyPanelStyle(centerBox);

        root.setTop(topWrap);
        root.setCenter(centerBox);

        Scene scene = new Scene(root, 900, 500);
        setScene(scene);

        applyInitialFilters();
        loadUnits();
    }

    public static void showWindow() {
        showWindow("All", "All", "All");
    }

    public static void showWindow(String statusFilter, String testFilter, String bloodTypeFilter) {
        if (instance == null) {
            instance = new InventoryFrame();
            instance.setOnHidden(e -> instance = null);
        }
        instance.applyFilters(statusFilter, testFilter, bloodTypeFilter);
        instance.setIconified(false);
        instance.setMaximized(true);
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

        TableColumn<BloodUnit, String> compCol = new TableColumn<>("Component");
        compCol.setCellValueFactory(new PropertyValueFactory<>("component"));

        TableColumn<BloodUnit, String> testCol = new TableColumn<>("Test");
        testCol.setCellValueFactory(new PropertyValueFactory<>("testStatus"));
        UIStyle.applyStatusBadgeColumn(testCol);

        TableColumn<BloodUnit, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        UIStyle.applyStatusBadgeColumn(statusCol);

        TableColumn<BloodUnit, String> locCol = new TableColumn<>("Location");
        locCol.setCellValueFactory(new PropertyValueFactory<>("storageLocation"));

        unitTable.getColumns().addAll(idCol, donorCol, bloodCol, volumeCol, collectionCol, expiryCol, compCol, testCol, statusCol, locCol);
    }

    private void loadUnits() {
        String status = filterCombo == null ? "All" : filterCombo.getSelectionModel().getSelectedItem();
        String test = testFilterCombo == null ? "All" : testFilterCombo.getSelectionModel().getSelectedItem();
        String bloodType = bloodTypeFilterCombo == null ? "All" : bloodTypeFilterCombo.getSelectionModel().getSelectedItem();
        currentUnits = unitDAO.getUnitsFiltered(status, test, bloodType);
        int pageCount = Math.max(1, (int) Math.ceil((double) currentUnits.size() / PAGE_SIZE));
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(0);
        updatePage(0);
    }

    private void applyInitialFilters() {
        applyFilters(initialStatusFilter, initialTestFilter, initialBloodTypeFilter);
    }

    private void applyFilters(String statusFilter, String testFilter, String bloodTypeFilter) {
        if (filterCombo == null || testFilterCombo == null || bloodTypeFilterCombo == null) {
            initialStatusFilter = statusFilter == null ? "All" : statusFilter;
            initialTestFilter = testFilter == null ? "All" : testFilter;
            initialBloodTypeFilter = bloodTypeFilter == null ? "All" : bloodTypeFilter;
            return;
        }
        selectFilterValue(filterCombo, statusFilter);
        selectFilterValue(testFilterCombo, testFilter);
        selectFilterValue(bloodTypeFilterCombo, bloodTypeFilter);
        loadUnits();
    }

    private void selectFilterValue(ComboBox<String> comboBox, String value) {
        String target = value == null || value.trim().isEmpty() ? "All" : value;
        if (comboBox.getItems().contains(target)) {
            comboBox.getSelectionModel().select(target);
        } else {
            comboBox.getSelectionModel().select("All");
        }
    }

    private void updateStatus() {
        BloodUnit unit = unitTable.getSelectionModel().getSelectedItem();
        if (unit == null) {
            new Alert(Alert.AlertType.WARNING, "Select a blood unit to update.").showAndWait();
            return;
        }
        javafx.scene.control.ChoiceDialog<String> confirm = new javafx.scene.control.ChoiceDialog<>(
                "AVAILABLE",
                "AVAILABLE",
                "QUARANTINE",
                "RESERVED",
                "ISSUED",
                "EXPIRED",
                "DISCARDED"
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

    private void issueSelected() {
        BloodUnit unit = unitTable.getSelectionModel().getSelectedItem();
        if (unit == null) {
            new Alert(Alert.AlertType.WARNING, "Select a blood unit to issue.").showAndWait();
            return;
        }
        if (!"PASSED".equalsIgnoreCase(unit.getTestStatus()) || !"AVAILABLE".equalsIgnoreCase(unit.getStatus())) {
            new Alert(Alert.AlertType.WARNING, "Only AVAILABLE units with PASSED tests can be issued.").showAndWait();
            return;
        }

        TextInputDialog refDlg = new TextInputDialog("");
        refDlg.setTitle("Issue Unit");
        refDlg.setHeaderText("Enter reference number (patient/request).");
        refDlg.setContentText("Reference No:");
        java.util.Optional<String> ref = refDlg.showAndWait();
        if (!ref.isPresent()) {
            return;
        }
        String refNo = ref.get().trim();

        TextInputDialog staffDlg = new TextInputDialog("201");
        staffDlg.setTitle("Issue Unit");
        staffDlg.setHeaderText("Enter Staff ID performing the issuance.");
        staffDlg.setContentText("Staff ID:");
        java.util.Optional<String> staff = staffDlg.showAndWait();
        if (!staff.isPresent()) {
            return;
        }
        String staffRaw = staff.get().trim();
        int staffId;
        try {
            staffId = Integer.parseInt(staffRaw);
        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.WARNING, "Invalid staff ID.").showAndWait();
            return;
        }

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        if (unitDAO.updateStatus(unit.getUnitId(), "ISSUED")) {
            invTxDAO.addTx(unit.getUnitId(), "ISSUE", now, staffId, refNo, "Issued");
            new Alert(Alert.AlertType.INFORMATION, "Unit issued.").showAndWait();
            loadUnits();
        } else {
            new Alert(Alert.AlertType.ERROR, "Failed to issue unit.").showAndWait();
        }
    }

    private void setLocation() {
        BloodUnit unit = unitTable.getSelectionModel().getSelectedItem();
        if (unit == null) {
            new Alert(Alert.AlertType.WARNING, "Select a blood unit.").showAndWait();
            return;
        }
        TextInputDialog dlg = new TextInputDialog(unit.getStorageLocation() == null ? "" : unit.getStorageLocation());
        dlg.setTitle("Set Location");
        dlg.setHeaderText("Update storage location for Unit ID " + unit.getUnitId());
        dlg.setContentText("Location:");
        java.util.Optional<String> result = dlg.showAndWait();
        if (!result.isPresent()) {
            return;
        }
        String location = result.get().trim();
        if (location.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Location cannot be empty.").showAndWait();
            return;
        }
        if (unitDAO.updateLocation(unit.getUnitId(), location)) {
            new Alert(Alert.AlertType.INFORMATION, "Location updated.").showAndWait();
            loadUnits();
        } else {
            new Alert(Alert.AlertType.ERROR, "Failed to update location.").showAndWait();
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
                    setStyle(UIStyle.lightRowStyle(getIndex()));
                }
            }
        });
    }
}

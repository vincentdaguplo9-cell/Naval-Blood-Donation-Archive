package ui;

import dao.BloodUnitDAO;
import dao.InventoryTxDAO;
import dao.LabTestDAO;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.BloodUnit;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

// Lab testing window (V2) for releasing units from quarantine.
public class LabTestingFrame extends Stage {
    private static LabTestingFrame instance;

    private final BloodUnitDAO unitDAO;
    private final LabTestDAO testDAO;
    private final InventoryTxDAO invTxDAO;

    private final TableView<BloodUnit> unitTable;
    private final TextField testerIdField;
    private final ComboBox<String> hivCombo;
    private final ComboBox<String> hbvCombo;
    private final ComboBox<String> hcvCombo;
    private final ComboBox<String> syphilisCombo;
    private final ComboBox<String> malariaCombo;
    private final TextField remarksField;

    public LabTestingFrame() {
        unitDAO = new BloodUnitDAO();
        testDAO = new LabTestDAO();
        invTxDAO = new InventoryTxDAO();

        setTitle("Lab Testing");

        Button dashboardBtn = UIStyle.secondaryButton("Dashboard", "dashboard");
        dashboardBtn.setOnAction(e -> DashboardFrame.showWindow());
        HBox header = UIStyle.appHeader("Lab Testing", dashboardBtn);

        unitTable = new TableView<>();
        UIStyle.applyTableStyle(unitTable);
        unitTable.setPlaceholder(new Label("No pending units for testing."));
        configureUnitTable();

        loadPendingUnits();

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        UIStyle.applyCardStyle(form);

        int row = 0;
        form.add(UIStyle.formLabel("Tester ID *"), 0, row);
        testerIdField = new TextField("201");
        UIStyle.applyInputStyle(testerIdField);
        form.add(testerIdField, 1, row);

        row++;
        form.add(UIStyle.formLabel("HIV"), 0, row);
        hivCombo = resultCombo();
        form.add(hivCombo, 1, row);

        row++;
        form.add(UIStyle.formLabel("HBV"), 0, row);
        hbvCombo = resultCombo();
        form.add(hbvCombo, 1, row);

        row++;
        form.add(UIStyle.formLabel("HCV"), 0, row);
        hcvCombo = resultCombo();
        form.add(hcvCombo, 1, row);

        row++;
        form.add(UIStyle.formLabel("Syphilis"), 0, row);
        syphilisCombo = resultCombo();
        form.add(syphilisCombo, 1, row);

        row++;
        form.add(UIStyle.formLabel("Malaria"), 0, row);
        malariaCombo = resultCombo();
        form.add(malariaCombo, 1, row);

        row++;
        form.add(UIStyle.formLabel("Remarks"), 0, row);
        remarksField = new TextField();
        UIStyle.applyInputStyle(remarksField);
        remarksField.setPromptText("Optional notes...");
        form.add(remarksField, 1, row);

        Button saveBtn = UIStyle.primaryButton("Save Result", "lab");
        saveBtn.setDefaultButton(true);
        saveBtn.disableProperty().bind(Bindings.createBooleanBinding(
                () -> unitTable.getSelectionModel().getSelectedItem() == null
                        || !isPositiveInt(testerIdField.getText().trim()),
                unitTable.getSelectionModel().selectedItemProperty(),
                testerIdField.textProperty()
        ));
        saveBtn.setOnAction(e -> saveResult());

        Button refreshBtn = UIStyle.secondaryButton("Refresh", "refresh");
        refreshBtn.setOnAction(e -> loadPendingUnits());

        HBox buttons = new HBox(10, saveBtn, refreshBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(0, 12, 12, 12));

        VBox center = new VBox(12, unitTable, form);
        center.setPadding(new Insets(12));
        VBox.setVgrow(unitTable, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setStyle(UIStyle.pageBackground());
        root.setPadding(new Insets(0));
        root.setTop(header);
        root.setCenter(center);
        root.setBottom(buttons);

        Scene scene = new Scene(root, 980, 650);
        setScene(scene);
    }

    public static void showWindow() {
        if (instance == null) {
            instance = new LabTestingFrame();
            instance.setOnHidden(e -> instance = null);
        }
        instance.setIconified(false);
        instance.show();
        instance.toFront();
    }

    private ComboBox<String> resultCombo() {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll("NEG", "POS", "NA");
        combo.getSelectionModel().select("NEG");
        return combo;
    }

    private void configureUnitTable() {
        TableColumn<BloodUnit, Integer> idCol = new TableColumn<>("Unit ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("unitId"));

        TableColumn<BloodUnit, Integer> donorCol = new TableColumn<>("Donor ID");
        donorCol.setCellValueFactory(new PropertyValueFactory<>("donorId"));

        TableColumn<BloodUnit, String> bloodCol = new TableColumn<>("Blood Type");
        bloodCol.setCellValueFactory(new PropertyValueFactory<>("bloodType"));

        TableColumn<BloodUnit, String> compCol = new TableColumn<>("Component");
        compCol.setCellValueFactory(new PropertyValueFactory<>("component"));

        TableColumn<BloodUnit, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<BloodUnit, String> testCol = new TableColumn<>("Test");
        testCol.setCellValueFactory(new PropertyValueFactory<>("testStatus"));

        TableColumn<BloodUnit, String> locCol = new TableColumn<>("Location");
        locCol.setCellValueFactory(new PropertyValueFactory<>("storageLocation"));

        unitTable.getColumns().addAll(idCol, donorCol, bloodCol, compCol, statusCol, testCol, locCol);
    }

    private void loadPendingUnits() {
        // Most realistic view: pending tests (usually quarantine).
        List<BloodUnit> list = unitDAO.getUnitsFiltered("All", "PENDING");
        unitTable.getItems().setAll(list);
    }

    private void saveResult() {
        BloodUnit selected = unitTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Select a unit to save results.").showAndWait();
            return;
        }

        if (!isPositiveInt(testerIdField.getText().trim())) {
            new Alert(Alert.AlertType.WARNING, "Enter a valid Tester ID.").showAndWait();
            return;
        }
        int testerId = Integer.parseInt(testerIdField.getText().trim());

        String hiv = hivCombo.getValue();
        String hbv = hbvCombo.getValue();
        String hcv = hcvCombo.getValue();
        String syphilis = syphilisCombo.getValue();
        String malaria = malariaCombo.getValue();

        boolean hasPos = "POS".equalsIgnoreCase(hiv)
                || "POS".equalsIgnoreCase(hbv)
                || "POS".equalsIgnoreCase(hcv)
                || "POS".equalsIgnoreCase(syphilis)
                || "POS".equalsIgnoreCase(malaria);

        String overall = hasPos ? "FAIL" : "PASS";
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        boolean saved = testDAO.addTest(
                selected.getUnitId(),
                now,
                testerId,
                hiv, hbv, hcv, syphilis, malaria,
                overall,
                remarksField.getText().trim()
        );
        if (!saved) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to save lab test.");
            alert.setHeaderText("Error");
            alert.showAndWait();
            return;
        }

        String nextTestStatus = "PASS".equals(overall) ? "PASSED" : "FAILED";
        String nextStatus = "PASS".equals(overall) ? "AVAILABLE" : "DISCARDED";
        unitDAO.updateTestAndStatus(selected.getUnitId(), nextTestStatus, nextStatus);

        String txType = "PASS".equals(overall) ? "TEST_PASS" : "TEST_FAIL";
        invTxDAO.addTx(selected.getUnitId(), txType, now, testerId, null, remarksField.getText().trim());

        new Alert(Alert.AlertType.INFORMATION,
                "Saved. Unit is now " + nextStatus + " (" + nextTestStatus + ").").showAndWait();
        remarksField.setText("");
        loadPendingUnits();
    }

    private boolean isPositiveInt(String value) {
        try {
            return Integer.parseInt(value) > 0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}

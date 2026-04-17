package ui;

import dao.BloodUnitDAO;
import dao.DonorDAO;
import dao.DonationSessionDAO;
import dao.InventoryTxDAO;
import dao.ScreeningDAO;
import dao.TransactionDAO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.beans.binding.Bindings;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.BloodUnit;
import model.DonationTransaction;
import model.Donor;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// Form for recording a blood donation (JavaFX).
public class DonationForm extends Stage {
    private static DonationForm instance;
    private final ComboBox<DonorItem> donorCombo;
    private final TextField donorSearchField;
    private final TextField volumeField;
    private final TextField siteField;
    private final DatePicker collectionDatePicker;
    private final DatePicker expiryDatePicker;
    private final TextField staffIdField;
    private final TextField remarksField;
    private final TextField weightField;
    private final TextField bpSysField;
    private final TextField bpDiaField;
    private final TextField hgbField;
    private final TextField tempField;
    private final ComboBox<String> screeningResultCombo;
    private final TextField screeningReasonField;
    private final Label volumeError;
    private final Label staffError;
    private final Label dateError;
    private final Label screeningError;

    private final DonorDAO donorDAO;
    private final BloodUnitDAO unitDAO;
    private final TransactionDAO txDAO;
    private final DonationSessionDAO sessionDAO;
    private final ScreeningDAO screeningDAO;
    private final InventoryTxDAO invTxDAO;

    public DonationForm() {
        donorDAO = new DonorDAO();
        unitDAO = new BloodUnitDAO();
        txDAO = new TransactionDAO();
        sessionDAO = new DonationSessionDAO();
        screeningDAO = new ScreeningDAO();
        invTxDAO = new InventoryTxDAO();

        setTitle("Record Donation");

        GridPane formPanel = new GridPane();
        formPanel.setHgap(12);
        formPanel.setVgap(10);
        UIStyle.applyCardStyle(formPanel);

        int row = 0;
        formPanel.add(UIStyle.formLabel("Donor Search"), 0, row);
        donorSearchField = new TextField();
        UIStyle.applyInputStyle(donorSearchField);
        donorSearchField.setPromptText("Search name or blood type");
        formPanel.add(donorSearchField, 1, row);

        row++;
        formPanel.add(UIStyle.helperLabel("Start with a donor name or blood type to narrow the list quickly."), 1, row);

        row++;
        formPanel.add(UIStyle.formLabel("Donor *"), 0, row);
        donorCombo = new ComboBox<>();
        UIStyle.applyInputStyle(donorCombo);
        formPanel.add(donorCombo, 1, row);

        row++;
        formPanel.add(UIStyle.formLabel("Volume (ml) *"), 0, row);
        volumeField = new TextField();
        UIStyle.applyInputStyle(volumeField);
        volumeField.setPromptText("Common whole blood volume: 450");
        formPanel.add(volumeField, 1, row);

        row++;
        formPanel.add(UIStyle.helperLabel("Enter the collected volume in milliliters. Example: 450."), 1, row);

        row++;
        volumeError = errorLabel();
        formPanel.add(volumeError, 1, row);

        row++;
        formPanel.add(UIStyle.formLabel("Collection Date *"), 0, row);
        collectionDatePicker = new DatePicker(LocalDate.now());
        UIStyle.applyInputStyle(collectionDatePicker);
        formPanel.add(collectionDatePicker, 1, row);

        row++;
        formPanel.add(UIStyle.helperLabel("Defaults to today. Use the actual collection date for traceability."), 1, row);

        row++;
        formPanel.add(UIStyle.formLabel("Collection Site"), 0, row);
        siteField = new TextField("Naval RHU");
        UIStyle.applyInputStyle(siteField);
        siteField.setPromptText("e.g., Naval RHU / Blood Drive");
        formPanel.add(siteField, 1, row);

        row++;
        formPanel.add(UIStyle.formLabel("Expiry Date *"), 0, row);
        expiryDatePicker = new DatePicker(LocalDate.now().plusDays(35));
        UIStyle.applyInputStyle(expiryDatePicker);
        formPanel.add(expiryDatePicker, 1, row);

        row++;
        formPanel.add(UIStyle.helperLabel("For whole blood, 35 days from collection is the current default."), 1, row);

        row++;
        dateError = errorLabel();
        formPanel.add(dateError, 1, row);

        row++;
        formPanel.add(UIStyle.formLabel("Staff ID *"), 0, row);
        staffIdField = new TextField("201");
        UIStyle.applyInputStyle(staffIdField);
        formPanel.add(staffIdField, 1, row);

        row++;
        formPanel.add(UIStyle.helperLabel("Use the staff member responsible for collection or review."), 1, row);

        row++;
        staffError = errorLabel();
        formPanel.add(staffError, 1, row);

        row++;
        formPanel.add(UIStyle.formLabel("Remarks:"), 0, row);
        remarksField = new TextField();
        UIStyle.applyInputStyle(remarksField);
        remarksField.setPromptText("Optional notes...");
        formPanel.add(remarksField, 1, row);

        row++;
        formPanel.add(UIStyle.formLabel("Screening Result *"), 0, row);
        screeningResultCombo = new ComboBox<>();
        UIStyle.applyInputStyle(screeningResultCombo);
        screeningResultCombo.getItems().addAll("PASSED", "FAILED");
        screeningResultCombo.getSelectionModel().select("PASSED");
        formPanel.add(screeningResultCombo, 1, row);

        row++;
        formPanel.add(UIStyle.helperLabel("Choose FAILED only if screening blocks collection or release."), 1, row);

        row++;
        formPanel.add(UIStyle.formLabel("Failure Reason"), 0, row);
        screeningReasonField = new TextField();
        UIStyle.applyInputStyle(screeningReasonField);
        screeningReasonField.setPromptText("Required if FAILED");
        screeningReasonField.disableProperty().bind(screeningResultCombo.valueProperty().isEqualTo("PASSED"));
        formPanel.add(screeningReasonField, 1, row);

        row++;
        screeningError = errorLabel();
        formPanel.add(screeningError, 1, row);

        row++;
        formPanel.add(UIStyle.formLabel("Weight (kg)"), 0, row);
        weightField = new TextField();
        UIStyle.applyInputStyle(weightField);
        formPanel.add(weightField, 1, row);

        row++;
        formPanel.add(UIStyle.formLabel("BP (Sys/Dia)"), 0, row);
        HBox bpBox = new HBox(8);
        bpSysField = new TextField();
        bpDiaField = new TextField();
        UIStyle.applyInputStyle(bpSysField);
        UIStyle.applyInputStyle(bpDiaField);
        bpSysField.setPromptText("Systolic");
        bpDiaField.setPromptText("Diastolic");
        bpBox.getChildren().addAll(bpSysField, bpDiaField);
        formPanel.add(bpBox, 1, row);

        row++;
        formPanel.add(UIStyle.formLabel("Hemoglobin (g/dL)"), 0, row);
        hgbField = new TextField();
        UIStyle.applyInputStyle(hgbField);
        formPanel.add(hgbField, 1, row);

        row++;
        formPanel.add(UIStyle.formLabel("Temperature (°C)"), 0, row);
        tempField = new TextField();
        UIStyle.applyInputStyle(tempField);
        formPanel.add(tempField, 1, row);

        Button saveBtn = UIStyle.primaryButton("Record Donation", "bloodbag");
        saveBtn.setOnAction(e -> saveDonation());
        saveBtn.setDefaultButton(true);
        saveBtn.disableProperty().bind(Bindings.createBooleanBinding(
                () -> donorCombo.getSelectionModel().getSelectedItem() == null
                        || volumeField.getText().trim().isEmpty()
                        || !isPositiveInt(volumeField.getText().trim())
                        || staffIdField.getText().trim().isEmpty()
                        || !isPositiveInt(staffIdField.getText().trim())
                        || collectionDatePicker.getValue() == null
                        || expiryDatePicker.getValue() == null
                        || screeningResultCombo.getSelectionModel().getSelectedItem() == null,
                donorCombo.valueProperty(),
                volumeField.textProperty(),
                staffIdField.textProperty(),
                collectionDatePicker.valueProperty(),
                expiryDatePicker.valueProperty(),
                screeningResultCombo.valueProperty()
        ));

        HBox btnPanel = new HBox(saveBtn);
        btnPanel.setAlignment(Pos.CENTER);
        btnPanel.setPadding(new Insets(0, 12, 12, 12));

        Button dashboardBtn = UIStyle.secondaryButton("Dashboard", "dashboard");
        dashboardBtn.setOnAction(e -> DashboardFrame.showWindow());
        HBox header = UIStyle.appHeader("Record Donation", dashboardBtn);

        BorderPane root = new BorderPane();
        root.setStyle(UIStyle.pageBackground());
        root.setPadding(new Insets(0));
        root.setTop(header);

        ScrollPane scrollPane = new ScrollPane(formPanel);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox centerWrap = new VBox(scrollPane);
        centerWrap.setPadding(new Insets(12));
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        root.setCenter(centerWrap);
        root.setBottom(btnPanel);

        Scene scene = new Scene(root, 520, 470);
        setScene(scene);

        loadDonors("");
        setupValidation();
        donorSearchField.textProperty().addListener((obs, oldVal, newVal) -> loadDonors(newVal.trim()));
    }

    private Label errorLabel() {
        return UIStyle.errorLabel();
    }

    private void setupValidation() {
        volumeField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                if (!isPositiveInt(volumeField.getText().trim())) {
                    volumeError.setText("Enter a valid volume.");
                } else {
                    volumeError.setText("");
                }
            }
        });

        staffIdField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                if (!isPositiveInt(staffIdField.getText().trim())) {
                    staffError.setText("Enter a valid staff ID.");
                } else {
                    staffError.setText("");
                }
            }
        });

        collectionDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateDates());
        expiryDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateDates());
    }

    private void validateDates() {
        if (collectionDatePicker.getValue() == null || expiryDatePicker.getValue() == null) {
            dateError.setText("Select collection and expiry dates.");
            return;
        }
        if (expiryDatePicker.getValue().isBefore(collectionDatePicker.getValue())) {
            dateError.setText("Expiry date cannot be before collection date.");
        } else {
            dateError.setText("");
        }
    }

    private boolean isPositiveInt(String value) {
        try {
            return Integer.parseInt(value) > 0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public static void showWindow() {
        if (instance == null) {
            instance = new DonationForm();
            instance.setOnHidden(e -> instance = null);
        }
        instance.setIconified(false);
        instance.show();
        instance.toFront();
    }

    private void loadDonors(String keyword) {
        donorCombo.getItems().clear();
        List<Donor> donors = (keyword == null || keyword.isEmpty())
                ? donorDAO.getAllDonors()
                : donorDAO.searchDonors(keyword);
        for (Donor d : donors) {
            donorCombo.getItems().add(new DonorItem(d));
        }
        if (!donorCombo.getItems().isEmpty()) {
            donorCombo.getSelectionModel().select(0);
        }
    }

    private void saveDonation() {
        DonorItem selected = donorCombo.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "No donors available.").showAndWait();
            return;
        }

        if (!"Eligible".equalsIgnoreCase(selected.eligibilityStatus)) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Selected donor is not eligible.");
            alert.setHeaderText("Eligibility");
            alert.showAndWait();
            return;
        }

        screeningError.setText("");
        boolean screeningPassed = "PASSED".equalsIgnoreCase(screeningResultCombo.getSelectionModel().getSelectedItem());
        if (!screeningPassed && screeningReasonField.getText().trim().isEmpty()) {
            screeningError.setText("Provide a failure reason if screening FAILED.");
            return;
        }

        int volume;
        if (!isPositiveInt(volumeField.getText().trim())) {
            volumeError.setText("Enter a valid volume.");
            return;
        }
        volume = Integer.parseInt(volumeField.getText().trim());

        LocalDate collectionLocal = collectionDatePicker.getValue();
        LocalDate expiryLocal = expiryDatePicker.getValue();
        if (collectionLocal == null || expiryLocal == null) {
            dateError.setText("Select collection and expiry dates.");
            return;
        }
        if (expiryLocal.isBefore(collectionLocal)) {
            dateError.setText("Expiry date cannot be before collection date.");
            return;
        }
        Date collectionDate = Date.valueOf(collectionLocal);
        Date expiryDate = Date.valueOf(expiryLocal);

        int staffId;
        if (!isPositiveInt(staffIdField.getText().trim())) {
            staffError.setText("Enter a valid staff ID.");
            return;
        }
        staffId = Integer.parseInt(staffIdField.getText().trim());

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        int sessionId = sessionDAO.createSession(selected.donorId, now, staffId, siteField.getText().trim(), remarksField.getText().trim());
        if (sessionId < 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to create donation session.");
            alert.setHeaderText("Error");
            alert.showAndWait();
            return;
        }

        Double weight = parseDoubleOrNull(weightField.getText());
        Integer bpSys = parseIntOrNull(bpSysField.getText());
        Integer bpDia = parseIntOrNull(bpDiaField.getText());
        Double hgb = parseDoubleOrNull(hgbField.getText());
        Double temp = parseDoubleOrNull(tempField.getText());
        screeningDAO.addScreening(sessionId, weight, bpSys, bpDia, hgb, temp, screeningPassed,
                screeningPassed ? null : screeningReasonField.getText().trim());

        if (!screeningPassed) {
            new Alert(Alert.AlertType.INFORMATION, "Screening failed. No blood unit collected.").showAndWait();
            close();
            return;
        }

        BloodUnit unit = new BloodUnit(
                0,
                selected.donorId,
                selected.bloodType,
                volume,
                collectionDate,
                expiryDate,
                "QUARANTINE",
                sessionId,
                null,
                "WB",
                "PENDING",
                "Quarantine - Main Fridge"
        );
        int unitId = unitDAO.addAndReturnId(unit);
        if (unitId < 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to create blood unit.");
            alert.setHeaderText("Error");
            alert.showAndWait();
            return;
        }

        // Traceability log (V2).
        invTxDAO.addTx(unitId, "COLLECT", now, staffId, null, remarksField.getText().trim());

        // Legacy transaction table (for existing dashboard/reports).
        DonationTransaction tx = new DonationTransaction(0, selected.donorId, unitId, staffId, now, remarksField.getText().trim());
        txDAO.addTransaction(tx);

        // Realistic donor deferral for whole blood (school approximation): 90 days.
        java.sql.Date deferredUntil = Date.valueOf(collectionLocal.plusDays(90));
        donorDAO.updateDonationStatusV2(selected.donorId, collectionDate, deferredUntil, "Ineligible");

        new Alert(Alert.AlertType.INFORMATION, "Donation recorded. Unit is in QUARANTINE pending tests.").showAndWait();
        close();
    }

    private Double parseDoubleOrNull(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer parseIntOrNull(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    // Simple combo item wrapper.
    private static class DonorItem {
        private final int donorId;
        private final String name;
        private final String bloodType;
        private final String eligibilityStatus;

        DonorItem(Donor donor) {
            this.donorId = donor.getDonorId();
            this.name = donor.getFirstName() + " " + donor.getLastName();
            this.bloodType = donor.getBloodType();
            this.eligibilityStatus = donor.getEligibilityStatus();
        }

        @Override
        public String toString() {
            return donorId + " - " + name + " (" + bloodType + ")";
        }
    }
}

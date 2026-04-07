package ui;

import dao.BloodUnitDAO;
import dao.DonorDAO;
import dao.TransactionDAO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.beans.binding.Bindings;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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
    private final DatePicker collectionDatePicker;
    private final DatePicker expiryDatePicker;
    private final TextField staffIdField;
    private final TextField remarksField;
    private final Label volumeError;
    private final Label staffError;
    private final Label dateError;

    private final DonorDAO donorDAO;
    private final BloodUnitDAO unitDAO;
    private final TransactionDAO txDAO;

    public DonationForm() {
        donorDAO = new DonorDAO();
        unitDAO = new BloodUnitDAO();
        txDAO = new TransactionDAO();

        setTitle("Record Donation");

        GridPane formPanel = new GridPane();
        formPanel.setHgap(12);
        formPanel.setVgap(10);
        UIStyle.applyCardStyle(formPanel);

        int row = 0;
        formPanel.add(new Label("Donor Search"), 0, row);
        donorSearchField = new TextField();
        UIStyle.applyInputStyle(donorSearchField);
        formPanel.add(donorSearchField, 1, row);

        row++;
        formPanel.add(new Label("Donor *"), 0, row);
        donorCombo = new ComboBox<>();
        formPanel.add(donorCombo, 1, row);

        row++;
        formPanel.add(new Label("Volume (ml) *"), 0, row);
        volumeField = new TextField();
        UIStyle.applyInputStyle(volumeField);
        formPanel.add(volumeField, 1, row);

        row++;
        volumeError = errorLabel();
        formPanel.add(volumeError, 1, row);

        row++;
        formPanel.add(new Label("Collection Date *"), 0, row);
        collectionDatePicker = new DatePicker(LocalDate.now());
        formPanel.add(collectionDatePicker, 1, row);

        row++;
        formPanel.add(new Label("Expiry Date *"), 0, row);
        expiryDatePicker = new DatePicker(LocalDate.now().plusDays(35));
        formPanel.add(expiryDatePicker, 1, row);

        row++;
        dateError = errorLabel();
        formPanel.add(dateError, 1, row);

        row++;
        formPanel.add(new Label("Staff ID *"), 0, row);
        staffIdField = new TextField("1");
        UIStyle.applyInputStyle(staffIdField);
        formPanel.add(staffIdField, 1, row);

        row++;
        staffError = errorLabel();
        formPanel.add(staffError, 1, row);

        row++;
        formPanel.add(new Label("Remarks:"), 0, row);
        remarksField = new TextField();
        UIStyle.applyInputStyle(remarksField);
        formPanel.add(remarksField, 1, row);

        Button saveBtn = UIStyle.primaryButton("Record Donation");
        saveBtn.setOnAction(e -> saveDonation());
        saveBtn.setDefaultButton(true);
        saveBtn.disableProperty().bind(Bindings.createBooleanBinding(
                () -> donorCombo.getSelectionModel().getSelectedItem() == null
                        || volumeField.getText().trim().isEmpty()
                        || !isPositiveInt(volumeField.getText().trim())
                        || staffIdField.getText().trim().isEmpty()
                        || !isPositiveInt(staffIdField.getText().trim())
                        || collectionDatePicker.getValue() == null
                        || expiryDatePicker.getValue() == null,
                donorCombo.valueProperty(),
                volumeField.textProperty(),
                staffIdField.textProperty(),
                collectionDatePicker.valueProperty(),
                expiryDatePicker.valueProperty()
        ));

        HBox btnPanel = new HBox(saveBtn);
        btnPanel.setAlignment(Pos.CENTER);
        btnPanel.setPadding(new Insets(6));

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + UIStyle.BG + ";");
        root.setPadding(new Insets(12));
        root.setCenter(formPanel);
        root.setBottom(btnPanel);

        Scene scene = new Scene(root, 520, 470);
        setScene(scene);

        loadDonors("");
        setupValidation();
        donorSearchField.textProperty().addListener((obs, oldVal, newVal) -> loadDonors(newVal.trim()));
    }

    private Label errorLabel() {
        Label label = new Label("");
        label.setTextFill(javafx.scene.paint.Color.web("#c0392b"));
        label.setStyle("-fx-font-size: 11px;");
        return label;
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

        BloodUnit unit = new BloodUnit(0, selected.donorId, selected.bloodType, volume, collectionDate, expiryDate, "Available");
        int unitId = unitDAO.addAndReturnId(unit);
        if (unitId < 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to create blood unit.");
            alert.setHeaderText("Error");
            alert.showAndWait();
            return;
        }

        DonationTransaction tx = new DonationTransaction(0, selected.donorId, unitId, staffId,
                Timestamp.valueOf(LocalDateTime.now()), remarksField.getText().trim());

        if (txDAO.addTransaction(tx)) {
            donorDAO.updateDonationStatus(selected.donorId, collectionDate, "Ineligible");
            new Alert(Alert.AlertType.INFORMATION, "Donation recorded successfully.").showAndWait();
            close();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to record donation.");
            alert.setHeaderText("Error");
            alert.showAndWait();
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

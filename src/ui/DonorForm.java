package ui;

import dao.DonorDAO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.beans.binding.Bindings;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.Donor;

import java.sql.Date;
import java.time.LocalDate;

// Form for adding or editing donors (JavaFX).
public class DonorForm extends Stage {
    private static DonorForm instance;
    private static Runnable onCloseRefresh;
    private final TextField firstNameField;
    private final TextField lastNameField;
    private final ComboBox<String> bloodTypeCombo;
    private final TextField contactField;
    private final TextField addressField;
    private final DatePicker lastDonationPicker;
    private final CheckBox noLastDonationCheck;
    private final ComboBox<String> eligibilityCombo;
    private final Label firstNameError;
    private final Label lastNameError;
    private final Label bloodError;
    private final Label eligibilityError;

    private final DonorDAO donorDAO;
    private final boolean editing;
    private final int donorId;

    public DonorForm() {
        this(null);
    }

    public DonorForm(Donor donor) {
        donorDAO = new DonorDAO();
        editing = donor != null;
        donorId = donor != null ? donor.getDonorId() : 0;

        setTitle(editing ? "Edit Donor" : "Add Donor");

        GridPane formPanel = new GridPane();
        formPanel.setHgap(12);
        formPanel.setVgap(10);
        UIStyle.applyCardStyle(formPanel);

        int row = 0;
        formPanel.add(new Label("First Name *"), 0, row);
        firstNameField = new TextField();
        UIStyle.applyInputStyle(firstNameField);
        formPanel.add(firstNameField, 1, row);

        row++;
        firstNameError = errorLabel();
        formPanel.add(firstNameError, 1, row);

        row++;
        formPanel.add(new Label("Last Name *"), 0, row);
        lastNameField = new TextField();
        UIStyle.applyInputStyle(lastNameField);
        formPanel.add(lastNameField, 1, row);

        row++;
        lastNameError = errorLabel();
        formPanel.add(lastNameError, 1, row);

        row++;
        formPanel.add(new Label("Blood Type *"), 0, row);
        bloodTypeCombo = new ComboBox<>();
        bloodTypeCombo.getItems().addAll("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
        formPanel.add(bloodTypeCombo, 1, row);

        row++;
        bloodError = errorLabel();
        formPanel.add(bloodError, 1, row);

        row++;
        formPanel.add(new Label("Contact No:"), 0, row);
        contactField = new TextField();
        UIStyle.applyInputStyle(contactField);
        formPanel.add(contactField, 1, row);

        row++;
        formPanel.add(new Label("Address:"), 0, row);
        addressField = new TextField();
        UIStyle.applyInputStyle(addressField);
        formPanel.add(addressField, 1, row);

        row++;
        formPanel.add(new Label("Last Donation Date:"), 0, row);
        lastDonationPicker = new DatePicker();
        formPanel.add(lastDonationPicker, 1, row);

        row++;
        noLastDonationCheck = new CheckBox("Not set");
        formPanel.add(noLastDonationCheck, 1, row);

        row++;
        formPanel.add(new Label("Eligibility Status *"), 0, row);
        eligibilityCombo = new ComboBox<>();
        eligibilityCombo.getItems().addAll("Eligible", "Ineligible");
        formPanel.add(eligibilityCombo, 1, row);

        row++;
        eligibilityError = errorLabel();
        formPanel.add(eligibilityError, 1, row);

        noLastDonationCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            lastDonationPicker.setDisable(newVal);
            if (newVal) {
                lastDonationPicker.setValue(null);
            }
        });

        Button saveBtn = UIStyle.primaryButton(editing ? "Update" : "Save");
        saveBtn.setOnAction(e -> saveDonor());
        saveBtn.setDefaultButton(true);
        saveBtn.disableProperty().bind(Bindings.createBooleanBinding(
                () -> firstNameField.getText().trim().isEmpty()
                        || lastNameField.getText().trim().isEmpty()
                        || bloodTypeCombo.getSelectionModel().getSelectedItem() == null
                        || eligibilityCombo.getSelectionModel().getSelectedItem() == null,
                firstNameField.textProperty(),
                lastNameField.textProperty(),
                bloodTypeCombo.valueProperty(),
                eligibilityCombo.valueProperty()
        ));

        HBox btnPanel = new HBox(saveBtn);
        btnPanel.setAlignment(Pos.CENTER);
        btnPanel.setPadding(new Insets(6));

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + UIStyle.BG + ";");
        root.setPadding(new Insets(12));
        root.setCenter(formPanel);
        root.setBottom(btnPanel);

        Scene scene = new Scene(root, 500, 440);
        setScene(scene);

        if (editing) {
            populateForm(donor);
        }

        setupValidation();
    }

    private Label errorLabel() {
        Label label = new Label("");
        label.setTextFill(javafx.scene.paint.Color.web("#c0392b"));
        label.setStyle("-fx-font-size: 11px;");
        return label;
    }

    private void setupValidation() {
        firstNameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && firstNameField.getText().trim().isEmpty()) {
                firstNameError.setText("First name is required.");
            } else {
                firstNameError.setText("");
            }
        });

        lastNameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && lastNameField.getText().trim().isEmpty()) {
                lastNameError.setText("Last name is required.");
            } else {
                lastNameError.setText("");
            }
        });

        bloodTypeCombo.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && bloodTypeCombo.getSelectionModel().getSelectedItem() == null) {
                bloodError.setText("Select a blood type.");
            } else {
                bloodError.setText("");
            }
        });

        eligibilityCombo.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && eligibilityCombo.getSelectionModel().getSelectedItem() == null) {
                eligibilityError.setText("Select eligibility.");
            } else {
                eligibilityError.setText("");
            }
        });
    }

    public static void showForm(Donor donor) {
        if (instance != null) {
            instance.close();
        }
        instance = new DonorForm(donor);
        instance.setOnHidden(e -> {
            instance = null;
            if (onCloseRefresh != null) {
                onCloseRefresh.run();
            }
        });
        instance.show();
        instance.toFront();
    }

    public static void setOnCloseRefresh(Runnable refreshAction) {
        onCloseRefresh = refreshAction;
    }

    private void populateForm(Donor donor) {
        firstNameField.setText(donor.getFirstName());
        lastNameField.setText(donor.getLastName());
        bloodTypeCombo.getSelectionModel().select(donor.getBloodType());
        contactField.setText(donor.getContactNo());
        addressField.setText(donor.getAddress());
        if (donor.getLastDonationDate() != null) {
            lastDonationPicker.setValue(donor.getLastDonationDate().toLocalDate());
        } else {
            noLastDonationCheck.setSelected(true);
        }
        eligibilityCombo.getSelectionModel().select(donor.getEligibilityStatus());
    }

    private void saveDonor() {
        String first = firstNameField.getText().trim();
        String last = lastNameField.getText().trim();
        String blood = bloodTypeCombo.getSelectionModel().getSelectedItem();
        String contact = contactField.getText().trim();
        String address = addressField.getText().trim();
        String eligibility = eligibilityCombo.getSelectionModel().getSelectedItem();

        boolean valid = true;
        if (first.isEmpty()) {
            firstNameError.setText("First name is required.");
            valid = false;
        }
        if (last.isEmpty()) {
            lastNameError.setText("Last name is required.");
            valid = false;
        }
        if (blood == null) {
            bloodError.setText("Select a blood type.");
            valid = false;
        }
        if (eligibility == null) {
            eligibilityError.setText("Select eligibility.");
            valid = false;
        }
        if (!valid) {
            return;
        }

        Date lastDonation = null;
        if (!noLastDonationCheck.isSelected()) {
            LocalDate picked = lastDonationPicker.getValue();
            if (picked != null) {
                lastDonation = Date.valueOf(picked);
            }
        }

        Donor donor = new Donor(donorId, first, last, blood, contact, address, lastDonation, eligibility);
        boolean success = editing ? donorDAO.updateDonor(donor) : donorDAO.addDonor(donor);

        if (success) {
            new Alert(Alert.AlertType.INFORMATION, "Donor saved successfully.").showAndWait();
            close();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to save donor.");
            alert.setHeaderText("Error");
            alert.showAndWait();
        }
    }
}

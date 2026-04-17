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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextField;
import javafx.beans.binding.Bindings;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import model.Donor;

import java.sql.Date;
import java.time.LocalDate;
import java.util.function.UnaryOperator;

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
    private final Label contactError;
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
        formPanel.add(UIStyle.formLabel("First Name *"), 0, row);
        firstNameField = new TextField();
        UIStyle.applyInputStyle(firstNameField);
        firstNameField.setPromptText("Given name");
        formPanel.add(firstNameField, 1, row);

        row++;
        firstNameError = errorLabel();
        formPanel.add(firstNameError, 1, row);

        row++;
        formPanel.add(UIStyle.formLabel("Last Name *"), 0, row);
        lastNameField = new TextField();
        UIStyle.applyInputStyle(lastNameField);
        lastNameField.setPromptText("Family name");
        formPanel.add(lastNameField, 1, row);

        row++;
        lastNameError = errorLabel();
        formPanel.add(lastNameError, 1, row);

        row++;
        formPanel.add(UIStyle.formLabel("Blood Type *"), 0, row);
        bloodTypeCombo = new ComboBox<>();
        bloodTypeCombo.getItems().addAll("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
        UIStyle.applyInputStyle(bloodTypeCombo);
        formPanel.add(bloodTypeCombo, 1, row);

        row++;
        bloodError = errorLabel();
        formPanel.add(bloodError, 1, row);

        row++;
        formPanel.add(UIStyle.formLabel("Contact No:"), 0, row);
        contactField = new TextField();
        UIStyle.applyInputStyle(contactField);
        formPanel.add(contactField, 1, row);

        UnaryOperator<TextFormatter.Change> phoneFilter = change -> {
            String proposed = change.getControlNewText();
            String digits = proposed == null ? "" : proposed.replaceAll("\\D", "");

            if (digits.startsWith("63") && digits.length() <= 12) {
                digits = "0" + digits.substring(2);
            } else if (digits.startsWith("9") && digits.length() <= 10) {
                digits = "0" + digits;
            }

            // Always keep the PH mobile prefix present in the field.
            if (digits.length() < 2) {
                digits = "09";
            } else {
                if (digits.charAt(0) != '0' || digits.charAt(1) != '9') {
                    return null;
                }
            }

            if (digits.length() > 11) {
                return null;
            }

            String formatted = formatContactDigits(digits);
            String current = change.getControlText();
            if (formatted.equals(current)) {
                return change;
            }

            change.setRange(0, current.length());
            change.setText(formatted);
            change.setCaretPosition(formatted.length());
            change.setAnchor(formatted.length());
            return change;
        };
        contactField.setTextFormatter(new TextFormatter<>(phoneFilter));
        contactField.setText("09");

        row++;
        formPanel.add(UIStyle.helperLabel("Philippine mobile format is applied automatically, for example 0917-402-1101."), 1, row);

        row++;
        contactError = errorLabel();
        formPanel.add(contactError, 1, row);

        row++;
        formPanel.add(UIStyle.formLabel("Address:"), 0, row);
        addressField = new TextField();
        UIStyle.applyInputStyle(addressField);
        addressField.setPromptText("Barangay, town, or full address");
        formPanel.add(addressField, 1, row);

        row++;
        formPanel.add(UIStyle.formLabel("Last Donation Date:"), 0, row);
        lastDonationPicker = new DatePicker();
        UIStyle.applyInputStyle(lastDonationPicker);
        formPanel.add(lastDonationPicker, 1, row);

        row++;
        noLastDonationCheck = new CheckBox("Not set");
        formPanel.add(noLastDonationCheck, 1, row);

        row++;
        formPanel.add(UIStyle.helperLabel("Leave this unset for first-time donors or when no history is available."), 1, row);

        row++;
        formPanel.add(UIStyle.formLabel("Eligibility Status *"), 0, row);
        eligibilityCombo = new ComboBox<>();
        eligibilityCombo.getItems().addAll("Eligible", "Ineligible");
        UIStyle.applyInputStyle(eligibilityCombo);
        formPanel.add(eligibilityCombo, 1, row);

        row++;
        formPanel.add(UIStyle.helperLabel("Use Eligible when the donor can proceed to collection today."), 1, row);

        row++;
        eligibilityError = errorLabel();
        formPanel.add(eligibilityError, 1, row);

        noLastDonationCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            lastDonationPicker.setDisable(newVal);
            if (newVal) {
                lastDonationPicker.setValue(null);
            }
        });

        Button saveBtn = UIStyle.primaryButton(editing ? "Update" : "Save", "donor");
        saveBtn.setOnAction(e -> saveDonor());
        saveBtn.setDefaultButton(true);
        saveBtn.disableProperty().bind(Bindings.createBooleanBinding(
                () -> firstNameField.getText().trim().isEmpty()
                        || lastNameField.getText().trim().isEmpty()
                        || bloodTypeCombo.getSelectionModel().getSelectedItem() == null
                        || normalizeContactNo(contactField.getText()) == null
                        || eligibilityCombo.getSelectionModel().getSelectedItem() == null,
                firstNameField.textProperty(),
                lastNameField.textProperty(),
                bloodTypeCombo.valueProperty(),
                contactField.textProperty(),
                eligibilityCombo.valueProperty()
        ));

        HBox btnPanel = new HBox(saveBtn);
        btnPanel.setAlignment(Pos.CENTER);
        btnPanel.setPadding(new Insets(0, 12, 12, 12));

        Button dashboardBtn = UIStyle.secondaryButton("Dashboard", "dashboard");
        dashboardBtn.setOnAction(e -> DashboardFrame.showWindow());
        HBox header = UIStyle.appHeader(editing ? "Edit Donor" : "Add Donor", dashboardBtn);

        BorderPane root = new BorderPane();
        root.setStyle(UIStyle.pageBackground());
        root.setPadding(new Insets(0));
        root.setTop(header);
        ScrollPane scrollPane = new ScrollPane(formPanel);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        javafx.scene.layout.VBox centerWrap = new javafx.scene.layout.VBox(scrollPane);
        centerWrap.setPadding(new Insets(12));
        root.setCenter(centerWrap);
        root.setBottom(btnPanel);

        Scene scene = new Scene(root, 500, 440);
        setScene(scene);

        if (editing) {
            populateForm(donor);
        }

        setupValidation();
    }

    private Label errorLabel() {
        return UIStyle.errorLabel();
    }

    private void setupValidation() {
        firstNameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                String formatted = formatPersonName(firstNameField.getText());
                firstNameField.setText(formatted);
                if (formatted.isEmpty()) {
                    firstNameError.setText("First name is required.");
                } else {
                    firstNameError.setText("");
                }
            }
        });

        lastNameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                String formatted = formatPersonName(lastNameField.getText());
                lastNameField.setText(formatted);
                if (formatted.isEmpty()) {
                    lastNameError.setText("Last name is required.");
                } else {
                    lastNameError.setText("");
                }
            }
        });

        bloodTypeCombo.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && bloodTypeCombo.getSelectionModel().getSelectedItem() == null) {
                bloodError.setText("Select a blood type.");
            } else {
                bloodError.setText("");
            }
        });

        contactField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                String normalized = normalizeContactNo(contactField.getText());
                if (normalized == null) {
                    contactError.setText("Contact number must be 11 digits (e.g., 0917-402-1101).");
                } else {
                    contactField.setText(normalized);
                    contactError.setText("");
                }
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
        instance.setIconified(false);
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
        String first = formatPersonName(firstNameField.getText());
        String last = formatPersonName(lastNameField.getText());
        String blood = bloodTypeCombo.getSelectionModel().getSelectedItem();
        String contact = normalizeContactNo(contactField.getText());
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
        if (contact == null) {
            contactError.setText("Contact number must be 11 digits (e.g., 0917-402-1101).");
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

    private static String formatPersonName(String raw) {
        if (raw == null) {
            return "";
        }
        String cleaned = raw.trim().replaceAll("\\s+", " ");
        if (cleaned.isEmpty()) {
            return "";
        }

        String lower = cleaned.toLowerCase();
        StringBuilder out = new StringBuilder(lower.length());
        boolean capitalizeNext = true;
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            if (capitalizeNext && Character.isLetter(c)) {
                out.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            }
            out.append(c);
            if (c == ' ' || c == '-' || c == '\'') {
                capitalizeNext = true;
            }
        }
        return out.toString();
    }

    // Normalizes to donors.sql-style phone formatting: ####-###-#### (11 digits).
    private static String normalizeContactNo(String raw) {
        if (raw == null) {
            return null;
        }
        String digits = raw.replaceAll("\\D", "");
        if (digits.startsWith("63") && digits.length() == 12) {
            digits = "0" + digits.substring(2);
        } else if (digits.startsWith("9") && digits.length() == 10) {
            digits = "0" + digits;
        }

        if (digits.length() != 11 || !digits.startsWith("09")) {
            return null;
        }

        return digits.substring(0, 4) + "-" + digits.substring(4, 7) + "-" + digits.substring(7);
    }

    private static String formatContactDigits(String digits) {
        if (digits == null || digits.isEmpty()) {
            return "";
        }
        String cleaned = digits.replaceAll("\\D", "");
        if (cleaned.length() <= 4) {
            return cleaned;
        }
        if (cleaned.length() <= 7) {
            return cleaned.substring(0, 4) + "-" + cleaned.substring(4);
        }
        return cleaned.substring(0, 4) + "-" + cleaned.substring(4, 7) + "-" + cleaned.substring(7);
    }
}

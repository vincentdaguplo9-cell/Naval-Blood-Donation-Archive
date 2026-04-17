package ui;

import dao.AdminDAO;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import util.AdminSession;

// Admin profile screen for password changes and staff account creation.
public class AdminProfileFrame extends Stage {
    private static AdminProfileFrame instance;

    private final AdminDAO adminDAO;
    private final PasswordField currentPasswordField;
    private final PasswordField newPasswordField;
    private final PasswordField confirmPasswordField;
    private final TextField staffIdField;
    private final PasswordField staffPasswordField;
    private final Label passwordStatusLabel;
    private final Label staffStatusLabel;
    private final TableView<AdminDAO.StaffAccount> staffTable;

    public AdminProfileFrame() {
        adminDAO = new AdminDAO();

        setTitle("Admin Profile");

        Button dashboardBtn = UIStyle.secondaryButton("Dashboard", "dashboard");
        dashboardBtn.setOnAction(e -> DashboardFrame.showWindow());
        HBox header = UIStyle.appHeader("Admin Profile", dashboardBtn);

        VBox summaryCard = new VBox(8);
        UIStyle.applyCardStyle(summaryCard);
        Label profileTitle = UIStyle.titleLabel("Administrator Profile");
        Label profileInfo = UIStyle.helperLabel("Signed in as `" + AdminSession.getUsername() + "`. Manage admin credentials and create staff accounts from here.");
        summaryCard.getChildren().addAll(profileTitle, profileInfo);

        GridPane passwordPanel = new GridPane();
        passwordPanel.setHgap(10);
        passwordPanel.setVgap(10);
        UIStyle.applyCardStyle(passwordPanel);
        addSingleColumn(passwordPanel);

        int row = 0;
        passwordPanel.add(UIStyle.formLabel("Current Password"), 0, row++);
        currentPasswordField = new PasswordField();
        UIStyle.applyInputStyle(currentPasswordField);
        passwordPanel.add(currentPasswordField, 0, row++);

        passwordPanel.add(UIStyle.formLabel("New Password"), 0, row++);
        newPasswordField = new PasswordField();
        UIStyle.applyInputStyle(newPasswordField);
        passwordPanel.add(newPasswordField, 0, row++);

        passwordPanel.add(UIStyle.formLabel("Confirm New Password"), 0, row++);
        confirmPasswordField = new PasswordField();
        UIStyle.applyInputStyle(confirmPasswordField);
        passwordPanel.add(confirmPasswordField, 0, row++);

        passwordStatusLabel = UIStyle.errorLabel();
        passwordPanel.add(passwordStatusLabel, 0, row++);

        Button updatePasswordBtn = UIStyle.primaryButton("Update Password", "login");
        updatePasswordBtn.setMaxWidth(Double.MAX_VALUE);
        updatePasswordBtn.disableProperty().bind(Bindings.createBooleanBinding(
                () -> currentPasswordField.getText().trim().isEmpty()
                        || newPasswordField.getText().trim().isEmpty()
                        || confirmPasswordField.getText().trim().isEmpty(),
                currentPasswordField.textProperty(),
                newPasswordField.textProperty(),
                confirmPasswordField.textProperty()
        ));
        updatePasswordBtn.setOnAction(e -> updatePassword());
        passwordPanel.add(updatePasswordBtn, 0, row);

        GridPane staffPanel = new GridPane();
        staffPanel.setHgap(10);
        staffPanel.setVgap(10);
        UIStyle.applyCardStyle(staffPanel);
        addSingleColumn(staffPanel);

        int staffRow = 0;
        staffPanel.add(UIStyle.formLabel("Staff ID"), 0, staffRow++);
        staffIdField = new TextField();
        UIStyle.applyInputStyle(staffIdField);
        staffIdField.setPromptText("e.g. 206");
        staffPanel.add(staffIdField, 0, staffRow++);

        staffPanel.add(UIStyle.formLabel("Staff Password"), 0, staffRow++);
        staffPasswordField = new PasswordField();
        UIStyle.applyInputStyle(staffPasswordField);
        staffPanel.add(staffPasswordField, 0, staffRow++);

        staffStatusLabel = UIStyle.errorLabel();
        staffPanel.add(staffStatusLabel, 0, staffRow++);

        Button createStaffBtn = UIStyle.primaryButton("Create Staff Account", "donor");
        createStaffBtn.setMaxWidth(Double.MAX_VALUE);
        createStaffBtn.disableProperty().bind(Bindings.createBooleanBinding(
                () -> staffIdField.getText().trim().isEmpty() || staffPasswordField.getText().trim().isEmpty(),
                staffIdField.textProperty(),
                staffPasswordField.textProperty()
        ));
        createStaffBtn.setOnAction(e -> createStaffAccount());
        staffPanel.add(createStaffBtn, 0, staffRow);

        HBox adminTools = new HBox(12, passwordPanel, staffPanel);
        adminTools.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(passwordPanel, Priority.ALWAYS);
        HBox.setHgrow(staffPanel, Priority.ALWAYS);

        staffTable = new TableView<>();
        UIStyle.applyTableStyle(staffTable);
        configureStaffTable();
        staffTable.setPlaceholder(UIStyle.helperLabel("No staff accounts found."));

        VBox tableBox = new VBox(10, UIStyle.subtitleLabel("Staff Accounts"), staffTable);
        UIStyle.applyPanelStyle(tableBox);
        tableBox.setPadding(new Insets(16));
        VBox.setVgrow(staffTable, Priority.ALWAYS);

        VBox center = new VBox(12, summaryCard, adminTools, tableBox);
        center.setPadding(new Insets(12));
        VBox.setVgrow(tableBox, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setStyle(UIStyle.pageBackground());
        root.setTop(header);
        root.setCenter(center);

        Scene scene = new Scene(root, 1050, 680);
        setScene(scene);
        setMaximized(true);

        loadStaffAccounts();
    }

    public static void showWindow() {
        if (instance == null) {
            instance = new AdminProfileFrame();
            instance.setOnHidden(e -> instance = null);
        }
        instance.loadStaffAccounts();
        instance.setIconified(false);
        instance.setMaximized(true);
        instance.show();
        instance.toFront();
    }

    private void configureStaffTable() {
        TableColumn<AdminDAO.StaffAccount, Integer> idCol = new TableColumn<>("Staff ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("staffId"));

        TableColumn<AdminDAO.StaffAccount, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<AdminDAO.StaffAccount, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        TableColumn<AdminDAO.StaffAccount, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        UIStyle.applyStatusBadgeColumn(statusCol);

        TableColumn<AdminDAO.StaffAccount, String> createdCol = new TableColumn<>("Created At");
        createdCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        staffTable.getColumns().addAll(idCol, usernameCol, nameCol, statusCol, createdCol);
    }

    private void updatePassword() {
        passwordStatusLabel.setText("");
        String currentPassword = currentPasswordField.getText().trim();
        String newPassword = newPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (!newPassword.equals(confirmPassword)) {
            passwordStatusLabel.setText("New password and confirmation do not match.");
            return;
        }
        if (newPassword.length() < 6) {
            passwordStatusLabel.setText("New password must be at least 6 characters.");
            return;
        }

        AdminDAO.ActionResult result = adminDAO.changePassword(AdminSession.getAdminId(), currentPassword, newPassword);
        if (result.success) {
            new Alert(Alert.AlertType.INFORMATION, result.message).showAndWait();
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
        } else {
            passwordStatusLabel.setText(result.message);
        }
    }

    private void createStaffAccount() {
        staffStatusLabel.setText("");
        int staffId;
        try {
            staffId = Integer.parseInt(staffIdField.getText().trim());
        } catch (NumberFormatException ex) {
            staffStatusLabel.setText("Enter a numeric staff ID.");
            return;
        }

        if (staffPasswordField.getText().trim().length() < 6) {
            staffStatusLabel.setText("Staff password must be at least 6 characters.");
            return;
        }

        AdminDAO.ActionResult result = adminDAO.createStaffAccount(staffId, staffPasswordField.getText().trim());
        if (result.success) {
            new Alert(Alert.AlertType.INFORMATION, result.message).showAndWait();
            staffIdField.clear();
            staffPasswordField.clear();
            loadStaffAccounts();
        } else {
            staffStatusLabel.setText(result.message);
        }
    }

    private void loadStaffAccounts() {
        staffTable.setItems(FXCollections.observableArrayList(adminDAO.getStaffAccounts()));
    }

    private void addSingleColumn(GridPane pane) {
        javafx.scene.layout.ColumnConstraints col = new javafx.scene.layout.ColumnConstraints();
        col.setHgrow(Priority.ALWAYS);
        pane.getColumnConstraints().add(col);
    }
}

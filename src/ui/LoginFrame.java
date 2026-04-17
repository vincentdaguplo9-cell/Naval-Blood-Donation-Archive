package ui;

import dao.AdminDAO;
import javafx.beans.binding.Bindings;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import util.AdminSession;

import java.nio.file.Paths;

// Simple admin login window (JavaFX).
public class LoginFrame extends Stage {
    private TextField usernameField;
    private PasswordField passwordField;
    private TextField passwordVisibleField;
    private final AdminDAO adminDAO;
    private Label usernameError;
    private Label passwordError;
    private Label statusLabel;

    public LoginFrame() {
        adminDAO = new AdminDAO();

        setTitle("Naval Blood Donation Archive System - Login");

        StackPane photoPane = createPhotoPane();
        VBox authPane = createAuthPane();

        BorderPane layout = new BorderPane();
        layout.setLeft(photoPane);
        layout.setCenter(authPane);
        layout.setStyle("-fx-background-color: #F5F7FB;");

        photoPane.prefWidthProperty().bind(layout.widthProperty().multiply(0.58));
        authPane.prefWidthProperty().bind(layout.widthProperty().multiply(0.42));

        Scene scene = new Scene(layout, 1360, 760);
        setScene(scene);
        setMaximized(true);
    }

    private StackPane createPhotoPane() {
        ImageView backgroundView = new ImageView(new Image(
                Paths.get("assets", "images", "blood.jpg").toUri().toString(),
                true
        ));
        backgroundView.setPreserveRatio(false);
        backgroundView.setSmooth(true);

        Region darkOverlay = new Region();
        darkOverlay.setStyle(
                "-fx-background-color: linear-gradient(to bottom, rgba(8,14,24,0.42), rgba(8,14,24,0.58));"
        );

        StackPane pane = new StackPane(backgroundView, darkOverlay);
        pane.setMinWidth(520);
        backgroundView.fitWidthProperty().bind(pane.widthProperty());
        backgroundView.fitHeightProperty().bind(pane.heightProperty());
        darkOverlay.prefWidthProperty().bind(pane.widthProperty());
        darkOverlay.prefHeightProperty().bind(pane.heightProperty());
        return pane;
    }

    private VBox createAuthPane() {
        VBox pane = new VBox();
        pane.setAlignment(Pos.CENTER);
        pane.setPadding(new Insets(42, 56, 42, 56));
        pane.setStyle("-fx-background-color: linear-gradient(to bottom, #FAFBFD, #F2F5F9);");

        VBox shell = new VBox(20);
        shell.setAlignment(Pos.CENTER_LEFT);
        shell.setMaxWidth(430);

        Label brand = new Label("Naval Blood Donation Archive");
        brand.setTextFill(Color.web(UIStyle.PRIMARY));
        brand.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));

        Label title = new Label("Secure Staff Login");
        title.setTextFill(Color.web(UIStyle.TEXT));
        title.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 32));

        Label subtitle = new Label("Sign in to continue managing donor records, donations, and blood inventory.");
        subtitle.setWrapText(true);
        subtitle.setTextFill(Color.web(UIStyle.MUTED));
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));

        GridPane formPanel = new GridPane();
        formPanel.setHgap(0);
        formPanel.setVgap(10);
        formPanel.setPadding(new Insets(28));
        formPanel.setMaxWidth(420);
        formPanel.setStyle(
                "-fx-background-color: rgba(255,255,255,0.97);" +
                "-fx-background-radius: 26;" +
                "-fx-border-color: #E4EBF2;" +
                "-fx-border-radius: 26;" +
                "-fx-effect: dropshadow(gaussian, rgba(24,39,75,0.12), 32, 0.18, 0, 10);"
        );

        ColumnConstraints column = new ColumnConstraints();
        column.setHgrow(Priority.ALWAYS);
        formPanel.getColumnConstraints().add(column);

        int row = 0;
        Label formTitle = new Label("Admin account");
        formTitle.setTextFill(Color.web(UIStyle.TEXT));
        formTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        formPanel.add(formTitle, 0, row++);

        Label formHelp = new Label("Use your assigned username and password.");
        formHelp.setTextFill(Color.web(UIStyle.MUTED));
        formHelp.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        formPanel.add(formHelp, 0, row++);

        Region spacer1 = new Region();
        spacer1.setMinHeight(6);
        formPanel.add(spacer1, 0, row++);

        Label userLabel = UIStyle.formLabel("Username");
        formPanel.add(userLabel, 0, row++);

        usernameField = new TextField();
        UIStyle.applyInputStyle(usernameField);
        usernameField.setPromptText("Enter username");
        usernameField.setPrefHeight(44);
        formPanel.add(usernameField, 0, row++);

        usernameError = UIStyle.errorLabel();
        formPanel.add(usernameError, 0, row++);

        Label passLabel = UIStyle.formLabel("Password");
        formPanel.add(passLabel, 0, row++);

        passwordField = new PasswordField();
        UIStyle.applyInputStyle(passwordField);
        passwordField.setPromptText("Enter password");
        passwordField.setPrefHeight(44);

        passwordVisibleField = new TextField();
        UIStyle.applyInputStyle(passwordVisibleField);
        passwordVisibleField.setPromptText("Enter password");
        passwordVisibleField.setPrefHeight(44);
        passwordVisibleField.setManaged(false);
        passwordVisibleField.setVisible(false);

        StackPane passwordStack = new StackPane(passwordField, passwordVisibleField);
        formPanel.add(passwordStack, 0, row++);

        passwordError = UIStyle.errorLabel();
        formPanel.add(passwordError, 0, row++);

        CheckBox showPassword = new CheckBox("Show password");
        showPassword.setTextFill(Color.web(UIStyle.MUTED));
        showPassword.setOnAction(e -> togglePasswordVisibility(showPassword.isSelected()));
        formPanel.add(showPassword, 0, row++);

        statusLabel = UIStyle.errorLabel();
        formPanel.add(statusLabel, 0, row++);

        Region spacer2 = new Region();
        spacer2.setMinHeight(4);
        formPanel.add(spacer2, 0, row++);

        Button loginBtn = UIStyle.primaryButton("Login", "login");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setPrefHeight(46);
        loginBtn.setOnAction(e -> handleLogin());
        loginBtn.setDefaultButton(true);
        formPanel.add(loginBtn, 0, row);

        GridPane.setHalignment(formTitle, HPos.LEFT);
        GridPane.setHalignment(formHelp, HPos.LEFT);

        loginBtn.disableProperty().bind(Bindings.createBooleanBinding(
                () -> usernameField.getText().trim().isEmpty() || getPassword().trim().isEmpty(),
                usernameField.textProperty(),
                passwordField.textProperty(),
                passwordVisibleField.textProperty()
        ));

        usernameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.trim().isEmpty()) {
                usernameError.setText("");
                statusLabel.setText("");
            }
        });

        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.trim().isEmpty()) {
                passwordError.setText("");
                statusLabel.setText("");
            }
            if (!passwordVisibleField.isVisible()) {
                passwordVisibleField.setText(newVal);
            }
        });

        passwordVisibleField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (passwordVisibleField.isVisible()) {
                passwordField.setText(newVal);
            }
        });

        shell.getChildren().addAll(brand, title, subtitle, formPanel);
        pane.getChildren().add(shell);
        return pane;
    }

    private void togglePasswordVisibility(boolean show) {
        if (show) {
            passwordVisibleField.setText(passwordField.getText());
        } else {
            passwordField.setText(passwordVisibleField.getText());
        }
        passwordVisibleField.setVisible(show);
        passwordVisibleField.setManaged(show);
        passwordField.setVisible(!show);
        passwordField.setManaged(!show);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = getPassword().trim();

        usernameError.setText("");
        passwordError.setText("");
        statusLabel.setText("");

        boolean valid = true;
        if (username.isEmpty()) {
            usernameError.setText("Username is required.");
            valid = false;
        }
        if (password.isEmpty()) {
            passwordError.setText("Password is required.");
            valid = false;
        }
        if (!valid) {
            return;
        }

        AdminDAO.LoginResult result = adminDAO.authenticate(username, password);
        if (result.success) {
            AdminSession.start(result.adminId, result.username);
            DashboardFrame.showWindow();
            close();
        } else {
            statusLabel.setText(result.message);
        }
    }

    private String getPassword() {
        return passwordVisibleField.isVisible() ? passwordVisibleField.getText() : passwordField.getText();
    }
}

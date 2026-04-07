package ui;

import dao.AdminDAO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.beans.binding.Bindings;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

// Simple admin login window (JavaFX).
public class LoginFrame extends Stage {
    private final TextField usernameField;
    private final PasswordField passwordField;
    private final TextField passwordVisibleField;
    private final AdminDAO adminDAO;
    private final Label usernameError;
    private final Label passwordError;
    private final Label statusLabel;

    public LoginFrame() {
        adminDAO = new AdminDAO();

        setTitle("Naval Blood Donation Archive System - Login");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + UIStyle.BG + ";");

        // Left branding panel.
        VBox brandPanel = new VBox(8);
        brandPanel.setAlignment(Pos.CENTER);
        brandPanel.setStyle("-fx-background-color: " + UIStyle.PRIMARY + ";");
        brandPanel.setPrefWidth(180);

        Label brandTitle = new Label("NBDA");
        brandTitle.setTextFill(Color.WHITE);
        brandTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));

        Label brandSubtitle = new Label("Blood Archive");
        brandSubtitle.setTextFill(Color.web("#dcebf5"));
        brandSubtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));

        brandPanel.getChildren().addAll(brandTitle, brandSubtitle);

        // Card panel provides a clean elevated area for login fields.
        GridPane formPanel = new GridPane();
        formPanel.setHgap(12);
        formPanel.setVgap(12);
        formPanel.setAlignment(Pos.CENTER);
        UIStyle.applyCardStyle(formPanel);

        Label title = UIStyle.titleLabel("Admin Login");
        Label subtitle = UIStyle.subtitleLabel("Secure access for hospital staff");

        formPanel.add(title, 0, 0, 2, 1);
        formPanel.add(subtitle, 0, 1, 2, 1);

        Label userLabel = new Label("Username *");
        userLabel.setFont(UIStyle.BODY);
        userLabel.setTextFill(Color.web(UIStyle.MUTED));
        formPanel.add(userLabel, 0, 2);

        usernameField = new TextField();
        UIStyle.applyInputStyle(usernameField);
        usernameField.setPrefWidth(220);
        formPanel.add(usernameField, 1, 2);


        Label passLabel = new Label("Password *");
        passLabel.setFont(UIStyle.BODY);
        passLabel.setTextFill(Color.web(UIStyle.MUTED));
        formPanel.add(passLabel, 0, 3);

        passwordField = new PasswordField();
        UIStyle.applyInputStyle(passwordField);
        passwordField.setPrefWidth(220);

        passwordVisibleField = new TextField();
        UIStyle.applyInputStyle(passwordVisibleField);
        passwordVisibleField.setPrefWidth(220);
        passwordVisibleField.setManaged(false);
        passwordVisibleField.setVisible(false);

        StackPane passwordStack = new StackPane(passwordField, passwordVisibleField);
        formPanel.add(passwordStack, 1, 3);


        usernameError = new Label("");
        usernameError.setTextFill(Color.web("#c0392b"));
        usernameError.setStyle("-fx-font-size: 11px;");
        formPanel.add(usernameError, 1, 4);

        passwordError = new Label("");
        passwordError.setTextFill(Color.web("#c0392b"));
        passwordError.setStyle("-fx-font-size: 11px;");
        formPanel.add(passwordError, 1, 5);

        statusLabel = new Label("");
        statusLabel.setTextFill(Color.web("#c0392b"));
        statusLabel.setStyle("-fx-font-size: 11px;");

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
        });

        javafx.scene.layout.GridPane.setHalignment(title, javafx.geometry.HPos.CENTER);
        javafx.scene.layout.GridPane.setHalignment(subtitle, javafx.geometry.HPos.CENTER);
        javafx.scene.layout.GridPane.setHalignment(userLabel, javafx.geometry.HPos.CENTER);
        javafx.scene.layout.GridPane.setHalignment(usernameField, javafx.geometry.HPos.CENTER);
        javafx.scene.layout.GridPane.setHalignment(passLabel, javafx.geometry.HPos.CENTER);
        javafx.scene.layout.GridPane.setHalignment(passwordStack, javafx.geometry.HPos.CENTER);

        CheckBox showPassword = new CheckBox("Show password");
        showPassword.setTextFill(Color.web(UIStyle.MUTED));
        showPassword.setOnAction(e -> {
            boolean show = showPassword.isSelected();
            if (show) {
                passwordVisibleField.setText(passwordField.getText());
            } else {
                passwordField.setText(passwordVisibleField.getText());
            }
            passwordVisibleField.setVisible(show);
            passwordVisibleField.setManaged(show);
            passwordField.setVisible(!show);
            passwordField.setManaged(!show);
        });

        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!passwordVisibleField.isVisible()) {
                passwordVisibleField.setText(newVal);
            }
        });

        passwordVisibleField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (passwordVisibleField.isVisible()) {
                passwordField.setText(newVal);
            }
        });

        Button loginBtn = UIStyle.primaryButton("Login");
        loginBtn.setOnAction(e -> handleLogin());
        loginBtn.setDefaultButton(true);

        loginBtn.disableProperty().bind(Bindings.createBooleanBinding(
                () -> usernameField.getText().trim().isEmpty() || getPassword().trim().isEmpty(),
                usernameField.textProperty(),
                passwordField.textProperty(),
                passwordVisibleField.textProperty()
        ));

        HBox loginRow = new HBox(12, showPassword, loginBtn);
        loginRow.setAlignment(Pos.CENTER);
        formPanel.add(loginRow, 1, 6);
        javafx.scene.layout.GridPane.setHalignment(loginRow, javafx.geometry.HPos.CENTER);

        formPanel.add(statusLabel, 1, 7);

        StackPane cardWrap = new StackPane(formPanel);
        cardWrap.setPadding(new Insets(16));

        root.setLeft(brandPanel);
        root.setCenter(cardWrap);

        Scene scene = new Scene(root, 520, 320);
        setScene(scene);
        setMaximized(true);
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

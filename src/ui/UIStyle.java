package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;

// Shared UI styling for a clean hospital-like look (JavaFX).
public class UIStyle {
    public static final String PRIMARY = "#085380";
    public static final String PRIMARY_DARK = "#053e62";
    public static final String ACCENT = "#14998c";
    public static final String BG = "#f5f8fb";
    public static final String CARD = "#ffffff";
    public static final String TEXT = "#212d3b";
    public static final String MUTED = "#5a6e82";

    public static final Font TITLE = Font.font("Segoe UI", FontWeight.BOLD, 20);
    public static final Font SUBTITLE = Font.font("Segoe UI", FontWeight.BOLD, 14);
    public static final Font BODY = Font.font("Segoe UI", FontWeight.NORMAL, 13);

    private UIStyle() {
    }

    public static Label titleLabel(String text) {
        Label label = new Label(text);
        label.setFont(TITLE);
        label.setTextFill(Color.web(TEXT));
        return label;
    }

    public static Label subtitleLabel(String text) {
        Label label = new Label(text);
        label.setFont(SUBTITLE);
        label.setTextFill(Color.web(MUTED));
        return label;
    }

    public static Button primaryButton(String text) {
        Button btn = new Button(text);
        btn.setFont(SUBTITLE);
        btn.setTextFill(Color.WHITE);
        btn.setStyle(
                "-fx-background-color: " + PRIMARY + ";" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 8 18 8 18;"
        );
        return btn;
    }

    public static Label statLabel(String text) {
        Label label = new Label(text);
        label.setFont(SUBTITLE);
        label.setTextFill(Color.web(TEXT));
        label.setAlignment(Pos.CENTER);
        label.setMinHeight(48);
        label.setStyle(
                "-fx-background-color: " + CARD + ";" +
                "-fx-border-color: #e1e6eb;" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 12;"
        );
        return label;
    }

    public static void applyCardStyle(Region region) {
        region.setStyle(
                "-fx-background-color: " + CARD + ";" +
                "-fx-border-color: #e6ebf0;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;"
        );
        region.setPadding(new Insets(18));
    }

    public static void applyInputStyle(Control control) {
        control.setStyle(
                "-fx-background-radius: 5;" +
                "-fx-border-radius: 5;" +
                "-fx-border-color: #d2dae1;" +
                "-fx-padding: 6 8 6 8;"
        );
    }

    public static void applyTableStyle(TableView<?> table) {
        table.setStyle(
                "-fx-background-color: " + CARD + ";" +
                "-fx-border-color: #e6ebf0;" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;" +
                "-fx-selection-bar: " + PRIMARY_DARK + ";" +
                "-fx-selection-bar-non-focused: " + PRIMARY + ";" +
                "-fx-selection-bar-text: #ffffff;"
        );
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
}

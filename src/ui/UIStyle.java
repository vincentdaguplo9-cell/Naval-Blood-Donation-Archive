package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Shared UI styling for a bright, clinical hospital dashboard look.
public class UIStyle {
    private static final Pattern SVG_PATH_PATTERN = Pattern.compile("d\\s*=\\s*\"([^\"]+)\"");

    public static final String PRIMARY = "#B7232F";
    public static final String PRIMARY_DARK = "#8F1824";
    public static final String ACCENT = "#0D9488";
    public static final String DANGER = "#CC3344";
    public static final String SUCCESS = "#1F8A5B";
    public static final String BG = "#F4F7FB";
    public static final String BG_ELEVATED = "#FFFFFF";
    public static final String CARD = "#FFFFFF";
    public static final String CARD_ALT = "#F8FBFF";
    public static final String TEXT = "#1F2937";
    public static final String MUTED = "#64748B";
    public static final String BORDER = "#D7E0EA";
    public static final String BORDER_STRONG = "#C6D3DF";
    public static final String SHADOW = "rgba(24,39,75,0.10)";

    public static final Font TITLE = Font.font("Segoe UI", FontWeight.BOLD, 24);
    public static final Font SUBTITLE = Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14);
    public static final Font BODY = Font.font("Segoe UI", FontWeight.NORMAL, 13);
    public static final Font CAPTION = Font.font("Segoe UI", FontWeight.NORMAL, 11);

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

    public static Label formLabel(String text) {
        Label label = new Label(text);
        label.setFont(BODY);
        label.setTextFill(Color.web(TEXT));
        return label;
    }

    public static Label helperLabel(String text) {
        Label label = new Label(text);
        label.setFont(CAPTION);
        label.setWrapText(true);
        label.setTextFill(Color.web(MUTED));
        return label;
    }

    public static Label errorLabel() {
        Label label = new Label("");
        label.setTextFill(Color.web(DANGER));
        label.setStyle("-fx-font-size: 11px;");
        return label;
    }

    public static Button primaryButton(String text) {
        return primaryButton(text, null);
    }

    public static Button primaryButton(String text, String iconName) {
        Button btn = createButton(text, iconName, true);
        btn.setTextFill(Color.WHITE);
        btn.setStyle(
                "-fx-background-color: linear-gradient(to right, " + PRIMARY + ", " + PRIMARY_DARK + ");" +
                "-fx-background-radius: 12;" +
                "-fx-background-insets: 0;" +
                "-fx-border-width: 0;" +
                "-fx-effect: dropshadow(gaussian, " + SHADOW + ", 18, 0.25, 0, 6);" +
                "-fx-cursor: hand;" +
                "-fx-padding: 11 18 11 18;"
        );
        return btn;
    }

    public static Button secondaryButton(String text) {
        return secondaryButton(text, null);
    }

    public static Button secondaryButton(String text, String iconName) {
        Button btn = createButton(text, iconName, false);
        btn.setTextFill(Color.web(TEXT));
        btn.setStyle(
                "-fx-background-color: " + BG_ELEVATED + ";" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 12;" +
                "-fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian, " + SHADOW + ", 14, 0.16, 0, 4);" +
                "-fx-cursor: hand;" +
                "-fx-padding: 11 18 11 18;"
        );
        return btn;
    }

    public static Button dangerButton(String text) {
        return dangerButton(text, "warning");
    }

    public static Button dangerButton(String text, String iconName) {
        Button btn = createButton(text, iconName, true);
        btn.setTextFill(Color.WHITE);
        btn.setStyle(
                "-fx-background-color: linear-gradient(to right, " + DANGER + ", #A61E32);" +
                "-fx-background-radius: 12;" +
                "-fx-effect: dropshadow(gaussian, " + SHADOW + ", 18, 0.22, 0, 6);" +
                "-fx-cursor: hand;" +
                "-fx-padding: 11 18 11 18;"
        );
        return btn;
    }

    private static Button createButton(String text, String iconName, boolean filled) {
        Button btn = new Button(text);
        btn.setFont(SUBTITLE);
        if (iconName != null && !iconName.isEmpty()) {
            btn.setGraphic(createButtonIcon(iconName, filled));
            btn.setGraphicTextGap(10);
        }
        return btn;
    }

    private static Node createButtonIcon(String iconName, boolean filled) {
        StackPane badge = new StackPane();
        badge.setMinSize(24, 24);
        badge.setPrefSize(24, 24);
        badge.setMaxSize(24, 24);
        badge.setStyle(
                "-fx-background-color: " + (filled ? "rgba(255,255,255,0.18)" : "#FEECEE") + ";" +
                "-fx-background-radius: 999;"
        );

        SVGPath icon = new SVGPath();
        icon.setContent(loadIconPath(iconName));
        icon.setFill(Color.web(filled ? "#FFFFFF" : PRIMARY));
        badge.getChildren().add(icon);
        return badge;
    }

    private static String loadIconPath(String iconName) {
        Path iconFile = Paths.get("assets", "icons", iconName + ".svg");
        try {
            String svg = Files.readString(iconFile);
            Matcher matcher = SVG_PATH_PATTERN.matcher(svg);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (IOException ignored) {
        }
        return fallbackIconPath();
    }

    private static String fallbackIconPath() {
        return "M12 2C9 6.1 6 9.2 6 13a6 6 0 0 0 12 0c0-3.8-3-6.9-6-11z";
    }

    public static HBox appHeader(String pageTitle, Button... rightActions) {
        Label brand = new Label("NBDA");
        brand.setFont(SUBTITLE);
        brand.setTextFill(Color.web(PRIMARY));

        Label page = new Label(pageTitle);
        page.setFont(TITLE);
        page.setTextFill(Color.web(TEXT));

        Label supporting = new Label("Naval Blood Donation Archive");
        supporting.setFont(CAPTION);
        supporting.setTextFill(Color.web(MUTED));

        VBox left = new VBox(1, brand, page, supporting);
        left.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        if (rightActions != null && rightActions.length > 0) {
            actions.getChildren().addAll(rightActions);
        }

        HBox header = new HBox(12, left, spacer, actions);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 20, 16, 20));
        header.setStyle(
                "-fx-background-color: linear-gradient(to right, #FFFFFF, #F9FCFF 72%, #FFF3F4 100%);" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 0 0 20 20;" +
                "-fx-background-radius: 0 0 20 20;" +
                "-fx-border-width: 0 0 1 0;" +
                "-fx-effect: dropshadow(gaussian, " + SHADOW + ", 24, 0.18, 0, 8);"
        );
        return header;
    }

    public static Label statLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        label.setTextFill(Color.web(TEXT));
        label.setAlignment(Pos.CENTER_LEFT);
        label.setMinHeight(82);
        label.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #FFFFFF, #F8FBFF);" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 18;" +
                "-fx-background-radius: 18;" +
                "-fx-effect: dropshadow(gaussian, " + SHADOW + ", 22, 0.16, 0, 8);" +
                "-fx-padding: 18 20 18 20;"
        );
        return label;
    }

    public static void applyCardStyle(Region region) {
        region.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + CARD + ", #F9FCFF);" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 20;" +
                "-fx-background-radius: 20;" +
                "-fx-effect: dropshadow(gaussian, " + SHADOW + ", 28, 0.16, 0, 10);"
        );
        region.setPadding(new Insets(22));
    }

    public static void applyPanelStyle(Region region) {
        region.setStyle(
                "-fx-background-color: " + CARD_ALT + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 18;" +
                "-fx-background-radius: 18;" +
                "-fx-effect: dropshadow(gaussian, " + SHADOW + ", 18, 0.14, 0, 6);"
        );
    }

    public static void applyInputStyle(Control control) {
        control.setStyle(
                "-fx-background-color: #FFFFFF;" +
                "-fx-text-fill: " + TEXT + ";" +
                "-fx-prompt-text-fill: " + MUTED + ";" +
                "-fx-background-radius: 10;" +
                "-fx-border-radius: 10;" +
                "-fx-border-color: " + BORDER_STRONG + ";" +
                "-fx-focus-color: " + PRIMARY + ";" +
                "-fx-faint-focus-color: rgba(183,35,47,0.10);" +
                "-fx-highlight-fill: " + PRIMARY + ";" +
                "-fx-highlight-text-fill: white;" +
                "-fx-padding: 10 12 10 12;"
        );
    }

    public static void applyTableStyle(TableView<?> table) {
        table.setStyle(
                "-fx-background-color: #FFFFFF;" +
                "-fx-control-inner-background: #FFFFFF;" +
                "-fx-control-inner-background-alt: #F7FBFF;" +
                "-fx-table-cell-border-color: #E6EDF5;" +
                "-fx-background-insets: 0;" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 18;" +
                "-fx-background-radius: 18;" +
                "-fx-selection-bar: #FAD3D7;" +
                "-fx-selection-bar-non-focused: #FCE2E5;" +
                "-fx-selection-bar-text: " + TEXT + ";" +
                "-fx-table-header-border-color: #E2EAF2;" +
                "-fx-effect: dropshadow(gaussian, " + SHADOW + ", 20, 0.12, 0, 8);"
        );
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setFixedCellSize(38);
    }

    public static <T> void applyStatusBadgeColumn(TableColumn<T, String> column) {
        column.setCellFactory(col -> new TableCell<T, String>() {
            private final Label badge = new Label();

            {
                badge.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
                badge.setPadding(new Insets(4, 10, 4, 10));
                badge.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.trim().isEmpty()) {
                    setGraphic(null);
                    return;
                }
                badge.setText(item);
                badge.setStyle(statusBadgeStyle(item));
                setGraphic(badge);
                setText(null);
                setAlignment(Pos.CENTER);
            }
        });
    }

    private static String statusBadgeStyle(String rawValue) {
        String value = rawValue == null ? "" : rawValue.trim().toUpperCase();
        String background = "#EEF2F7";
        String borderColor = "#D5DEE8";
        String textColor = TEXT;

        if (value.contains("ELIGIBLE") || value.contains("AVAILABLE") || value.contains("PASS")) {
            background = "#E7F7EF";
            borderColor = "#B8E3CB";
            textColor = SUCCESS;
        } else if (value.contains("QUARANTINE") || value.contains("PENDING") || value.contains("RESERVED")) {
            background = "#FFF5E8";
            borderColor = "#F6D19B";
            textColor = "#A35B00";
        } else if (value.contains("ISSUED")) {
            background = "#E9F4FF";
            borderColor = "#B6D7F6";
            textColor = "#1E5FAF";
        } else if (value.contains("FAILED") || value.contains("FAIL") || value.contains("EXPIRED")
                || value.contains("DISCARDED") || value.contains("INELIGIBLE")) {
            background = "#FDECEC";
            borderColor = "#F2B8BE";
            textColor = DANGER;
        }

        return "-fx-background-color: " + background + ";" +
                "-fx-background-radius: 999;" +
                "-fx-border-color: " + borderColor + ";" +
                "-fx-border-radius: 999;" +
                "-fx-text-fill: " + textColor + ";";
    }

    public static String lightRowStyle(int rowIndex) {
        if (rowIndex % 2 == 0) {
            return "-fx-background-color: #FFFFFF; -fx-text-fill: " + TEXT + ";";
        }
        return "-fx-background-color: #F8FBFF; -fx-text-fill: " + TEXT + ";";
    }

    public static String pageBackground() {
        return "-fx-background-color: " +
                "radial-gradient(center 18% 10%, radius 34%, rgba(13,148,136,0.10), transparent 70%)," +
                "radial-gradient(center 92% 8%, radius 28%, rgba(183,35,47,0.12), transparent 72%)," +
                "linear-gradient(to bottom, #FAFCFF, " + BG + ");";
    }
}

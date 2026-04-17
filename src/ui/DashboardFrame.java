package ui;

import dao.BloodUnitDAO;
import dao.DonorDAO;
import dao.TransactionDAO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.DonationTransaction;
import model.Donor;

import java.util.List;

// Main dashboard window (JavaFX).
public class DashboardFrame extends Stage {
    private static DashboardFrame instance;
    private final Label totalDonorsLabel;
    private final Label totalUnitsLabel;
    private final Label availableUnitsLabel;
    private final TableView<RecentTransactionRow> recentTable;
    private final TableView<Donor> recentDonorTable;
    private final Label lastUpdatedLabel;

    private final DonorDAO donorDAO;
    private final BloodUnitDAO unitDAO;
    private final TransactionDAO txDAO;

    public DashboardFrame() {
        donorDAO = new DonorDAO();
        unitDAO = new BloodUnitDAO();
        txDAO = new TransactionDAO();

        setTitle("Naval Blood Donation Archive System - Dashboard");
        setOnCloseRequest(e -> {
            Alert confirm = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Do you want to exit the program?",
                    ButtonType.OK,
                    ButtonType.CANCEL
            );
            confirm.setHeaderText("Exit Confirmation");
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.OK) {
                    Platform.exit();
                } else {
                    e.consume();
                }
            });
        });

        BorderPane root = new BorderPane();
        root.setStyle(UIStyle.pageBackground());
        root.setPadding(new Insets(0));

        // App header.
        Button labBtn = UIStyle.secondaryButton("Lab Testing", "lab");
        labBtn.setOnAction(e -> LabTestingFrame.showWindow());
        Button adminBtn = UIStyle.secondaryButton("Admin Profile", "dashboard");
        adminBtn.setOnAction(e -> AdminProfileFrame.showWindow());
        Button reportsBtn = UIStyle.secondaryButton("Reports", "report");
        reportsBtn.setOnAction(e -> ReportFrame.showWindow());
        HBox header = UIStyle.appHeader("Dashboard", adminBtn, labBtn, reportsBtn);

        VBox topWrap = new VBox(10);
        topWrap.getChildren().add(header);
        topWrap.setPadding(new Insets(0, 0, 8, 0));

        // Top stats bar for quick hospital overview.
        HBox statsPanel = new HBox(12);
        totalDonorsLabel = UIStyle.statLabel("Total Donors: 0");
        totalUnitsLabel = UIStyle.statLabel("Total Blood Units: 0");
        availableUnitsLabel = UIStyle.statLabel("Available Units: 0");
        makeStatCardClickable(totalDonorsLabel, "Click to open donor records.", DonorListFrame::showWindow);
        makeStatCardClickable(totalUnitsLabel, "Click to open all blood units with filters.", () ->
                InventoryFrame.showWindow("All", "All", "All"));
        makeStatCardClickable(availableUnitsLabel, "Click to open available blood units with filters.", () ->
                InventoryFrame.showWindow("AVAILABLE", "All", "All"));
        statsPanel.getChildren().addAll(totalDonorsLabel, totalUnitsLabel, availableUnitsLabel);

        Button refreshBtn = UIStyle.primaryButton("Refresh", "refresh");
        refreshBtn.setOnAction(e -> refreshDashboard());
        lastUpdatedLabel = new Label("");
        lastUpdatedLabel.setTextFill(javafx.scene.paint.Color.web(UIStyle.MUTED));
        lastUpdatedLabel.setStyle("-fx-font-size: 12px;");
        VBox refreshBox = new VBox(6, refreshBtn, lastUpdatedLabel);
        refreshBox.setAlignment(Pos.CENTER_RIGHT);
        UIStyle.applyPanelStyle(refreshBox);
        refreshBox.setPadding(new Insets(12));

        HBox topBar = new HBox(12, statsPanel, refreshBox);
        HBox.setHgrow(statsPanel, Priority.ALWAYS);
        topBar.setPadding(new Insets(0, 12, 0, 12));
        topWrap.getChildren().add(topBar);

        recentTable = new TableView<>();
        UIStyle.applyTableStyle(recentTable);
        recentTable.setPlaceholder(UIStyle.helperLabel("No recent transactions yet. Record a donation to begin the traceability log."));
        configureRecentTable();
        applyZebraRows();

        recentDonorTable = new TableView<>();
        UIStyle.applyTableStyle(recentDonorTable);
        recentDonorTable.setPlaceholder(UIStyle.helperLabel("No recent donors yet. Add a donor to start building the registry."));
        configureRecentDonorTable();
        applyDonorZebraRows();

        // Action buttons for main workflows.
        HBox actionsPanel = new HBox(12);
        actionsPanel.setAlignment(Pos.CENTER_LEFT);
        actionsPanel.setPadding(new Insets(10, 12, 12, 12));
        Button donorsBtn = UIStyle.primaryButton("1. Add Donor", "donor");
        donorsBtn.setOnAction(e -> DonorListFrame.showWindow());
        donorsBtn.setMaxWidth(Double.MAX_VALUE);

        Button donationBtn = UIStyle.primaryButton("2. Record Donation", "bloodbag");
        donationBtn.setOnAction(e -> DonationForm.showWindow());
        donationBtn.setMaxWidth(Double.MAX_VALUE);

        Button labQuickBtn = UIStyle.primaryButton("3. Run Lab Testing", "lab");
        labQuickBtn.setOnAction(e -> LabTestingFrame.showWindow());
        labQuickBtn.setMaxWidth(Double.MAX_VALUE);

        Region donorCard = createActionCard("Register a donor", "Create or update donor records before collection.", donorsBtn);
        Region donationCard = createActionCard("Capture collection", "Enter volume, dates, staff ID, and screening details.", donationBtn);
        Region labCard = createActionCard("Release units", "Review pending units and mark them passed or failed.", labQuickBtn);
        HBox.setHgrow(donorCard, Priority.ALWAYS);
        HBox.setHgrow(donationCard, Priority.ALWAYS);
        HBox.setHgrow(labCard, Priority.ALWAYS);
        actionsPanel.getChildren().addAll(donorCard, donationCard, labCard);

        VBox transactionPanel = createPortraitPanel(
                "Recent Transactions",
                "Latest donation activity with only the key traceability details.",
                recentTable
        );
        VBox donorPanel = createPortraitPanel(
                "Recent Donors",
                "Newest donor records with quick eligibility and blood type visibility.",
                recentDonorTable
        );
        HBox centerBox = new HBox(12, transactionPanel, donorPanel);
        centerBox.setPadding(new Insets(12));
        HBox.setHgrow(transactionPanel, Priority.ALWAYS);
        HBox.setHgrow(donorPanel, Priority.ALWAYS);

        root.setTop(topWrap);
        root.setCenter(centerBox);
        root.setBottom(actionsPanel);

        Scene scene = new Scene(root, 900, 600);
        setScene(scene);
        setMaximized(true);

        refreshDashboard();
    }

    public static void showWindow() {
        if (instance == null) {
            instance = new DashboardFrame();
            instance.setOnHidden(e -> instance = null);
        }
        instance.setIconified(false);
        instance.show();
        instance.toFront();
    }

    private void configureRecentTable() {
        TableColumn<RecentTransactionRow, String> donorCol = new TableColumn<>("Donor");
        donorCol.setCellValueFactory(new PropertyValueFactory<>("donorName"));

        TableColumn<RecentTransactionRow, Integer> unitCol = new TableColumn<>("Unit");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unitId"));

        TableColumn<RecentTransactionRow, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));

        recentTable.getColumns().addAll(donorCol, unitCol, dateCol);
    }

    private void configureRecentDonorTable() {
        TableColumn<Donor, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(
                        cell.getValue().getFirstName() + " " + cell.getValue().getLastName()
                )
        );

        TableColumn<Donor, String> bloodCol = new TableColumn<>("Blood Type");
        bloodCol.setCellValueFactory(new PropertyValueFactory<>("bloodType"));

        TableColumn<Donor, java.sql.Date> lastDonationCol = new TableColumn<>("Last Donation");
        lastDonationCol.setCellValueFactory(new PropertyValueFactory<>("lastDonationDate"));

        TableColumn<Donor, String> eligibilityCol = new TableColumn<>("Eligibility");
        eligibilityCol.setCellValueFactory(new PropertyValueFactory<>("eligibilityStatus"));
        UIStyle.applyStatusBadgeColumn(eligibilityCol);

        recentDonorTable.getColumns().addAll(nameCol, bloodCol, eligibilityCol);
    }

    private Region createActionCard(String title, String body, Button actionButton) {
        Label titleLabel = UIStyle.formLabel(title);
        Label bodyLabel = UIStyle.helperLabel(body);
        VBox box = new VBox(8, titleLabel, bodyLabel, actionButton);
        UIStyle.applyPanelStyle(box);
        box.setPadding(new Insets(16));
        VBox.setVgrow(actionButton, Priority.NEVER);
        return box;
    }

    private VBox createPortraitPanel(String title, String body, TableView<?> table) {
        Label titleLabel = UIStyle.subtitleLabel(title);
        Label bodyLabel = UIStyle.helperLabel(body);
        VBox panel = new VBox(10, titleLabel, bodyLabel, table);
        UIStyle.applyPanelStyle(panel);
        panel.setPadding(new Insets(16));
        panel.setPrefWidth(420);
        VBox.setVgrow(table, Priority.ALWAYS);
        return panel;
    }

    private void makeStatCardClickable(Label card, String hint, Runnable action) {
        card.setStyle(card.getStyle() + "-fx-cursor: hand;");
        card.setOnMouseClicked(e -> action.run());
        card.setOnMouseEntered(e -> card.setOpacity(0.92));
        card.setOnMouseExited(e -> card.setOpacity(1.0));
        card.setTooltip(new javafx.scene.control.Tooltip(hint));
    }

    private void applyZebraRows() {
        recentTable.setRowFactory(tv -> new javafx.scene.control.TableRow<RecentTransactionRow>() {
            @Override
            protected void updateItem(RecentTransactionRow item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (isSelected()) {
                    setStyle("");
                } else {
                    setStyle(UIStyle.lightRowStyle(getIndex()));
                }
            }
        });
    }

    private void applyDonorZebraRows() {
        recentDonorTable.setRowFactory(tv -> new javafx.scene.control.TableRow<Donor>() {
            @Override
            protected void updateItem(Donor item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (isSelected()) {
                    setStyle("");
                } else {
                    setStyle(UIStyle.lightRowStyle(getIndex()));
                }
            }
        });
    }

    private void refreshDashboard() {
        totalDonorsLabel.setText("Total Donors: " + donorDAO.getAllDonors().size());
        totalUnitsLabel.setText("Total Blood Units: " + unitDAO.getTotalUnitsCount());
        availableUnitsLabel.setText("Available Units: " + unitDAO.getAvailableUnitsCount());
        lastUpdatedLabel.setText("Last updated: " + java.time.LocalDateTime.now().toString().replace('T', ' '));

        List<DonationTransaction> recent = txDAO.getRecentTransactions(5);
        ObservableList<RecentTransactionRow> rows = FXCollections.observableArrayList();
        for (DonationTransaction tx : recent) {
            String donorName = getDonorName(tx.getDonorId());
            rows.add(new RecentTransactionRow(
                    tx.getTransactionId(),
                    donorName,
                    tx.getUnitId(),
                    tx.getStaffId(),
                    tx.getTransactionDate() == null ? "" : tx.getTransactionDate().toString(),
                    tx.getRemarks()
            ));
        }
        recentTable.setItems(rows);

        List<Donor> recentDonors = donorDAO.getRecentDonors(8);
        ObservableList<Donor> donorRows = FXCollections.observableArrayList(recentDonors);
        recentDonorTable.setItems(donorRows);
    }

    private String getDonorName(int donorId) {
        Donor donor = donorDAO.getDonorById(donorId);
        if (donor == null) {
            return String.valueOf(donorId);
        }
        return donor.getFirstName() + " " + donor.getLastName();
    }

    public static class RecentTransactionRow {
        private final Integer transactionId;
        private final String donorName;
        private final Integer unitId;
        private final Integer staffId;
        private final String transactionDate;
        private final String remarks;

        public RecentTransactionRow(int transactionId, String donorName, int unitId, int staffId, String transactionDate, String remarks) {
            this.transactionId = transactionId;
            this.donorName = donorName;
            this.unitId = unitId;
            this.staffId = staffId;
            this.transactionDate = transactionDate;
            this.remarks = remarks;
        }

        public Integer getTransactionId() {
            return transactionId;
        }

        public String getDonorName() {
            return donorName;
        }

        public Integer getUnitId() {
            return unitId;
        }

        public Integer getStaffId() {
            return staffId;
        }

        public String getTransactionDate() {
            return transactionDate;
        }

        public String getRemarks() {
            return remarks;
        }
    }
}

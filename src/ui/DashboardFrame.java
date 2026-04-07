package ui;

import dao.BloodUnitDAO;
import dao.DonorDAO;
import dao.TransactionDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
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

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + UIStyle.BG + ";");
        root.setPadding(new Insets(12));

        // Top stats bar for quick hospital overview.
        HBox statsPanel = new HBox(12);
        totalDonorsLabel = UIStyle.statLabel("Total Donors: 0");
        totalUnitsLabel = UIStyle.statLabel("Total Blood Units: 0");
        availableUnitsLabel = UIStyle.statLabel("Available Units: 0");
        statsPanel.getChildren().addAll(totalDonorsLabel, totalUnitsLabel, availableUnitsLabel);

        Button refreshBtn = UIStyle.primaryButton("Refresh");
        refreshBtn.setOnAction(e -> refreshDashboard());
        lastUpdatedLabel = new Label("");
        lastUpdatedLabel.setTextFill(javafx.scene.paint.Color.web(UIStyle.MUTED));
        lastUpdatedLabel.setStyle("-fx-font-size: 12px;");
        VBox refreshBox = new VBox(6, refreshBtn, lastUpdatedLabel);
        refreshBox.setAlignment(Pos.CENTER_RIGHT);

        HBox topBar = new HBox(12, statsPanel, refreshBox);
        HBox.setHgrow(statsPanel, Priority.ALWAYS);

        recentTable = new TableView<>();
        UIStyle.applyTableStyle(recentTable);
        configureRecentTable();
        applyZebraRows();

        recentDonorTable = new TableView<>();
        UIStyle.applyTableStyle(recentDonorTable);
        configureRecentDonorTable();
        applyDonorZebraRows();

        // Action buttons for main workflows.
        HBox actionsPanel = new HBox(10);
        actionsPanel.setAlignment(Pos.CENTER);
        Button donorsBtn = UIStyle.primaryButton("Donor Management");
        donorsBtn.setOnAction(e -> DonorListFrame.showWindow());

        Button donationBtn = UIStyle.primaryButton("Record Donation");
        donationBtn.setOnAction(e -> DonationForm.showWindow());

        Button inventoryBtn = UIStyle.primaryButton("Blood Inventory");
        inventoryBtn.setOnAction(e -> InventoryFrame.showWindow());

        Button reportBtn = UIStyle.primaryButton("Reports");
        reportBtn.setOnAction(e -> ReportFrame.showWindow());

        actionsPanel.getChildren().addAll(donorsBtn, donationBtn, inventoryBtn, reportBtn);

        Label txTitle = UIStyle.subtitleLabel("Recent Transactions");
        Label donorTitle = UIStyle.subtitleLabel("Recent Donors");

        VBox centerBox = new VBox(12, txTitle, recentTable, donorTitle, recentDonorTable);
        VBox.setMargin(recentTable, new Insets(0, 0, 0, 0));
        VBox.setMargin(recentDonorTable, new Insets(0, 0, 0, 0));
        VBox.setVgrow(recentTable, Priority.ALWAYS);
        VBox.setVgrow(recentDonorTable, Priority.ALWAYS);

        root.setTop(topBar);
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
        instance.show();
        instance.toFront();
    }

    private void configureRecentTable() {
        TableColumn<RecentTransactionRow, Integer> txIdCol = new TableColumn<>("Transaction ID");
        txIdCol.setCellValueFactory(new PropertyValueFactory<>("transactionId"));

        TableColumn<RecentTransactionRow, String> donorCol = new TableColumn<>("Donor");
        donorCol.setCellValueFactory(new PropertyValueFactory<>("donorName"));

        TableColumn<RecentTransactionRow, Integer> unitCol = new TableColumn<>("Unit ID");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unitId"));

        TableColumn<RecentTransactionRow, Integer> staffCol = new TableColumn<>("Staff ID");
        staffCol.setCellValueFactory(new PropertyValueFactory<>("staffId"));

        TableColumn<RecentTransactionRow, String> dateCol = new TableColumn<>("Transaction Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));

        TableColumn<RecentTransactionRow, String> remarksCol = new TableColumn<>("Remarks");
        remarksCol.setCellValueFactory(new PropertyValueFactory<>("remarks"));

        recentTable.getColumns().addAll(txIdCol, donorCol, unitCol, staffCol, dateCol, remarksCol);
    }

    private void configureRecentDonorTable() {
        TableColumn<Donor, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("donorId"));

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

        recentDonorTable.getColumns().addAll(idCol, nameCol, bloodCol, lastDonationCol, eligibilityCol);
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
                    if (getIndex() % 2 == 0) {
                        setStyle("-fx-background-color: #ffffff;");
                    } else {
                        setStyle("-fx-background-color: #f5f7fa;");
                    }
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
                    if (getIndex() % 2 == 0) {
                        setStyle("-fx-background-color: #ffffff;");
                    } else {
                        setStyle("-fx-background-color: #f5f7fa;");
                    }
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

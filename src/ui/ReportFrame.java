package ui;

import dao.BloodUnitDAO;
import dao.DonorDAO;
import dao.TransactionDAO;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Pagination;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.BloodUnit;
import model.DonationTransaction;
import model.Donor;
import util.ReportExporter;

import java.io.File;
import java.util.List;

// Reports window with multiple tabs (JavaFX).
public class ReportFrame extends Stage {
    private static ReportFrame instance;
    private final DonorDAO donorDAO;
    private final BloodUnitDAO unitDAO;
    private final TransactionDAO txDAO;
    private final TableView<Donor> donorTable;
    private final TableView<BloodUnit> inventoryTable;
    private final TableView<DonationTransaction> transactionTable;
    private final TabPane tabs;
    private final Pagination donorPagination;
    private final Pagination inventoryPagination;
    private final Pagination transactionPagination;
    private static final int PAGE_SIZE = 25;
    private List<Donor> donorData;
    private List<BloodUnit> inventoryData;
    private List<DonationTransaction> transactionData;

    public ReportFrame() {
        donorDAO = new DonorDAO();
        unitDAO = new BloodUnitDAO();
        txDAO = new TransactionDAO();

        setTitle("Reports");

        donorTable = createDonorTable();
        inventoryTable = createInventoryTable();
        transactionTable = createTransactionTable();

        donorPagination = new Pagination(1, 0);
        inventoryPagination = new Pagination(1, 0);
        transactionPagination = new Pagination(1, 0);

        donorPagination.setPageFactory(index -> { updateDonorPage(index); return new VBox(); });
        inventoryPagination.setPageFactory(index -> { updateInventoryPage(index); return new VBox(); });
        transactionPagination.setPageFactory(index -> { updateTransactionPage(index); return new VBox(); });

        updateDonorPage(0);
        updateInventoryPage(0);
        updateTransactionPage(0);

        tabs = new TabPane();
        tabs.getTabs().add(new Tab("Donor List", new VBox(10, donorTable, donorPagination)));
        tabs.getTabs().add(new Tab("Inventory", new VBox(10, inventoryTable, inventoryPagination)));
        tabs.getTabs().add(new Tab("Donation History", new VBox(10, transactionTable, transactionPagination)));
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-tab-min-width: 140;" +
                "-fx-tab-max-height: 40;"
        );

        Button dashboardBtn = UIStyle.secondaryButton("Dashboard", "dashboard");
        dashboardBtn.setOnAction(e -> DashboardFrame.showWindow());

        HBox exportPanel = new HBox(10);
        exportPanel.setAlignment(Pos.CENTER_LEFT);
        Button csvBtn = UIStyle.primaryButton("Export CSV", "report");
        csvBtn.setOnAction(e -> exportCsv());
        Button pdfBtn = UIStyle.secondaryButton("Export PDF", "report");
        pdfBtn.setOnAction(e -> exportPdf());
        exportPanel.getChildren().addAll(csvBtn, pdfBtn);

        BorderPane root = new BorderPane();
        root.setStyle(UIStyle.pageBackground());
        root.setPadding(new Insets(0));

        HBox header = UIStyle.appHeader("Reports", dashboardBtn);
        VBox topWrap = new VBox(10, header, exportPanel);
        topWrap.setPadding(new Insets(0, 0, 8, 0));
        UIStyle.applyPanelStyle(exportPanel);
        exportPanel.setPadding(new Insets(14, 16, 14, 16));

        VBox centerWrap = new VBox(tabs);
        centerWrap.setPadding(new Insets(12));
        UIStyle.applyPanelStyle(centerWrap);

        root.setTop(topWrap);
        root.setCenter(centerWrap);

        Scene scene = new Scene(root, 900, 550);
        setScene(scene);
    }

    public static void showWindow() {
        if (instance == null) {
            instance = new ReportFrame();
            instance.setOnHidden(e -> instance = null);
        }
        instance.setIconified(false);
        instance.show();
        instance.toFront();
    }

    private TableView<Donor> createDonorTable() {
        TableView<Donor> table = new TableView<>();
        UIStyle.applyTableStyle(table);
        applyZebraRows(table);
        table.setPlaceholder(UIStyle.helperLabel("No donor records available for this report."));

        TableColumn<Donor, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("donorId"));
        TableColumn<Donor, String> firstCol = new TableColumn<>("First Name");
        firstCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        TableColumn<Donor, String> lastCol = new TableColumn<>("Last Name");
        lastCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        TableColumn<Donor, String> bloodCol = new TableColumn<>("Blood Type");
        bloodCol.setCellValueFactory(new PropertyValueFactory<>("bloodType"));
        TableColumn<Donor, String> contactCol = new TableColumn<>("Contact");
        contactCol.setCellValueFactory(new PropertyValueFactory<>("contactNo"));
        TableColumn<Donor, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        TableColumn<Donor, java.sql.Date> lastDonationCol = new TableColumn<>("Last Donation");
        lastDonationCol.setCellValueFactory(new PropertyValueFactory<>("lastDonationDate"));
        TableColumn<Donor, String> eligibilityCol = new TableColumn<>("Eligibility");
        eligibilityCol.setCellValueFactory(new PropertyValueFactory<>("eligibilityStatus"));
        UIStyle.applyStatusBadgeColumn(eligibilityCol);

        table.getColumns().addAll(idCol, firstCol, lastCol, bloodCol, contactCol, addressCol, lastDonationCol, eligibilityCol);

        donorData = donorDAO.getAllDonors();
        table.setItems(FXCollections.observableArrayList());
        return table;
    }

    private TableView<BloodUnit> createInventoryTable() {
        TableView<BloodUnit> table = new TableView<>();
        UIStyle.applyTableStyle(table);
        applyZebraRows(table);
        table.setPlaceholder(UIStyle.helperLabel("No inventory records available for this report."));

        TableColumn<BloodUnit, Integer> idCol = new TableColumn<>("Unit ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("unitId"));
        TableColumn<BloodUnit, Integer> donorCol = new TableColumn<>("Donor ID");
        donorCol.setCellValueFactory(new PropertyValueFactory<>("donorId"));
        TableColumn<BloodUnit, String> bloodCol = new TableColumn<>("Blood Type");
        bloodCol.setCellValueFactory(new PropertyValueFactory<>("bloodType"));
        TableColumn<BloodUnit, Integer> volumeCol = new TableColumn<>("Volume");
        volumeCol.setCellValueFactory(new PropertyValueFactory<>("volumeMl"));
        TableColumn<BloodUnit, java.sql.Date> collectionCol = new TableColumn<>("Collection Date");
        collectionCol.setCellValueFactory(new PropertyValueFactory<>("collectionDate"));
        TableColumn<BloodUnit, java.sql.Date> expiryCol = new TableColumn<>("Expiry Date");
        expiryCol.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        TableColumn<BloodUnit, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        UIStyle.applyStatusBadgeColumn(statusCol);

        table.getColumns().addAll(idCol, donorCol, bloodCol, volumeCol, collectionCol, expiryCol, statusCol);

        inventoryData = unitDAO.getAllUnits();
        table.setItems(FXCollections.observableArrayList());
        return table;
    }

    private TableView<DonationTransaction> createTransactionTable() {
        TableView<DonationTransaction> table = new TableView<>();
        UIStyle.applyTableStyle(table);
        applyZebraRows(table);
        table.setPlaceholder(UIStyle.helperLabel("No transaction records available for this report."));

        TableColumn<DonationTransaction, Integer> idCol = new TableColumn<>("Transaction ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        TableColumn<DonationTransaction, Integer> donorCol = new TableColumn<>("Donor ID");
        donorCol.setCellValueFactory(new PropertyValueFactory<>("donorId"));
        TableColumn<DonationTransaction, Integer> unitCol = new TableColumn<>("Unit ID");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unitId"));
        TableColumn<DonationTransaction, Integer> staffCol = new TableColumn<>("Staff ID");
        staffCol.setCellValueFactory(new PropertyValueFactory<>("staffId"));
        TableColumn<DonationTransaction, java.sql.Timestamp> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
        TableColumn<DonationTransaction, String> remarksCol = new TableColumn<>("Remarks");
        remarksCol.setCellValueFactory(new PropertyValueFactory<>("remarks"));

        table.getColumns().addAll(idCol, donorCol, unitCol, staffCol, dateCol, remarksCol);

        transactionData = txDAO.getAllTransactions();
        table.setItems(FXCollections.observableArrayList());
        return table;
    }

    private TableView<?> getActiveTable() {
        int index = tabs.getSelectionModel().getSelectedIndex();
        if (index == 0) {
            return donorTable;
        }
        if (index == 1) {
            return inventoryTable;
        }
        return transactionTable;
    }

    private void exportCsv() {
        TableView<?> table = getActiveTable();
        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName("report.csv");
        File file = chooser.showSaveDialog(this);
        if (file != null) {
            try {
                ReportExporter.exportTableToCSV(table, file);
                new Alert(Alert.AlertType.INFORMATION, "CSV exported successfully.").showAndWait();
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to export CSV.");
                alert.setHeaderText("Error");
                alert.showAndWait();
            }
        }
    }

    private void exportPdf() {
        TableView<?> table = getActiveTable();
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No printer configured.");
            alert.setHeaderText("Error");
            alert.showAndWait();
            return;
        }
        if (job.showPrintDialog(this)) {
            boolean success = job.printPage(table);
            if (success) {
                job.endJob();
                new Alert(Alert.AlertType.INFORMATION,
                        "Print job started. Select 'Microsoft Print to PDF' to save as PDF.").showAndWait();
            } else {
                new Alert(Alert.AlertType.WARNING, "Print job canceled.").showAndWait();
            }
        }
    }

    private <T> void applyZebraRows(TableView<T> table) {
        table.setRowFactory(tv -> new javafx.scene.control.TableRow<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
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

    private void updateDonorPage(int pageIndex) {
        donorData = donorData == null ? donorDAO.getAllDonors() : donorData;
        int pageCount = Math.max(1, (int) Math.ceil((double) donorData.size() / PAGE_SIZE));
        donorPagination.setPageCount(pageCount);
        int from = pageIndex * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, donorData.size());
        donorTable.setItems(FXCollections.observableArrayList(donorData.subList(from, to)));
    }

    private void updateInventoryPage(int pageIndex) {
        inventoryData = inventoryData == null ? unitDAO.getAllUnits() : inventoryData;
        int pageCount = Math.max(1, (int) Math.ceil((double) inventoryData.size() / PAGE_SIZE));
        inventoryPagination.setPageCount(pageCount);
        int from = pageIndex * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, inventoryData.size());
        inventoryTable.setItems(FXCollections.observableArrayList(inventoryData.subList(from, to)));
    }

    private void updateTransactionPage(int pageIndex) {
        transactionData = transactionData == null ? txDAO.getAllTransactions() : transactionData;
        int pageCount = Math.max(1, (int) Math.ceil((double) transactionData.size() / PAGE_SIZE));
        transactionPagination.setPageCount(pageCount);
        int from = pageIndex * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, transactionData.size());
        transactionTable.setItems(FXCollections.observableArrayList(transactionData.subList(from, to)));
    }
}


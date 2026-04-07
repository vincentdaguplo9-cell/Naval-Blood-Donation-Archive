package ui;

import dao.DonorDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Donor;

import java.util.List;
import java.util.Optional;

// Donor management list window (JavaFX).
public class DonorListFrame extends Stage {
    private static DonorListFrame instance;
    private final DonorDAO donorDAO;
    private final TableView<Donor> donorTable;
    private final TextField searchField;
    private final Pagination pagination;
    private static final int PAGE_SIZE = 20;
    private List<Donor> currentDonors;

    public DonorListFrame() {
        donorDAO = new DonorDAO();

        setTitle("Donor Management");

        searchField = new TextField();
        UIStyle.applyInputStyle(searchField);

        Button searchBtn = UIStyle.primaryButton("Search");
        searchBtn.setOnAction(e -> loadDonors(searchField.getText().trim()));

        Button refreshBtn = UIStyle.primaryButton("Refresh");
        refreshBtn.setOnAction(e -> loadDonors(""));

        Button addBtn = UIStyle.primaryButton("Add Donor");
        addBtn.setOnAction(e -> openDonorForm(null));

        Button editBtn = UIStyle.primaryButton("Edit Selected");
        editBtn.setOnAction(e -> editSelected());

        Button deleteBtn = UIStyle.primaryButton("Delete Selected");
        deleteBtn.setOnAction(e -> deleteSelected());

        HBox topPanel = new HBox(8, searchField, searchBtn, refreshBtn);
        topPanel.setAlignment(Pos.CENTER_LEFT);

        HBox btnPanel = new HBox(8, addBtn, editBtn, deleteBtn);
        btnPanel.setAlignment(Pos.CENTER_LEFT);

        donorTable = new TableView<>();
        UIStyle.applyTableStyle(donorTable);
        configureDonorTable();
        applyZebraRows();

        editBtn.disableProperty().bind(donorTable.getSelectionModel().selectedItemProperty().isNull());
        deleteBtn.disableProperty().bind(donorTable.getSelectionModel().selectedItemProperty().isNull());

        pagination = new Pagination(1, 0);
        pagination.setPageFactory(this::createPage);

        VBox centerBox = new VBox(10, donorTable, pagination);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + UIStyle.BG + ";");
        root.setPadding(new Insets(12));
        root.setTop(topPanel);
        root.setCenter(centerBox);
        root.setBottom(btnPanel);

        Scene scene = new Scene(root, 900, 500);
        setScene(scene);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadDonors(newVal.trim()));
        loadDonors("");
    }

    public static void showWindow() {
        if (instance == null) {
            instance = new DonorListFrame();
            instance.setOnHidden(e -> instance = null);
        }
        instance.show();
        instance.toFront();
    }

    private void configureDonorTable() {
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

        donorTable.getColumns().addAll(idCol, firstCol, lastCol, bloodCol, contactCol, addressCol, lastDonationCol, eligibilityCol);
    }

    private void loadDonors(String keyword) {
        currentDonors = keyword == null || keyword.isEmpty() ? donorDAO.getAllDonors() : donorDAO.searchDonors(keyword);
        int pageCount = Math.max(1, (int) Math.ceil((double) currentDonors.size() / PAGE_SIZE));
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(0);
        updatePage(0);
    }

    private void editSelected() {
        Donor donor = donorTable.getSelectionModel().getSelectedItem();
        if (donor == null) {
            new Alert(Alert.AlertType.WARNING, "Select a donor to edit.").showAndWait();
            return;
        }
        Donor full = donorDAO.getDonorById(donor.getDonorId());
        openDonorForm(full);
    }

    private void deleteSelected() {
        Donor donor = donorTable.getSelectionModel().getSelectedItem();
        if (donor == null) {
            new Alert(Alert.AlertType.WARNING, "Select a donor to delete.").showAndWait();
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete selected donor?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (donorDAO.deleteDonor(donor.getDonorId())) {
                new Alert(Alert.AlertType.INFORMATION, "Donor deleted.").showAndWait();
                loadDonors("");
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to delete donor.");
                alert.setHeaderText("Error");
                alert.showAndWait();
            }
        }
    }

    private void openDonorForm(Donor donor) {
        DonorForm.showForm(donor);
        DonorForm.setOnCloseRefresh(this::loadAllDonors);
    }

    private void loadAllDonors() {
        loadDonors("");
    }

    private VBox createPage(Integer pageIndex) {
        updatePage(pageIndex);
        return new VBox();
    }

    private void updatePage(int pageIndex) {
        if (currentDonors == null) {
            donorTable.setItems(FXCollections.observableArrayList());
            return;
        }
        int fromIndex = pageIndex * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, currentDonors.size());
        ObservableList<Donor> rows = FXCollections.observableArrayList(currentDonors.subList(fromIndex, toIndex));
        donorTable.setItems(rows);
    }

    private void applyZebraRows() {
        donorTable.setRowFactory(tv -> new javafx.scene.control.TableRow<Donor>() {
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
}

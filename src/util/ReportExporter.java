package util;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

// Utility for exporting JavaFX TableView data.
public class ReportExporter {

    public static void exportTableToCSV(TableView<?> table, File file) throws IOException {
        ObservableList<? extends TableColumn<?, ?>> columns = table.getColumns();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Write header row.
            for (int col = 0; col < columns.size(); col++) {
                writer.write(escapeCsv(columns.get(col).getText()));
                if (col < columns.size() - 1) {
                    writer.write(",");
                }
            }
            writer.newLine();

            // Write data rows.
            for (Object row : table.getItems()) {
                for (int col = 0; col < columns.size(); col++) {
                    @SuppressWarnings("unchecked")
                    TableColumn<Object, Object> column = (TableColumn<Object, Object>) columns.get(col);
                    Object value = column.getCellData(row);
                    writer.write(escapeCsv(value == null ? "" : value.toString()));
                    if (col < columns.size() - 1) {
                        writer.write(",");
                    }
                }
                writer.newLine();
            }
        }
    }

    private static String escapeCsv(String value) {
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}

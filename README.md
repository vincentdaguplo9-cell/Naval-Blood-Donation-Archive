# Naval Blood Donation Archive System

A JavaFX + MySQL desktop application for hospital blood donation management. It supports donor records, blood unit inventory, donation transactions, and reporting with CSV/PDF export.

## Features
- Admin login (DB-backed `admin_table`)
- Donor management (add/edit/delete/search)
- Donation recording (creates blood unit + transaction)
- Inventory management (status updates)
- Reports (donor list, inventory, donation history)
- CSV export + PDF print (via Print to PDF)

## Tech Stack
- Java 11+ (JavaFX)
- JavaFX GUI
- MySQL (XAMPP compatible)
- JDBC (PreparedStatement)
- MVC-style package structure

## Project Structure
```
src/
 ├── database/
 │      DBConnection.java
 ├── model/
 │      Donor.java
 │      BloodUnit.java
 │      DonationTransaction.java
 ├── dao/
 │      AdminDAO.java
 │      DonorDAO.java
 │      BloodUnitDAO.java
 │      TransactionDAO.java
 ├── ui/
 │      UIStyle.java
 │      LoginFrame.java
 │      DashboardFrame.java
 │      DonorForm.java
 │      DonorListFrame.java
 │      DonationForm.java
 │      InventoryFrame.java
 │      ReportFrame.java
 ├── util/
 │      ReportExporter.java
 └── Main.java
```

## Database Setup (XAMPP MySQL)
1. Start **MySQL** in XAMPP.
2. Open **phpMyAdmin**.
3. Import `blood_archive.sql`.

This will create:
- `donor_table`
- `blood_unit_table`
- `donation_transaction_table`
- `admin_table`

Default admin:
- Username: `admin`
- Password: `admin`

Security updates:
- Admin passwords are now hashed on first successful login.
- After 5 failed attempts, the account locks for 15 minutes.
- If you already imported the database, run `migrations\admin_security.sql` or re-run `blood_archive.sql`.

## Configure DB Connection (Config File)
Edit `config/db.properties` to point to your MySQL server:
```
db.url=jdbc:mysql://YOUR_HOST:3306/blood_archive?useSSL=false&serverTimezone=UTC
db.user=YOUR_USER
db.pass=YOUR_PASSWORD
```

The app reads this file at startup, so you can change databases without recompiling.

## Add MySQL Connector/J (VS Code)
1. Create `lib/` in project root.
2. Place `mysql-connector-j-9.6.0.jar` inside `lib/`.
3. Ensure `.vscode/settings.json` contains:
```json
{
  "java.project.referencedLibraries": [
    "lib/**/*.jar"
  ]
}
```

## Build & Run (Manual)
Install JavaFX SDK and set a `PATH_TO_FX` variable pointing to its `lib` directory.
```bash
set PATH_TO_FX=C:\path\to\javafx-sdk\lib
javac --module-path "%PATH_TO_FX%" --add-modules javafx.controls,javafx.graphics -cp ".;lib\mysql-connector-j-9.6.0.jar" src\Main.java src\database\DBConnection.java src\model\*.java src\dao\*.java src\ui\*.java src\util\*.java
java --module-path "%PATH_TO_FX%" --add-modules javafx.controls,javafx.graphics -cp ".;lib\mysql-connector-j-9.6.0.jar;src" Main
```

## Reports Export
- **CSV Export:** Choose a save location and a `.csv` file is created.
- **PDF Export:** Uses the print dialog. Select **Microsoft Print to PDF**.

## How CRUD Works
The system follows a simple MVC flow:
1. **UI (View)** collects input from staff in JavaFX forms.
2. **DAO (Model/Controller layer)** executes SQL with `PreparedStatement`.
3. **Database** stores and returns records.

### Donor CRUD
- **Create:** `DonorForm` → `DonorDAO.addDonor()` inserts into `donor_table`.
- **Read:** `DonorListFrame` calls `DonorDAO.getAllDonors()` and displays in `JTable`.
- **Update:** `DonorForm` (edit) → `DonorDAO.updateDonor()` updates `donor_table`.
- **Delete:** `DonorListFrame` → `DonorDAO.deleteDonor()` removes from `donor_table`.

### Blood Unit CRUD (Inventory)
- **Create:** `DonationForm` creates a `BloodUnit` and calls `BloodUnitDAO.addAndReturnId()`.
- **Read:** `InventoryFrame` uses `BloodUnitDAO.getAllUnits()` to display in `JTable`.
- **Update:** `InventoryFrame` uses `BloodUnitDAO.updateStatus()` to change unit status.
- **Delete:** Not exposed in UI to preserve audit trail (can be added if needed).

### Donation Transaction CRUD
- **Create:** `DonationForm` calls `TransactionDAO.addTransaction()` after a unit is created.
- **Read:** `ReportFrame` and `DashboardFrame` use `TransactionDAO.getAllTransactions()` / `getRecentTransactions()`.
- **Update/Delete:** Not exposed in UI to preserve donation history integrity.

## Notes
- UI uses a hospital-style theme defined in `src/ui/UIStyle.java`.
- Date inputs use `JSpinner` date pickers.

## Future Enhancements
- Role-based staff accounts
- Password hashing
- PDF generation without print dialog
- Advanced analytics dashboard

## Build Windows MSI (with icon, shortcut, bundled JRE)
This project includes a packaging script that:
- Builds the JAR
- Bundles MySQL Connector/J
- Creates an MSI installer
- Adds a desktop shortcut and Start Menu entry
- Uses a custom app icon

Run:
```powershell
powershell -ExecutionPolicy Bypass -File build\package.ps1 -Version 1.0.0
```

Output:
- `dist\Naval Blood Donation Archive System-1.0.0.msi`

Icon source:
- `assets\app-icon.ico`

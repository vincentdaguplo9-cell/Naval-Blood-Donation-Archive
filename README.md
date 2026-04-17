# Naval Blood Donation Archive System (NBDA)

A JavaFX + MySQL desktop application for a **realistic blood bank workflow** (school project).  
V2 implements **donation session + screening → quarantine → lab testing → release/issue** with traceability.

## Features (V2)
- Admin login (DB-backed `admin_table`)
- Donor management (add/edit/delete/search)
  - Phone standard: always starts with `09`, hard-limited to 11 digits, formatted like `0917-402-1101`
  - Name formatting: Title Case
- Record donation (V2 flow)
  - Creates a donation session + screening
  - If screening passed: creates a unit in `QUARANTINE` with `test_status=PENDING`
- Lab testing (V2)
  - Encode TTI-style results (HIV/HBV/HCV/Syphilis/Malaria)
  - Auto-updates unit to `AVAILABLE` (PASSED) or `DISCARDED` (FAILED)
- Inventory (V2)
  - Filters by `status` and `test_status`
  - Set storage location
  - Issue unit (only `AVAILABLE` + `PASSED`)
- Reports (donor list, inventory, donation history) + CSV export + print-to-PDF
- UI/UX improvements: shared app header, button hierarchy, empty states, scrollable forms

## Tech Stack
- Java + JavaFX
- MySQL (XAMPP compatible)
- JDBC (PreparedStatement)

## Project Structure
```
src/
 ├── database/
 │      DBConnection.java
 │      MigrationRunner.java
 ├── model/
 │      Donor.java
 │      BloodUnit.java
 │      DonationTransaction.java
 ├── dao/
 │      AdminDAO.java
 │      DonorDAO.java
 │      BloodUnitDAO.java
 │      TransactionDAO.java
 │      DonationSessionDAO.java
 │      ScreeningDAO.java
 │      LabTestDAO.java
 │      InventoryTxDAO.java
 ├── ui/
 │      UIStyle.java
 │      LoginFrame.java
 │      DashboardFrame.java
 │      DonorForm.java
 │      DonorListFrame.java
 │      DonationForm.java
 │      LabTestingFrame.java
 │      InventoryFrame.java
 │      ReportFrame.java
 ├── util/
 │      ReportExporter.java
 └── Main.java
```

## Database Setup
### Option A (Recommended): import base schema once + let migrations upgrade
1. Start MySQL in XAMPP
2. Import `blood_archive.sql` (creates V2-ready tables)
3. Run the app; it applies scripts in `migrations/` automatically

### Option B: already have `blood_archive`
- Keep your existing database and run the app once.
- The app runs `MigrationRunner.migrate()` on startup and applies `migrations/*.sql` in filename order.
- Applied migrations are tracked in `schema_migrations`.

Default admin:
- Username: `admin`
- Password: `admin`

## Configure DB Connection
Edit `config/db.properties`:
```
db.url=jdbc:mysql://YOUR_HOST:3306/blood_archive?useSSL=false&serverTimezone=UTC
db.user=YOUR_USER
db.pass=YOUR_PASSWORD
```

## Build & Run
Use `build-run.bat` (recommended).

Manual compile (if needed):
```powershell
set PATH_TO_FX=C:\path\to\javafx-sdk\lib
javac --module-path "%PATH_TO_FX%" --add-modules javafx.controls,javafx.graphics -cp ".;lib\mysql-connector-j-9.6.0.jar" -d build\classes src\Main.java src\database\*.java src\model\*.java src\dao\*.java src\ui\*.java src\util\*.java
java --module-path "%PATH_TO_FX%" --add-modules javafx.controls,javafx.graphics -cp ".;lib\mysql-connector-j-9.6.0.jar;build\classes" Main
```

## V2 Workflow (End-to-End)

### 1) Donor Registration / Maintenance
1. Donor Management → Add Donor
2. Save (phone and name formatting are enforced)

### 2) Donation Session + Screening (Record Donation)
1. Record Donation → select donor, encode staff id, volume, dates, screening result
2. Screening Result:
   - `FAILED` requires a failure reason (no unit created)
   - `PASSED` creates a unit in `QUARANTINE` with `test_status=PENDING`
3. Traceability: a `COLLECT` row is added to `inventory_tx_table`

### 3) Lab Testing (Release or Discard)
1. Lab Testing → select a `PENDING` unit
2. Encode results and save
3. Rule used for school realism:
   - Any `POS` result → overall FAIL
   - Otherwise → overall PASS
4. Result:
   - PASS → unit becomes `AVAILABLE` / `PASSED` and logs `TEST_PASS`
   - FAIL → unit becomes `DISCARDED` / `FAILED` and logs `TEST_FAIL`

### 4) Inventory Management (Filter, Location, Issue)
1. Blood Inventory → filter by status/test
2. Issue allowed only for `AVAILABLE` + `PASSED`
3. Issuance logs an `ISSUE` transaction with a reference number

## Reports Export
- CSV Export saves the active table to a `.csv`
- PDF uses the print dialog (choose Microsoft Print to PDF)

## Detailed Screen Process Guide (V1 + V2 Notes)

### Login (`LoginFrame`)
- UI calls `AdminDAO.validateLogin(username, password)` and checks `admin_table`.

### Dashboard (`DashboardFrame`)
- Loads totals and recent activity.
- Navigation to Donors, Record Donation, Inventory, Lab Testing, Reports.

### Donor Management (`DonorListFrame` + `DonorForm`)
- Full CRUD on `donor_table`.
- Donor form enforces realistic formatting for contact numbers and names.

### Record Donation (`DonationForm`, V2)
- Creates:
  - `donation_session_table`
  - `screening_table`
- If screening PASSED:
  - creates `blood_unit_table` row: `status=QUARANTINE`, `test_status=PENDING`
  - logs `inventory_tx_table`: `COLLECT`
  - writes legacy `donation_transaction_table` for compatibility

### Lab Testing (`LabTestingFrame`, V2)
- Inserts `lab_test_table`
- Updates `blood_unit_table`:
  - PASS → `AVAILABLE` / `PASSED`
  - FAIL → `DISCARDED` / `FAILED`
- Logs `inventory_tx_table`: `TEST_PASS` or `TEST_FAIL`

### Inventory (`InventoryFrame`, V2)
- Filter by `status` and `test_status`.
- Set Location updates `blood_unit_table.storage_location`.
- Issue updates unit to `ISSUED` and logs `inventory_tx_table`: `ISSUE`.

## Change Log (High Level, 2026-04-17)
- UI/UX: confirmation on dashboard close, restore-from-minimize paths, consistent headers/buttons, empty states, scrollable forms
- V2 schema + migration runner + new DAOs
- V2 flow: session/screening, quarantine, lab testing, release/issue, traceability logging

## Docs
- HTML reports and older markdown files are kept in `Documentation/` for reference.

## Known Issue (Environment)
- `build-run.bat` may print `java.nio.file.AccessDeniedException` for `lib/mysql-connector-j-9.6.0.jar` after running even when build/run succeeds (usually a file lock by another process).


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {

    // SQLite keeps the whole database in one file (banking.db) in the project folder
    private static final String URL = "jdbc:sqlite:banking.db";

    private static boolean initialized = false;

    // Every DAO calls this. The first call creates the tables and sample data if needed.
    public static synchronized Connection getConnection() throws SQLException {
        if (!initialized) {
            initializeDatabase();
            initialized = true;
        }
        return openConnection();
    }

    private static Connection openConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(URL);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    private static void initializeDatabase() throws SQLException {

        try (Connection connection = openConnection();
             Statement statement = connection.createStatement()) {

            boolean firstRun = true;

            try (ResultSet rs = statement.executeQuery(
                    "SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'Customers'")) {
                if (rs.next()) {
                    firstRun = false;
                }
            }

            statement.execute(
                "CREATE TABLE IF NOT EXISTS Customers ("
                + "CustomerID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "FirstName VARCHAR(100) NOT NULL, "
                + "Address_ VARCHAR(200) NOT NULL, "
                + "PhoneNumber VARCHAR(20) NOT NULL)");

            statement.execute(
                "CREATE TABLE IF NOT EXISTS Accounts ("
                + "AccountNumber VARCHAR(30) PRIMARY KEY, "
                + "CustomerID INTEGER NOT NULL, "
                + "AccountType VARCHAR(30) NOT NULL, "
                + "Balance DECIMAL(18,2) NOT NULL DEFAULT 0, "
                + "Branch VARCHAR(100) NOT NULL, "
                + "OpeningDate TEXT NOT NULL DEFAULT (date('now')), "
                + "EmployerName VARCHAR(100) NULL, "
                + "EmployerAddress VARCHAR(200) NULL, "
                + "FOREIGN KEY (CustomerID) REFERENCES Customers(CustomerID), "
                + "CHECK (Balance >= 0), "
                + "CHECK (AccountType IN ('Investment', 'Savings', 'Cheque')))");

            statement.execute(
                "CREATE TABLE IF NOT EXISTS Transactions ("
                + "TransactionID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "AccountNumber VARCHAR(30) NOT NULL, "
                + "TransactionType VARCHAR(30) NOT NULL, "
                + "Amount DECIMAL(18,2) NOT NULL, "
                + "TransactionDate TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "BalanceAfter DECIMAL(18,2) NOT NULL, "
                + "FOREIGN KEY (AccountNumber) REFERENCES Accounts(AccountNumber), "
                + "CHECK (Amount > 0), "
                + "CHECK (TransactionType IN ('Deposit', 'Withdrawal', 'Transfer')))");

            // Sample data is inserted only when the database is created for the first time
            if (firstRun) {

                // Customer IDs start at 1001
                statement.executeUpdate("INSERT INTO sqlite_sequence (name, seq) VALUES ('Customers', 1000)");

                statement.executeUpdate(
                    "INSERT INTO Customers (FirstName, Address_, PhoneNumber) VALUES "
                    + "('Kabo', 'Plot 101, Gaborone', '71234501'), "
                    + "('Neo', 'Plot 202, Gaborone', '71234502'), "
                    + "('Thato', 'Plot 303, Francistown', '71234503'), "
                    + "('Lerato', 'Plot 404, Maun', '71234504'), "
                    + "('Tumelo', 'Plot 505, Gaborone', '71234505'), "
                    + "('Boitumelo', 'Plot 606, Palapye', '71234506'), "
                    + "('Kagiso', 'Plot 707, Lobatse', '71234507'), "
                    + "('Naledi', 'Plot 808, Gaborone', '71234508'), "
                    + "('Mpho', 'Plot 909, Serowe', '71234509'), "
                    + "('Onkabetse', 'Plot 1010, Kasane', '71234510')");

                statement.executeUpdate(
                    "INSERT INTO Accounts (AccountNumber, CustomerID, AccountType, Balance, Branch, OpeningDate, EmployerName, EmployerAddress) VALUES "
                    + "('ACC4001', 1001, 'Savings', 1250.00, 'Gaborone Main', '2026-09-01', NULL, NULL), "
                    + "('ACC4002', 1001, 'Cheque', 3800.00, 'Gaborone Main', '2026-09-02', 'Choppies Enterprises', 'Broadhurst, Gaborone'), "
                    + "('ACC4003', 1002, 'Investment', 2200.00, 'Gaborone Main', '2026-09-05', NULL, NULL), "
                    + "('ACC4004', 1003, 'Savings', 400.00, 'Francistown', '2026-09-07', NULL, NULL), "
                    + "('ACC4005', 1004, 'Cheque', 4000.00, 'Maun', '2026-09-10', 'Okavango Lodge', 'Maun'), "
                    + "('ACC4006', 1005, 'Investment', 1500.00, 'Gaborone Main', '2026-09-12', NULL, NULL), "
                    + "('ACC4007', 1006, 'Savings', 150.00, 'Palapye', '2026-09-15', NULL, NULL), "
                    + "('ACC4008', 1007, 'Cheque', 8500.00, 'Lobatse', '2026-09-18', 'Botswana Meat Commission', 'Lobatse'), "
                    + "('ACC4009', 1008, 'Investment', 750.00, 'Gaborone Main', '2026-09-20', NULL, NULL), "
                    + "('ACC4010', 1009, 'Savings', 700.00, 'Serowe', '2026-09-22', NULL, NULL), "
                    + "('ACC4011', 1010, 'Cheque', 3200.00, 'Kasane', '2026-09-25', 'Chobe Safari Lodge', 'Kasane'), "
                    + "('ACC4012', 1002, 'Savings', 250.00, 'Gaborone Main', '2026-09-28', NULL, NULL)");

                statement.executeUpdate(
                    "INSERT INTO Transactions (AccountNumber, TransactionType, Amount, TransactionDate, BalanceAfter) VALUES "
                    + "('ACC4001', 'Deposit', 250.00, '2026-10-01 09:15', 1250.00), "
                    + "('ACC4002', 'Withdrawal', 1200.00, '2026-10-02 10:30', 3800.00), "
                    + "('ACC4003', 'Deposit', 500.00, '2026-10-03 11:00', 2500.00), "
                    + "('ACC4003', 'Withdrawal', 300.00, '2026-10-04 14:20', 2200.00), "
                    + "('ACC4004', 'Deposit', 100.00, '2026-10-05 08:45', 400.00), "
                    + "('ACC4005', 'Withdrawal', 500.00, '2026-10-06 13:10', 4000.00), "
                    + "('ACC4006', 'Deposit', 700.00, '2026-10-07 15:30', 1500.00), "
                    + "('ACC4008', 'Deposit', 2500.00, '2026-10-08 09:00', 8500.00), "
                    + "('ACC4009', 'Withdrawal', 250.00, '2026-10-09 10:45', 750.00), "
                    + "('ACC4010', 'Deposit', 200.00, '2026-10-10 12:00', 700.00), "
                    + "('ACC4012', 'Deposit', 50.00, '2026-10-11 16:05', 250.00)");
            }
        }
    }

    // Quick connection test
    public static void main(String[] args) {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT COUNT(*) AS Total FROM Customers")) {

            System.out.println("Database connection successful!");
            if (rs.next()) {
                System.out.println("Customers in database: " + rs.getInt("Total"));
            }
        } catch (SQLException e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
    }
}

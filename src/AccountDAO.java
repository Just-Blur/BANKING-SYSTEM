import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {

    private static final int FIRST_ACCOUNT_NUMBER = 4001;

    // Next account number: ACC4001, ACC4002, ...
    public String generateNextAccountNumber() throws SQLException {

        String sql = "SELECT MAX(CAST(SUBSTR(AccountNumber, 4) AS INTEGER)) AS MaxNumber "
                   + "FROM Accounts WHERE AccountNumber LIKE 'ACC%'";

        int next = FIRST_ACCOUNT_NUMBER;

        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            if (rs.next()) {
                int max = rs.getInt("MaxNumber");
                if (!rs.wasNull() && max >= FIRST_ACCOUNT_NUMBER) {
                    next = max + 1;
                }
            }
        }
        return String.format("ACC%03d", next);
    }

    // Saves a new account (employer details are stored only for Cheque accounts)
    public void addAccount(Account account) throws SQLException {

        String sql = "INSERT INTO Accounts (AccountNumber, CustomerID, AccountType, Balance, Branch, "
                   + "OpeningDate, EmployerName, EmployerAddress) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        String employerName = null;
        String employerAddress = null;

        if (account instanceof ChequeAccount) {
            ChequeAccount cheque = (ChequeAccount) account;
            employerName = cheque.getEmployerName();
            employerAddress = cheque.getEmployerAddress();
        }

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, account.getAccountNumber());
            statement.setInt(2, account.getCustomer().getCustomerID());
            statement.setString(3, account.getAccountType());
            statement.setBigDecimal(4, BigDecimal.valueOf(account.getBalance()));
            statement.setString(5, account.getBranch());
            statement.setString(6, account.getOpeningDate().toString());
            statement.setString(7, employerName);
            statement.setString(8, employerAddress);

            statement.executeUpdate();
        }
    }

    // Loads every account (with its customer) from the database
    public List<Account> getAllAccounts() throws SQLException {

        List<Account> list = new ArrayList<>();

        String sql = "SELECT a.AccountNumber, a.AccountType, a.Balance, a.Branch, a.OpeningDate, "
                   + "a.EmployerName, a.EmployerAddress, "
                   + "c.CustomerID, c.FirstName, c.Address_, c.PhoneNumber "
                   + "FROM Accounts a JOIN Customers c ON a.CustomerID = c.CustomerID "
                   + "ORDER BY a.AccountNumber";

        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                try {
                    list.add(mapRow(rs));
                } catch (RuntimeException e) {
                    // A row that breaks the account rules is skipped instead of crashing the app
                    System.out.println("Skipped account " + rs.getString("AccountNumber")
                            + ": " + e.getMessage());
                }
            }
        }
        return list;
    }

    // Saves a new balance (used by deposits and withdrawals in the next stage)
    public void updateBalance(String accountNumber, double newBalance) throws SQLException {

        String sql = "UPDATE Accounts SET Balance = ? WHERE AccountNumber = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setBigDecimal(1, BigDecimal.valueOf(newBalance));
            statement.setString(2, accountNumber);
            statement.executeUpdate();
        }
    }

        // Closes an account: removes its transactions first (foreign key), then the account itself.
    // Both deletes happen together or not at all.
    public void deleteAccount(String accountNumber) throws SQLException {

        String deleteTransactionsSql = "DELETE FROM Transactions WHERE AccountNumber = ?";
        String deleteAccountSql = "DELETE FROM Accounts WHERE AccountNumber = ?";

        try (Connection connection = DBConnection.getConnection()) {

            connection.setAutoCommit(false);

            try (PreparedStatement deleteTransactions = connection.prepareStatement(deleteTransactionsSql);
                 PreparedStatement deleteAccount = connection.prepareStatement(deleteAccountSql)) {

                deleteTransactions.setString(1, accountNumber);
                deleteTransactions.executeUpdate();

                deleteAccount.setString(1, accountNumber);
                deleteAccount.executeUpdate();

                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    // Converts one database row into the correct Account subclass
    private Account mapRow(ResultSet rs) throws SQLException {

        Customer customer = new Customer(
                rs.getInt("CustomerID"),
                rs.getString("FirstName"),
                rs.getString("Address_"),
                Integer.parseInt(rs.getString("PhoneNumber").trim()));

        String accountNumber = rs.getString("AccountNumber");
        String type = rs.getString("AccountType");
        double balance = rs.getBigDecimal("Balance").doubleValue();
        String branch = rs.getString("Branch");
        LocalDate openingDate = LocalDate.parse(rs.getString("OpeningDate").substring(0, 10));

        if (type.equalsIgnoreCase("Savings")) {
            return new SavingsAccount(accountNumber, balance, branch, openingDate, customer);
        }

        if (type.equalsIgnoreCase("Investment")) {
            // The constructor insists on a P500 minimum, but a later withdrawal may have
            // lowered the balance, so build it at the minimum and withdraw the difference.
            double minimum = 500.00;
            Account investment = new InvestmentAccount(accountNumber, Math.max(balance, minimum),
                    branch, openingDate, customer);
            if (balance < minimum) {
                investment.withdraw(minimum - balance);
            }
            return investment;
        }

        return new ChequeAccount(accountNumber, balance, branch, openingDate, customer,
                rs.getString("EmployerName"), rs.getString("EmployerAddress"));
    }
}
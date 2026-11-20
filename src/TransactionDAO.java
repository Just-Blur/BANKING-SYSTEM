import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    // Saves the new balance AND the transaction record together.
    // If either step fails, both are rolled back so the database never ends up half-updated.
    // type must be "Deposit" or "Withdrawal" (matches the CHECK constraint on the table).
    public void recordTransaction(String accountNumber, String type, double amount, double balanceAfter)
            throws SQLException {

        String updateSql = "UPDATE Accounts SET Balance = ? WHERE AccountNumber = ?";
        String insertSql = "INSERT INTO Transactions (AccountNumber, TransactionType, Amount, BalanceAfter) "
                         + "VALUES (?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection()) {

            connection.setAutoCommit(false);

            try (PreparedStatement update = connection.prepareStatement(updateSql);
                 PreparedStatement insert = connection.prepareStatement(insertSql)) {

                update.setBigDecimal(1, BigDecimal.valueOf(balanceAfter));
                update.setString(2, accountNumber);
                update.executeUpdate();

                insert.setString(1, accountNumber);
                insert.setString(2, type);
                insert.setBigDecimal(3, BigDecimal.valueOf(amount));
                insert.setBigDecimal(4, BigDecimal.valueOf(balanceAfter));
                insert.executeUpdate();

                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    // Transaction objects for one account, newest first (uses your existing Transaction class)
    public List<Transaction> getTransactions(String accountNumber) throws SQLException {

        List<Transaction> list = new ArrayList<>();

        String sql = "SELECT t.TransactionID, t.AccountNumber, a.AccountType, t.Amount, "
                   + "t.TransactionDate, t.BalanceAfter "
                   + "FROM Transactions t JOIN Accounts a ON t.AccountNumber = a.AccountNumber "
                   + "WHERE t.AccountNumber = ? ORDER BY t.TransactionID DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, accountNumber);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    list.add(new Transaction(
                            rs.getInt("TransactionID"),
                            rs.getString("AccountNumber"),
                            rs.getString("AccountType"),
                            rs.getBigDecimal("Amount").doubleValue(),
                            java.time.LocalDate.parse(rs.getString("TransactionDate").substring(0, 10)),
                            rs.getBigDecimal("BalanceAfter").doubleValue()));
                }
            }
        }
        return list;
    }

    // Ready-to-display history lines for one account, newest first.
    // Your Transaction class has no "type" field, so the type (Deposit/Withdrawal) is added here.
    public List<String> getHistoryLines(String accountNumber) throws SQLException {

        List<String> lines = new ArrayList<>();

        String sql = "SELECT TransactionID, TransactionType, Amount, TransactionDate, BalanceAfter "
                   + "FROM Transactions WHERE AccountNumber = ? ORDER BY TransactionID DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, accountNumber);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    lines.add("Transaction ID: " + rs.getInt("TransactionID")
                            + "\nType: " + rs.getString("TransactionType")
                            + "\nAmount: P" + String.format("%.2f", rs.getBigDecimal("Amount").doubleValue())
                            + "\nDate: " + rs.getString("TransactionDate").substring(0, 10)
                            + "\nBalance After: P" + String.format("%.2f", rs.getBigDecimal("BalanceAfter").doubleValue())
                            + "\n");
                }
            }
        }
        return lines;
    }
}
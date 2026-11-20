import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.sql.SQLException;

public class EnterTransactionController {

    @FXML private ComboBox<Account> accountComboBox;
    @FXML private RadioButton depositRadio;
    @FXML private RadioButton withdrawRadio;
    @FXML private TextField amountField;
    @FXML private Button submitButton;
    @FXML private Button backButton;

    private final TransactionDAO transactionDAO = new TransactionDAO();

    // Runs automatically when the screen opens
    @FXML
    private void initialize() {

        // Only one radio button can be selected at a time
        ToggleGroup typeGroup = new ToggleGroup();
        depositRadio.setToggleGroup(typeGroup);
        withdrawRadio.setToggleGroup(typeGroup);

        // Show "ACC4001 - Savings" in the dropdown
        accountComboBox.setConverter(new StringConverter<Account>() {
            @Override
            public String toString(Account account) {
                if (account == null) return "";
                return account.getAccountNumber() + " - " + account.getAccountType();
            }

            @Override
            public Account fromString(String text) {
                return null;
            }
        });

        accountComboBox.getItems().addAll(ManageAccountsController.accounts);
    }

    @FXML
    private void submitTransaction() {

        Account account = accountComboBox.getValue();

        if (account == null) {
            showAlert(Alert.AlertType.WARNING, "Please select an account.");
            return;
        }

        if (!depositRadio.isSelected() && !withdrawRadio.isSelected()) {
            showAlert(Alert.AlertType.WARNING, "Please choose Deposit or Withdraw.");
            return;
        }

        String amountText = amountField.getText().trim();

        if (amountText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Please enter an amount.");
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);

            String type;

            // The Account class checks the rules (amount > 0, enough funds, no savings withdrawals)
            if (depositRadio.isSelected()) {
                account.deposit(amount);
                type = "Deposit";
            } else {
                account.withdraw(amount);
                type = "Withdrawal";
            }

            try {
                // Saves the new balance and the transaction record together
                transactionDAO.recordTransaction(account.getAccountNumber(), type, amount, account.getBalance());

            } catch (SQLException e) {
                // The save failed, so reload the real balances from the database
                e.printStackTrace();
                reloadAccountsFromDatabase();
                showAlert(Alert.AlertType.ERROR, "Could not save the transaction to the database:\n" + e.getMessage());
                return;
            }

            showAlert(Alert.AlertType.INFORMATION,
                    type + " successful!\nNew balance: P" + String.format("%.2f", account.getBalance()));

            amountField.clear();
            accountComboBox.getSelectionModel().clearSelection();
            depositRadio.setSelected(false);
            withdrawRadio.setSelected(false);

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Please enter a valid amount.");
        } catch (IllegalArgumentException | UnsupportedOperationException e) {
            showAlert(Alert.AlertType.ERROR, e.getMessage());
        }
    }

    // Replaces the in-memory account list with what is really stored in SQL Server
    private void reloadAccountsFromDatabase() {
        try {
            ManageAccountsController.accounts.clear();
            ManageAccountsController.accounts.addAll(new AccountDAO().getAllAccounts());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // BACK BUTTON FUNCTIONALITY
    @FXML
    private void goBack() {

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/resources/fxml/dashboard.fxml"));
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
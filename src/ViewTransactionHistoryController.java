import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.util.List;

public class ViewTransactionHistoryController {

    @FXML private ComboBox<Account> accountComboBox;
    @FXML private Button viewHistoryButton;
    @FXML private Button backButton;

    private final TransactionDAO transactionDAO = new TransactionDAO();

    // Runs automatically when the screen opens
    @FXML
    private void initialize() {

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
    private void viewHistory() {

        Account account = accountComboBox.getValue();

        if (account == null) {
            showAlert(Alert.AlertType.WARNING, "Please select an account.");
            return;
        }

        try {
            List<String> lines = transactionDAO.getHistoryLines(account.getAccountNumber());

            if (lines.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION,
                        "No transactions found for account " + account.getAccountNumber() + ".");
                return;
            }

            StringBuilder history = new StringBuilder();

            for (String line : lines) {
                history.append(line).append("\n");
            }

            showHistoryWindow(account, history.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Could not load the transaction history:\n" + e.getMessage());
        }
    }

    // Long histories scroll inside a text area instead of running off the screen
    private void showHistoryWindow(Account account, String text) {

        TextArea area = new TextArea(text);
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefSize(380, 400);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Transaction History");
        alert.setHeaderText("Account " + account.getAccountNumber() + " (" + account.getAccountType() + ")");
        alert.getDialogPane().setContent(area);
        alert.showAndWait();
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
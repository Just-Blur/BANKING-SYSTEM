import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class ViewBalanceAndInterestController {

    @FXML private ComboBox<Account> accountComboBox;
    @FXML private TextArea balanceArea;
    @FXML private Button backButton;

    // Runs automatically when the screen opens
    @FXML
    private void initialize() {

        balanceArea.setEditable(false);
        balanceArea.setWrapText(true);

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

        // This screen has no "View" button, so the details appear as soon as an account is picked
        accountComboBox.valueProperty().addListener((observable, oldAccount, newAccount) -> showDetails(newAccount));
    }

    private void showDetails(Account account) {

        if (account == null) {
            balanceArea.clear();
            return;
        }

        StringBuilder details = new StringBuilder();

        details.append("Account: ").append(account.getAccountNumber())
               .append("\nType: ").append(account.getAccountType())
               .append("\nBalance: P").append(String.format("%.2f", account.getBalance()));

        // Interest rates and the calculation come from the account type
        if (account instanceof SavingsAccount) {
            double rate = ((SavingsAccount) account).getMonthlyInterestRate();
            appendInterest(details, account, rate);

        } else if (account instanceof InvestmentAccount) {
            double rate = ((InvestmentAccount) account).getMonthlyInterestRate();
            appendInterest(details, account, rate);

        } else {
            details.append("\nMonthly interest: none (cheque accounts earn no interest)");
        }

        balanceArea.setText(details.toString());
    }

    private void appendInterest(StringBuilder details, Account account, double rate) {

        double interest = account.getBalance() * rate;

        details.append("\nMonthly interest rate: ").append(String.format("%.2f", rate * 100)).append("%")
               .append("\nMonthly interest: P").append(String.format("%.2f", interest));
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
}
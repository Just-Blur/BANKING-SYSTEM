import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.TextArea;

public class ManageAccountsController {

    @FXML private Button viewAccountsButton;
    @FXML private Button openAccountButton;
    @FXML private Button closeAccountButton;
    @FXML private Button backButton;

    private final AccountDAO accountDAO = new AccountDAO();

    // In-memory copy of the Accounts table, filled from SQL Server when this class is first loaded
    public static List<Account> accounts = new ArrayList<>();

    static {
        try {
            accounts.addAll(new AccountDAO().getAllAccounts());
        } catch (Exception e) {
            // If the database can't be read, the screen still opens with an empty list
            e.printStackTrace();
        }
    }

    // VIEW ACCOUNTS FUNCTIONALITY
   
@FXML
private void viewAccounts() {

    if (accounts.isEmpty()) {
        showAlert(Alert.AlertType.INFORMATION,
                "No accounts have been registered yet.");
        return;
    }

    StringBuilder details = new StringBuilder();

    for (Account account : accounts) {

        details.append("Account Number: ")
                .append(account.getAccountNumber())
                .append("\nCustomer: ")
                .append(account.getCustomer().getFirstName())
                .append("\nAccount Type: ")
                .append(account.getAccountType())
                .append("\nBalance: P")
                .append(String.format("%.2f", account.getBalance()))
                .append("\nBranch: ")
                .append(account.getBranch());

        // Employer details are shown for Cheque accounts only
        if (account instanceof ChequeAccount) {

            ChequeAccount cheque = (ChequeAccount) account;

            details.append("\nEmployer: ")
                    .append(cheque.getEmployerName())
                    .append("\nEmployer Address: ")
                    .append(cheque.getEmployerAddress());
        }

        details.append("\n\n");
    }

    // Create a scrollable area for displaying account details
    TextArea accountList = new TextArea(details.toString());

    accountList.setEditable(false);
    accountList.setWrapText(true);
    accountList.setPrefWidth(430);
    accountList.setPrefHeight(450);

    // Display accounts in a fixed-size popup
    Alert alert = new Alert(Alert.AlertType.INFORMATION);

    alert.setTitle("Nodwell Banking System");
    alert.setHeaderText("Registered Customer Accounts");

    alert.getDialogPane().setContent(accountList);
    alert.getDialogPane().setPrefWidth(480);

    alert.showAndWait();
}

    // OPEN NEW ACCOUNT FUNCTIONALITY
    @FXML
    private void openNewAccount() {

        if (RegisterCustomerController.customers.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Please register a customer first.");
            return;
        }

        TextInputDialog customerDialog = new TextInputDialog();

        customerDialog.setTitle("Open New Account");
        customerDialog.setHeaderText("Enter Customer ID");
        customerDialog.setContentText("Customer ID:");

        Optional<String> customerResult = customerDialog.showAndWait();

        if (!customerResult.isPresent()) return;

        try {
            int customerID = Integer.parseInt(customerResult.get().trim());

            Customer selectedCustomer = null;

            for (Customer customer : RegisterCustomerController.customers) {
                if (customer.getCustomerID() == customerID) {
                    selectedCustomer = customer;
                    break;
                }
            }

            if (selectedCustomer == null) {
                showAlert(Alert.AlertType.ERROR, "Customer ID not found.");
                return;
            }

            // ACCOUNT TYPE
            TextInputDialog typeDialog = new TextInputDialog();

            typeDialog.setTitle("Account Type");
            typeDialog.setHeaderText("Choose: Investment, Savings or Cheque");

            Optional<String> typeResult = typeDialog.showAndWait();

            if (!typeResult.isPresent()) return;

            String type = typeResult.get().trim();

            if (!type.equalsIgnoreCase("Investment") && !type.equalsIgnoreCase("Savings") && !type.equalsIgnoreCase("Cheque")) {

                showAlert(Alert.AlertType.ERROR, "Invalid account type.");
                return;
            }

            // EMPLOYER DETAILS: only asked when the account type is Cheque
            String employerName = null;
            String employerAddress = null;

            if (type.equalsIgnoreCase("Cheque")) {

                TextInputDialog employerDialog = new TextInputDialog();

                employerDialog.setTitle("Cheque Account");
                employerDialog.setHeaderText("Enter employer name");
                employerDialog.setContentText("Employer name:");

                Optional<String> employerResult = employerDialog.showAndWait();

                if (!employerResult.isPresent()) return;

                employerName = employerResult.get().trim();

                if (employerName.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Employer name is required for a cheque account.");
                    return;
                }

                TextInputDialog employerAddressDialog = new TextInputDialog();

                employerAddressDialog.setTitle("Cheque Account");
                employerAddressDialog.setHeaderText("Enter employer address");
                employerAddressDialog.setContentText("Employer address:");

                Optional<String> addressResult = employerAddressDialog.showAndWait();

                if (!addressResult.isPresent()) return;

                employerAddress = addressResult.get().trim();

                if (employerAddress.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Employer address is required for a cheque account.");
                    return;
                }
            }

            // INITIAL DEPOSIT
            TextInputDialog amountDialog = new TextInputDialog();

            amountDialog.setTitle("Initial Deposit");
            amountDialog.setHeaderText("Enter the initial deposit amount");

            Optional<String> amountResult = amountDialog.showAndWait();

            if (!amountResult.isPresent()) return;

            double amount = Double.parseDouble(amountResult.get().trim());

            // BRANCH
            TextInputDialog branchDialog = new TextInputDialog();

            branchDialog.setTitle("Branch");
            branchDialog.setHeaderText("Enter branch name");

            Optional<String> branchResult = branchDialog.showAndWait();

            if (!branchResult.isPresent()) return;

            String branch = branchResult.get().trim();

            if (branch.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Branch is required.");
                return;
            }

            // Next account number comes from the database (ACC4001, ACC4002, ...)
            String accountNumber = accountDAO.generateNextAccountNumber();

            Account newAccount;

            if (type.equalsIgnoreCase("Investment")) {

                newAccount = new InvestmentAccount(accountNumber, amount, branch, LocalDate.now(), selectedCustomer);

            } else if (type.equalsIgnoreCase("Savings")) {

                newAccount = new SavingsAccount(accountNumber, amount, branch, LocalDate.now(), selectedCustomer);

            } else {

                newAccount = new ChequeAccount(accountNumber, amount, branch, LocalDate.now(), selectedCustomer, employerName, employerAddress);
            }

            // Save to SQL Server first; only add to the list if the save worked
            accountDAO.addAccount(newAccount);
            accounts.add(newAccount);

            showAlert(Alert.AlertType.INFORMATION, "Account opened successfully!\n" + "Account Number: " + accountNumber);

        } catch (NumberFormatException e) {

            showAlert(Alert.AlertType.ERROR, "Please enter valid numerical values.");

        } catch (IllegalArgumentException | UnsupportedOperationException e) {

            showAlert(Alert.AlertType.ERROR, e.getMessage());

        } catch (SQLException e) {

            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Could not save the account to the database:\n" + e.getMessage());
        }
    }

    // CLOSE ACCOUNT FUNCTIONALITY
    @FXML
    private void closeAccount() {

        TextInputDialog dialog = new TextInputDialog();

        dialog.setTitle("Close Account");
        dialog.setHeaderText("Enter account number");
        dialog.setContentText("Account Number:");

        Optional<String> result = dialog.showAndWait();

        if (!result.isPresent()) return;

        Account selectedAccount = null;

        for (Account account : accounts) {
            if (account.getAccountNumber().equalsIgnoreCase(result.get().trim())) {
                selectedAccount = account;
                break;
            }
        }

        if (selectedAccount == null) {
            showAlert(Alert.AlertType.ERROR, "Account not found.");
            return;
        }

        if (selectedAccount.getBalance() != 0) {
            showAlert(Alert.AlertType.WARNING, "An account must have a zero balance before it can be closed.");
            return;
        }

        try {
            accountDAO.deleteAccount(selectedAccount.getAccountNumber());
            accounts.remove(selectedAccount);

            showAlert(Alert.AlertType.INFORMATION, "Account closed successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Could not close the account in the database:\n" + e.getMessage());
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
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;

public class DashboardController {
    @FXML private Button registerCustomerButton;
    @FXML private Button manageAccountsButton;
    @FXML private Button enterTransactionButton;
    @FXML private Button viewTransactionHistoryButton;
    @FXML private Button viewBalanceAndInterestButton;
    @FXML private Button logoutButton;

    @FXML
    private void registerCustomer() {
        loadScene("/resources/fxml/registerCustomer.fxml");
    }

    @FXML
    private void manageAccounts() {
        loadScene("/resources/fxml/ManageAccounts.fxml");
    }

    @FXML
    private void enterTransaction() {
        loadScene("/resources/fxml/EnterTransaction.fxml");
    }

    @FXML
    private void viewTransactionHistory() {
        loadScene("/resources/fxml/ViewTransactionHistory.fxml");
    }

    @FXML
    private void viewBalanceAndInterest() {
        loadScene("/resources/fxml/ViewBalanceAndInterest.fxml");
    }

    @FXML
    private void logout() {
        // Implementation for logout functionality,
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void loadScene(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) registerCustomerButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


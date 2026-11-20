import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RegisterCustomerController {

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField addressField;

    @FXML
    private TextField phoneNumberField;

    @FXML
    private Button registerButton;

    @FXML
    private Button backButton;

    private final CustomerDAO customerDAO = new CustomerDAO();

    // In-memory copy of the Customers table, kept so ManageAccountsController still works unchanged.
    // It is filled from SQL Server when this class is first loaded.
    public static List<Customer> customers = new ArrayList<>();

    static {
        try {
            customers.addAll(new CustomerDAO().getAllCustomers());
        } catch (Exception e) {
            // If the database can't be read, the screen still opens with an empty list
            e.printStackTrace();
        }
    }

    // Method to handle customer registration
    @FXML
    private void registerCustomer() {

        String firstName = firstNameField.getText().trim();
        String address = addressField.getText().trim();
        String phoneText = phoneNumberField.getText().trim();

        if (firstName.isEmpty() || address.isEmpty()
                || phoneText.isEmpty()) {

            showAlert(Alert.AlertType.WARNING, "Please fill in all customer details.");
            return;
        }

        try {
            int phoneNumber = Integer.parseInt(phoneText);

            if (phoneNumber <= 0) {
                showAlert(Alert.AlertType.WARNING, "Enter a valid phone number.");
                return;
            }

            // Save to SQL Server; the database generates the Customer ID
            Customer customer = customerDAO.addCustomer(firstName, address, phoneNumber);

            customers.add(customer);

            showAlert(Alert.AlertType.INFORMATION, "Customer registered successfully!\nCustomer ID: " + customer.getCustomerID());

            firstNameField.clear();
            addressField.clear();
            phoneNumberField.clear();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Phone number must contain digits only.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Could not save customer to the database:\n" + e.getMessage());
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
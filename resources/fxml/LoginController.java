package resources.fxml;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;


import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.scene.control.Alert;
public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML 
    private Button loginButton;

    //----------LOGIN BUTTON------------------
    @FXML
    private void handleLogin() {

        String username = usernameField.getText();
        String password = passwordField.getText();

        
      if (username.equals("BLUR") && password.equals("1223")) {

           try {
            Parent root = FXMLLoader.load(getClass().getResource("/resources/fxml/dashboard.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

            } catch (Exception e) {// to prevent crashing of system when incorrect or invalid input is added
            e.printStackTrace();

            }

        }else {

          Alert error = new Alert(Alert.AlertType.ERROR, "Incorrect Username or Password");//Displayed whenever wrong input apart from hardcoded declared username or password is used
        error.showAndWait();

        }

   }

 }
   



package controller;

import domain.User;
import domain.validators.ServiceException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import repository.database.UtilizatorDbRepository;
import service.Service;

import java.io.IOException;
import java.util.List;

import static utils.Hasher.hashPassword;

public class HelloController {

    @FXML
    private TextField textUsername;

    @FXML
    private PasswordField textPassword;


    @FXML
    private VBox vboxDebug;



    private Service service;



    public void setService(Service service) {
        this.service = service;
     //   load_debug();
    }


    private void load_debug(){
        Button debugUtilizator_ViewButton = new Button("Debug Utilizator");
        Button debugProfile_ViewButton = new Button("Debug Profile");
        vboxDebug.getChildren().addAll(debugUtilizator_ViewButton, debugProfile_ViewButton);
    }

    @FXML
    private void handleLogin() {
        try {
            if(textUsername.getText().isEmpty() || textPassword.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Username and Password are Required");
                alert.showAndWait();
            }

            service.set_password(1L,"123");
            String username = textUsername.getText();
            String password = textPassword.getText();

            password = hashPassword(password);
            System.out.println(password);
            User user = service.LogIn(username, password);

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/utilizator-view.fxml"));
            AnchorPane root = loader.load();
            UtilizatorController utilizatorController = loader.getController();
            utilizatorController.setService(this.service);
            utilizatorController.setUserInfo(user);
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(scene);
            stage.show();

        }
        catch(ServiceException e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
        catch(IOException e){
            e.printStackTrace();
        }

    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/register-window.fxml"));

            AnchorPane root = loader.load();

            RegisterController controller = loader.getController();
            controller.setService(service);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        }catch(IOException e){
            e.printStackTrace();
        }

    }
}
package org.example.socialnetworkapp;

import controller.HelloController;
import controller.LoginController;
import domain.Friendship;
import domain.Message;
import domain.User;
import domain.validators.FriendshipValidator;
import domain.validators.UserValidator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import repository.FriendshipPageRepository;
import repository.PagingRepository;
import repository.Repository;
import repository.database.MessageDBRepository;
import repository.database.PrietenieDbRepository;
import repository.database.UtilizatorDbRepository;
import service.NetworkService;
import service.Service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class HelloApplication extends Application {

    Repository<Long, User> userRepository;
    Service service;
    NetworkService net;

    private boolean testDatabaseConnection(String url, String username, String password) {
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            System.out.println("Database connection successful!");
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void start(Stage stage) throws IOException {

        String username="postgres";
        String password="12345678";
        String url="jdbc:postgresql://localhost:5432/lab_map";

        if (!testDatabaseConnection(url, username, password)) {
            System.err.println("Application cannot start due to database connection issues.");
            return; // Stop the application if the connection fails
        }
        Repository<Long, User> utilizatorRepository = new UtilizatorDbRepository(url,username, password, new UserValidator());

        FriendshipPageRepository<Long, Friendship> prietenieRepository = new PrietenieDbRepository(url, username, password, new FriendshipValidator());
        Repository<Long, Message> messageRepository = new MessageDBRepository(utilizatorRepository,url, username, password);


        service = new Service(utilizatorRepository, prietenieRepository, messageRepository);
        net = new NetworkService(service);


        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/views/hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 400);
        stage.setTitle("Social Media App");
        HelloController controller = fxmlLoader.getController();
        controller.setService(service);
        stage.setScene(scene);
        stage.show();

    }

    private void initView(Stage primaryStage) throws IOException {

    }

    public static void main(String[] args) {
        launch();
    }
}
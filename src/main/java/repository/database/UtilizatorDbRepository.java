package repository.database;


import domain.User;
import domain.validators.Validator;
import repository.Repository;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.*;

public class UtilizatorDbRepository implements Repository<Long, User> {
    private String url;
    private String username;
    private String password;
    private Validator<User> validator;

    public UtilizatorDbRepository(String url, String username, String password, Validator<User> validator) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
    }


    @Override
    public Optional<User> findOne(Long id) {
        String query = "SELECT * FROM users WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                byte[] profile_picture = resultSet.getBytes("imagine_profil");
                String bio = resultSet.getString("bio");
                User utilizator = new User(firstName, lastName, username,password, profile_picture, bio);

                utilizator.setId(id);

                return Optional.of(utilizator);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Iterable<User> findAll() {
        Set<User> users = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM users");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                byte[] profile_picture = resultSet.getBytes("imagine_profil");
                String bio = resultSet.getString("bio");
                User utilizator = new User(firstName, lastName, username, password, profile_picture,bio);
                utilizator.setId(id);
                users.add(utilizator);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public Optional<User> save(User entity) {
        validator.validate(entity);
        String query = "INSERT INTO users (id, first_name, last_name, username, password, imagine_profil, bio) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, entity.getId());
            statement.setString(2, entity.getFirstName());
            statement.setString(3, entity.getLastName());
            statement.setString(4,entity.getUsername());
            statement.setString(5, entity.getPassword());
            statement.setBytes(6, entity.getPicture());
            statement.setString(7, entity.getBio());



            int result = statement.executeUpdate();
            if (result > 0) return Optional.empty();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.of(entity);
    }

    @Override
    public Optional<User> delete(Long id) {
        String query = "DELETE FROM users WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id);
            int result = statement.executeUpdate();

            if (result > 0) return Optional.of(new User("Deleted", "User", "Delted", "", null, null));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("test");

        return Optional.empty();
    }

    @Override
    public Optional<User> update(User entity) {
        validator.validate(entity);
        String query = "UPDATE users SET first_name = ?, last_name = ?, username = ?, password = ?, bio = ?, imagine_profil = ?  WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, entity.getFirstName());
            statement.setString(2, entity.getLastName());
            statement.setString(3,entity.getUsername());
            statement.setString(4, entity.getPassword());
            statement.setString(5, entity.getBio());
            statement.setBytes(6, entity.getPicture());
            statement.setLong(7, entity.getId());
            int result = statement.executeUpdate();
            if (result > 0) return Optional.empty();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.of(entity);
    }
}

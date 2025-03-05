package repository.database;

import domain.Friendship;
import domain.validators.Validator;
import repository.FriendshipPageRepository;
import utils.Pageable.Page;
import utils.Pageable.Pageable;

import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.util.*;

public class PrietenieDbRepository implements FriendshipPageRepository<Long, Friendship> {
    private String url;
    private String username;
    private String password;
    private Validator<Friendship> validator;

    public PrietenieDbRepository(String url, String username, String password, Validator<Friendship> validator) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
    }

    @Override
    public Optional<Friendship> findOne(Long id) {
        String query = "SELECT * FROM friendships WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Long user_id1 = resultSet.getLong("user_id1");
                Long user_id2 = resultSet.getLong("user_id2");
                Long status = resultSet.getLong("status");
                Timestamp friendsFromTimestamp = resultSet.getTimestamp("friendsFrom");

                LocalDateTime friendsFrom = friendsFromTimestamp != null ?
                        friendsFromTimestamp.toLocalDateTime() : null;

                Friendship prietenie = new Friendship(user_id1, user_id2,status, friendsFrom);
                prietenie.setId(id);
                return Optional.of(prietenie);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }



    @Override
    public Iterable<Friendship> findAll() {
        Set<Friendship> friendships = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM friendships");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long user_id1 = resultSet.getLong("user_id1");
                Long user_id2 = resultSet.getLong("user_id2");
                Long status = resultSet.getLong("status");
                Timestamp friendsFromTimestamp = resultSet.getTimestamp("friendsFrom");

                LocalDateTime friendsFrom = friendsFromTimestamp != null ?
                        friendsFromTimestamp.toLocalDateTime() : null;

                Friendship prietenie = new Friendship(user_id1, user_id2,status, friendsFrom);
                prietenie.setId(id);
                friendships.add(prietenie);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friendships;
    }

    @Override
    public Optional<Friendship> save(Friendship entity) {
        validator.validate(entity);
        String query = "INSERT INTO friendships (user_id1, user_id2, status, friendsfrom) VALUES (?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, entity.getFirstUserId());
            statement.setLong(2, entity.getSecondUserId());
            statement.setLong(3, entity.getStatus());

            Timestamp friendsFromTimeStamp = Timestamp.valueOf(entity.getFriendsFrom());
            statement.setTimestamp(4, friendsFromTimeStamp);

            int result = statement.executeUpdate();
            if (result > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        entity.setId(generatedKeys.getLong(1));
                    }
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.of(entity);
    }

    @Override
    public Optional<Friendship> delete(Long id) {
        String query = "DELETE FROM friendships WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id);
            int result = statement.executeUpdate();
            if (result > 0) return Optional.of(new Friendship(-1L, -1L, -1L, null));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Friendship> update(Friendship entity) {
        validator.validate(entity);
        String query2 ="SELECT id FROM friendships WHERE (user_id1 = ? AND user_id2 = ?) OR (user_id1 = ? AND user_id2 = ?)";


        String query = "UPDATE friendships SET user_id1 = ?, user_id2 = ?, status = ?, friendsfrom = ? WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(query);
             PreparedStatement statement2 = connection.prepareStatement(query2)) {


            statement2.setLong(1, entity.getFirstUserId());
            statement2.setLong(2, entity.getSecondUserId());
            statement2.setLong(3, entity.getSecondUserId());
            statement2.setLong(4, entity.getFirstUserId());


            Long friendshipId;

            ResultSet resultSet = statement2.executeQuery();
            if (resultSet.next()) {
                friendshipId = resultSet.getLong("id");
            } else {
                return Optional.of(entity);

            }




            statement.setLong(1, entity.getFirstUserId());
            statement.setLong(2, entity.getSecondUserId());
            statement.setLong(3, entity.getStatus());
            statement.setTimestamp(4, Timestamp.valueOf(entity.getFriendsFrom()));
            statement.setLong(5, friendshipId);



            int result = statement.executeUpdate();
            if (result > 0) return Optional.empty();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.of(entity);
    }

    @Override
    public Page<Friendship> findAllOnPage(Pageable pageable, Long id) {
        List<Friendship> friendships = new ArrayList<>();
        String query = "SELECT * FROM friendships WHERE (user_id1 = ? OR user_id2 = ?) AND status = 3";
        query += " LIMIT ? OFFSET ? ";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id);
            statement.setLong(2, id);
            statement.setInt(3, pageable.getPageSize());
            statement.setInt(4, pageable.getPageNumber() * pageable.getPageSize());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Long Id = resultSet.getLong("id");
                Long user_id1 = resultSet.getLong("user_id1");
                Long user_id2 = resultSet.getLong("user_id2");
                LocalDateTime date = resultSet.getTimestamp("friendsfrom").toLocalDateTime();
                Long pending = resultSet.getLong("status");
                if(user_id1.equals(id)) {
                    Friendship prietenie = new Friendship(user_id1, user_id2, pending,  date);
                    prietenie.setId(Id);
                    friendships.add(prietenie);
                }
                else {
                    Friendship prietenie = new Friendship(user_id2, user_id1, pending, date);
                    prietenie.setId(Id);
                    friendships.add(prietenie);
                }
            }


            return new Page<>(friendships, friendships.size());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Page<Friendship> findAllOnPageRequests(Pageable pageable, Long id) {
        List<Friendship> friendships = new ArrayList<>();
        String query = "SELECT * FROM friendships WHERE user_id2 = ? AND status = 1";
        query += " LIMIT ? OFFSET ? ";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id);
            statement.setInt(2, pageable.getPageSize());
            statement.setInt(3, pageable.getPageNumber() * pageable.getPageSize());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Long Id = resultSet.getLong("id");
                Long user_id1 = resultSet.getLong("user_id1");
                Long user_id2 = resultSet.getLong("user_id2");
                LocalDateTime date = resultSet.getTimestamp("friendsfrom").toLocalDateTime();
                Long pending = resultSet.getLong("status");
                if(pending == 1)
                {
                    Friendship f = new Friendship(user_id1, user_id2, pending,  date);
                    f.setId(Id);
                    friendships.add(f);
                }
            }


            return new Page<>(friendships, friendships.size());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;    }

}
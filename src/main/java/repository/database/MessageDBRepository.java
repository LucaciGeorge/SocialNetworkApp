package repository.database;


import domain.Message;
import domain.User;
import repository.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MessageDBRepository implements Repository<Long, Message> {
    private final Repository<Long, User> userRepository;
    private final String url;
    private final String username;
    private final String password;

    public MessageDBRepository(Repository<Long, User> userRepository, String url, String username, String password) {
        this.userRepository = userRepository;
        this.url = url;
        if(userRepository == null){
            System.out.println("userRepository is null");
        }
        this.username = username;
        this.password = password;
    }

    public Optional<Message> findOneNoReply(Long id) {
        String query = "SELECT * FROM messages WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(query);
        ) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Long senderid = resultSet.getLong("senderid");
                Long receiverid = resultSet.getLong("receiverid");
                String message = resultSet.getString("message");
                LocalDateTime date = resultSet.getTimestamp("date").toLocalDateTime();
                Message msg = new Message(userRepository.findOne(senderid).get(), userRepository.findOne(receiverid).get(), message, date);
                msg.setId(id);
                return Optional.of(msg);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Message> findOne(Long id) {
        Message msg;
        if (findOneNoReply(id).isPresent()) {
            msg = findOneNoReply(id).get();
        } else return Optional.empty();

        String query = "SELECT * FROM messages WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(query);
        ) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            Long reply_id = resultSet.getLong("resultid");
            if (!resultSet.next()) {
                Message replyMessage;
                if (findOneNoReply(reply_id).isPresent()) {
                    replyMessage = findOneNoReply(reply_id).get();
                } else return Optional.empty();

                msg.setReply(replyMessage);
                return Optional.of(msg);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public Iterable<Message> findAll() {
        List<Message> messages = new ArrayList<>();
        String query = "SELECT * FROM messages";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(query);
        ) {
            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long senderid = resultSet.getLong("senderid");
                Long receiverid = resultSet.getLong("receiverid");
                LocalDateTime date = resultSet.getTimestamp("date").toLocalDateTime();
                String message = resultSet.getString("message");
                Long replyid = resultSet.getLong("reply_id");
                User sender = userRepository.findOne(senderid).get();
                User receivers = userRepository.findOne(receiverid).get();
                Message msg = new Message(sender, receivers, message, date);
                msg.setId(id);
                messages.add(msg);
            }

        } catch(SQLException e) {
            throw new RuntimeException(e);
        }
        return messages;
    }

    @Override
    public Optional<Message> save(Message entity) {
        String query = "INSERT INTO messages (senderid, receiverid, date, message, reply_id) VALUES (?,?,?,?,?)";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(query);) {
            statement.setLong(1, entity.getSender().getId());
            statement.setLong(2, entity.getReceivers().getId());
            statement.setTimestamp(3, Timestamp.valueOf(entity.getDate()));
            statement.setString(4, entity.getText());
            if(entity.getReply() == null) {
                statement.setNull(5, Types.NULL);
            } else statement.setLong(5, entity.getReply().getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.of(entity);
    }

    @Override
    public Optional<Message> delete(Long aLong) {
        String query = "DELETE FROM messages WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(query);
        ) {
            statement.setLong(1, aLong);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Message> update(Message entity) {
        String query = "UPDATE messages SET senderid = ?, receiverid = ?, date = ?, message = ?, reply_id = ? WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(query);
        ) {
            statement.setLong(1, entity.getSender().getId());
            statement.setLong(2, entity.getReceivers().getId());
            statement.setTimestamp(3, Timestamp.valueOf(entity.getDate()));
            statement.setString(4, entity.getText());
            statement.setLong(5, entity.getReply().getId());
            statement.setLong(6, entity.getId());
            statement.executeUpdate();
            return Optional.of(entity);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}

package ru.job4j.grabber;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {
    private final Connection cnn;

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("driver-class-name"));
            cnn = DriverManager.getConnection(
                    cfg.getProperty("url"),
                    cfg.getProperty("username"),
                    cfg.getProperty("password")
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement statement = cnn.prepareStatement(
                "INSERT INTO post(name, text, link, created) VALUES (?, ?, ?, ?) ON CONFLICT(link) DO NOTHING"
        )) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.execute();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> result = new ArrayList<>();
        try (PreparedStatement statement = cnn.prepareStatement(
                "SELECT p.id, p.name, p.text, p.link, p.created FROM post p ORDER BY p.id");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                result.add(postFromQueryResult(resultSet));
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return result;
    }

    @Override
    public Post findById(int id) {
        Post result = null;
        try (PreparedStatement statement = cnn.prepareStatement(
                "SELECT p.id, p.name, p.text, p.link, p.created FROM post p WHERE p.id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    result = postFromQueryResult(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return result;
    }

    private Post postFromQueryResult(ResultSet result) throws SQLException {
        return new Post(
                result.getInt("id"),
                result.getString("name"),
                result.getString("link"),
                result.getString("text"),
                result.getTimestamp("created").toLocalDateTime()
        );
    }

    @Override
    public void close() throws SQLException {
        if (cnn != null) {
            cnn.close();
        }
    }
}

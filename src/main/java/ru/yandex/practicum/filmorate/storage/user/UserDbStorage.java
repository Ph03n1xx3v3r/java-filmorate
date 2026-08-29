package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getObject("birthday", LocalDate.class));
        return user;
    };

    @Override
    public List<User> getAllUsers() {
        String sql = "SELECT * FROM users ORDER BY id";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    @Override
    public Optional<User> getUser(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, id);
            return Optional.of(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public User addUser(User user) {
        String sql = """
            INSERT INTO users (email, login, name, birthday)
            VALUES (?, ?, ?, ?)
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setObject(4, user.getBirthday());
            return ps;
        }, keyHolder);
        user.setId(keyHolder.getKey().longValue());
        return user;
    }

    @Override
    public User updateUser(User user) {
        String sql = """
            UPDATE users
            SET email = ?, login = ?, name = ?, birthday = ?
            WHERE id = ?
            """;
        int updated = jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );
        if (updated == 0) {
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }
        return user;
    }

    @Override
    public void deleteUser(Long id) {
        int deleted = jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
        if (deleted == 0) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        jdbcTemplate.update("INSERT INTO friends (user_id, friend_id) VALUES (?, ?)", userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        int deleted = jdbcTemplate.update("DELETE FROM friends WHERE user_id = ? AND friend_id = ?", userId, friendId);
        if (deleted == 0) {
            throw new NotFoundException("Друг не найден");
        }
    }

    @Override
    public List<User> getFriends(Long userId) {
        String sql = """
            SELECT u.*
            FROM friends f
            JOIN users u ON f.friend_id = u.id
            WHERE f.user_id = ?
            """;
        return jdbcTemplate.query(sql, userRowMapper, userId);
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        String sql = """
            SELECT u.*
            FROM friends f1
            JOIN friends f2 ON f1.friend_id = f2.friend_id
            JOIN users u ON f1.friend_id = u.id
            WHERE f1.user_id = ? AND f2.user_id = ?
            """;
        return jdbcTemplate.query(sql, userRowMapper, userId, otherId);
    }
}
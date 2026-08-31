package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Sql(scripts = "/schema.sql")
public class UserDbStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UserDbStorage userStorage;

    @BeforeEach
    void setUp() {
        userStorage = new UserDbStorage(jdbcTemplate);
    }

    @Test
    void testAddAndGetUser() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testUser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userStorage.addUser(user);
        Optional<User> found = userStorage.getUser(created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@mail.ru");
        assertThat(found.get().getLogin()).isEqualTo("testUser");
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        user.setEmail("update@mail.ru");
        user.setLogin("updateUser");
        user.setName("Old Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userStorage.addUser(user);
        created.setName("New Name");
        userStorage.updateUser(created);

        Optional<User> updated = userStorage.getUser(created.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo("New Name");
    }

    @Test
    void testDeleteUser() {
        User user = new User();
        user.setEmail("delete@mail.ru");
        user.setLogin("deleteUser");
        user.setName("Delete User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userStorage.addUser(user);
        userStorage.deleteUser(created.getId());

        Optional<User> deleted = userStorage.getUser(created.getId());
        assertThat(deleted).isEmpty();
    }

    @Test
    void testAddAndGetFriends() {
        User user1 = new User();
        user1.setEmail("user1@mail.ru");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@mail.ru");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1991, 2, 2));

        User created1 = userStorage.addUser(user1);
        User created2 = userStorage.addUser(user2);

        userStorage.addFriend(created1.getId(), created2.getId());

        List<User> friends = userStorage.getFriends(created1.getId());
        assertThat(friends).hasSize(1);
        assertThat(friends.get(0).getId()).isEqualTo(created2.getId());
    }

    @Test
    void testGetCommonFriends() {
        User user1 = new User();
        user1.setEmail("common1@mail.ru");
        user1.setLogin("common1");
        user1.setName("Common One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("common2@mail.ru");
        user2.setLogin("common2");
        user2.setName("Common Two");
        user2.setBirthday(LocalDate.of(1991, 2, 2));

        User user3 = new User();
        user3.setEmail("common3@mail.ru");
        user3.setLogin("common3");
        user3.setName("Common Three");
        user3.setBirthday(LocalDate.of(1992, 3, 3));

        User u1 = userStorage.addUser(user1);
        User u2 = userStorage.addUser(user2);
        User u3 = userStorage.addUser(user3);

        userStorage.addFriend(u1.getId(), u3.getId());
        userStorage.addFriend(u2.getId(), u3.getId());

        List<User> commonFriends = userStorage.getCommonFriends(u1.getId(), u2.getId());
        assertThat(commonFriends).hasSize(1);
        assertThat(commonFriends.get(0).getId()).isEqualTo(u3.getId());
    }
}
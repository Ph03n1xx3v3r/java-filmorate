package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.User;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmorateApplicationTests {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void contextLoads() {
    }

    // ========== ТЕСТЫ ДЛЯ USER ==========

    @Test
    void shouldValidateCorrectUser() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        var violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWhenUserEmailIsBlank() {
        User user = new User();
        user.setEmail("");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.now());

        var violations = validator.validate(user);
        assertEquals(1, violations.size());
        assertEquals("Email не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailWhenUserEmailIsInvalid() {
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.now());

        var violations = validator.validate(user);
        assertEquals(1, violations.size());
        assertEquals("Email должен быть корректным", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailWhenUserLoginIsBlank() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("");
        user.setName("Name");
        user.setBirthday(LocalDate.now());

        var violations = validator.validate(user);
        assertEquals(2, violations.size());
    }

    @Test
    void shouldFailWhenUserLoginContainsSpaces() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("log in");
        user.setName("Name");
        user.setBirthday(LocalDate.now());

        var violations = validator.validate(user);
        assertEquals(1, violations.size());
        assertEquals("Логин не должен содержать пробелы", violations.iterator().next().getMessage());
    }

    @Test
    void shouldAllowEmptyUserNameAndReplaceWithLogin() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("login");
        user.setName(null);
        user.setBirthday(LocalDate.now());

        var violations = validator.validate(user);
        assertTrue(violations.isEmpty());

        user.setName("");
        assertEquals("login", user.getName());
    }

    @Test
    void shouldFailWhenUserBirthdayIsInFuture() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.now().plusDays(1));

        var violations = validator.validate(user);
        assertEquals(1, violations.size());
        assertEquals("Дата рождения не может быть в будущем", violations.iterator().next().getMessage());
    }

    @Test
    void shouldAllowUserBirthdayToday() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.now());

        var violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }
}
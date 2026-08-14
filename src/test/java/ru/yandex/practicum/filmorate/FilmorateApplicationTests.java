package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.OnCreate;

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

    @Test
    void shouldValidateCorrectFilm() {
        Film film = new Film();
        film.setName("Начало");
        film.setDescription("Фантастический боевик");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);

        var violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWhenFilmNameIsBlank() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120);

        var violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertEquals("Название не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailWhenFilmDescriptionTooLong() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("a".repeat(201));
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120);

        var violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertEquals("Описание не должно превышать 200 символов", violations.iterator().next().getMessage());
    }

    @Test
    void shouldAllowFilmDescriptionExactly200Chars() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("a".repeat(200));
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120);

        var violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWhenFilmReleaseDateBefore1895() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(120);

        var violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года",
                violations.iterator().next().getMessage());
    }

    @Test
    void shouldAllowFilmReleaseDateExactly1895_12_28() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(120);

        var violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWhenFilmDurationIsZero() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(0);

        var violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertEquals("Продолжительность должна быть положительным числом",
                violations.iterator().next().getMessage());
    }

    @Test
    void shouldFailWhenFilmDurationIsNegative() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(-10);

        var violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertEquals("Продолжительность должна быть положительным числом",
                violations.iterator().next().getMessage());
    }

    @Test
    void shouldValidateCorrectUser() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        var violations = validator.validate(user, OnCreate.class);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWhenUserEmailIsBlank() {
        User user = new User();
        user.setEmail("");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.now());

        var violations = validator.validate(user, OnCreate.class);
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

        var violations = validator.validate(user, OnCreate.class);
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

        var violations = validator.validate(user, OnCreate.class);
        assertEquals(2, violations.size()); // @NotBlank и @Pattern
    }

    @Test
    void shouldFailWhenUserLoginContainsSpaces() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("log in");
        user.setName("Name");
        user.setBirthday(LocalDate.now());

        var violations = validator.validate(user, OnCreate.class);
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

        var violations = validator.validate(user, OnCreate.class);
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

        var violations = validator.validate(user, OnCreate.class);
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
        
        var violations = validator.validate(user, OnCreate.class);
        assertTrue(violations.isEmpty());
    }
}
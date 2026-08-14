package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import ru.yandex.practicum.filmorate.validation.OnCreate;
import ru.yandex.practicum.filmorate.validation.OnUpdate;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
public class User {
    private Long id;

    @NotBlank(groups = OnCreate.class, message = "Email не может быть пустым")
    @Email(groups = {OnCreate.class, OnUpdate.class}, message = "Email должен быть корректным")
    private String email;

    @NotBlank(groups = OnCreate.class, message = "Логин не может быть пустым")
    @Pattern(regexp = "^\\S+$", message = "Логин не должен содержать пробелы")
    private String login;

    private String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    public void setName(String name) {
        this.name = (name == null || name.isBlank()) ? this.login : name;
    }
}
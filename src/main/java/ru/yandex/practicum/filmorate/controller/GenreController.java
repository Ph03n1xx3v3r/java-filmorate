package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/genres")
@Slf4j
public class GenreController {
    private static final List<Map<String, Object>> GENRES = List.of(
            Map.of("id", 1, "name", "Комедия"),
            Map.of("id", 2, "name", "Драма"),
            Map.of("id", 3, "name", "Боевик"),
            Map.of("id", 4, "name", "Триллер"),
            Map.of("id", 5, "name", "Мелодрама"),
            Map.of("id", 6, "name", "Фантастика")
    );

    @GetMapping
    public List<Map<String, Object>> getAll() {
        return GENRES;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getGenre(@PathVariable Long id) {
        return GENRES.stream()
                .filter(g -> g.get("id").equals(id))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Жанр не найден"));
    }
}
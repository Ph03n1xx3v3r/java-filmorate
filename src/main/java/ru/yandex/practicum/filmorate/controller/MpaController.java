package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mpa")
@Slf4j
public class MpaController {
    private static final List<Map<String, Object>> MPA_LIST = List.of(
            Map.of("id", 1, "name", "G"),
            Map.of("id", 2, "name", "PG"),
            Map.of("id", 3, "name", "PG-13"),
            Map.of("id", 4, "name", "R"),
            Map.of("id", 5, "name", "NC-17")
    );

    @GetMapping
    public List<Map<String, Object>> getAll() {
        return MPA_LIST;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getMpa(@PathVariable Long id) {
        return MPA_LIST.stream()
                .filter(m -> m.get("id").equals(id))
                .findFirst()
                .orElseThrow(() -> new ValidationException("MPA не найден"));
    }
}
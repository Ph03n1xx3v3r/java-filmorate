package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/directors")
@Slf4j
public class DirectorController {
    private final Map<Long, Map<String, Object>> directors = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    @GetMapping
    public List<Map<String, Object>> getAll() {
        return List.copyOf(directors.values());
    }

    @GetMapping("/{id}")
    public Map<String, Object> getDirector(@PathVariable Long id) {
        if (!directors.containsKey(id)) {
            throw new ValidationException("Режиссёр не найден");
        }
        return directors.get(id);
    }

    @PostMapping
    public Map<String, Object> addDirector(@RequestBody Map<String, Object> director) {
        if (director.get("name") == null || director.get("name").toString().isBlank()) {
            throw new ValidationException("Имя режиссёра не может быть пустым");
        }
        Long id = idCounter.getAndIncrement();
        director.put("id", id);
        directors.put(id, director);
        return director;
    }

    @PutMapping
    public Map<String, Object> updateDirector(@RequestBody Map<String, Object> director) {
        if (director.get("id") == null) {
            throw new ValidationException("Id должен быть указан");
        }
        Long id = ((Number) director.get("id")).longValue();
        if (!directors.containsKey(id)) {
            throw new ValidationException("Режиссёр не найден");
        }
        directors.put(id, director);
        return director;
    }

    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable Long id) {
        if (!directors.containsKey(id)) {
            throw new ValidationException("Режиссёр не найден");
        }
        directors.remove(id);
    }
}
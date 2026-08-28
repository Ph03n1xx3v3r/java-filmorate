package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private long nextId = 1L;

    @Override
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film getFilm(Long id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
        return films.get(id);
    }

    @Override
    public Film addFilm(Film film) {
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Добавлен фильм: {}", film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (film.getId() == null) {
            throw new IllegalArgumentException("Id должен быть указан");
        }
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Фильм с таким id не существует");
        }
        films.put(film.getId(), film);
        log.info("Обновлён фильм: {}", film);
        return film;
    }

    @Override
    public void deleteFilm(Long id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
        films.remove(id);
        log.info("Удалён фильм с id={}", id);
    }
}
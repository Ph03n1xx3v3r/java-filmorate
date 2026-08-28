package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private static final int DEFAULT_POPULAR_COUNT = 10;

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film getFilm(Long id) {
        return filmStorage.getFilm(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public void deleteFilm(Long id) {
        filmStorage.deleteFilm(id);
    }

    public void addLike(Long filmId, Long userId) {
        userStorage.getUser(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        userStorage.getUser(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopular(Integer count) {
        int limit = (count == null || count <= 0) ? DEFAULT_POPULAR_COUNT : count;
        return filmStorage.getPopular(limit);
    }
}
package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
<<<<<<< Updated upstream
    private final InMemoryFilmStorage filmStorage;
=======
    private static final int DEFAULT_POPULAR_COUNT = 10;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
>>>>>>> Stashed changes

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
<<<<<<< Updated upstream
=======
        userStorage.getUser(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
>>>>>>> Stashed changes
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
<<<<<<< Updated upstream
=======
        userStorage.getUser(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
>>>>>>> Stashed changes
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopular(Integer count) {
<<<<<<< Updated upstream
        return filmStorage.getPopular(count);
    }

    public List<Film> searchFilms(String query, String by) {
        return filmStorage.searchFilms(query, by);
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        return filmStorage.getCommonFilms(userId, friendId);
    }

    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        return filmStorage.getFilmsByDirector(directorId, sortBy);
=======
        int limit = (count == null || count <= 0) ? DEFAULT_POPULAR_COUNT : count;
        return filmStorage.getPopular(limit);
>>>>>>> Stashed changes
    }
}
package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private static final int DEFAULT_POPULAR_COUNT = 10;

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final Map<Long, Set<Long>> likes = new HashMap<>();

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film getFilm(Long id) {
        return filmStorage.getFilm(id);
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public void deleteFilm(Long id) {
        filmStorage.deleteFilm(id);
        likes.remove(id);
    }

    public void addLike(Long filmId, Long userId) {
        // Проверяем существование
        userStorage.getUser(userId);
        filmStorage.getFilm(filmId);
        likes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        userStorage.getUser(userId);
        filmStorage.getFilm(filmId);
        Set<Long> filmLikes = likes.get(filmId);
        if (filmLikes == null || !filmLikes.contains(userId)) {
            throw new NotFoundException("Лайк не найден");
        }
        filmLikes.remove(userId);
        log.info("Пользователь {} удалил лайк у фильма {}", userId, filmId);
    }

    public List<Film> getPopular(Integer count) {
        int limit = (count == null || count <= 0) ? DEFAULT_POPULAR_COUNT : count;
        return filmStorage.getAllFilms().stream()
                .sorted((f1, f2) -> {
                    int l1 = likes.getOrDefault(f1.getId(), Collections.emptySet()).size();
                    int l2 = likes.getOrDefault(f2.getId(), Collections.emptySet()).size();
                    return Integer.compare(l2, l1);
                })
                .limit(limit)
                .collect(Collectors.toList());
    }
}
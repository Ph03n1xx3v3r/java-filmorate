package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final Map<Long, Film> films = new HashMap<>();
    private final Map<Long, Set<Long>> likes = new HashMap<>();
    private final Map<Long, Map<String, Object>> mpaStorage;
    private final Map<Long, Map<String, Object>> genreStorage;
    private long nextId = 1L;

    public FilmService() {
        mpaStorage = new HashMap<>();
        mpaStorage.put(1L, Map.of("id", 1, "name", "G"));
        mpaStorage.put(2L, Map.of("id", 2, "name", "PG"));
        mpaStorage.put(3L, Map.of("id", 3, "name", "PG-13"));
        mpaStorage.put(4L, Map.of("id", 4, "name", "R"));
        mpaStorage.put(5L, Map.of("id", 5, "name", "NC-17"));

        genreStorage = new HashMap<>();
        genreStorage.put(1L, Map.of("id", 1, "name", "Комедия"));
        genreStorage.put(2L, Map.of("id", 2, "name", "Драма"));
        genreStorage.put(3L, Map.of("id", 3, "name", "Боевик"));
        genreStorage.put(4L, Map.of("id", 4, "name", "Триллер"));
        genreStorage.put(5L, Map.of("id", 5, "name", "Мелодрама"));
        genreStorage.put(6L, Map.of("id", 6, "name", "Фантастика"));
    }

    public List<Film> getAllFilms() {
        List<Film> result = new ArrayList<>(films.values());
        result.forEach(this::enrichFilmForResponse);
        return result;
    }

    public Film getFilm(Long id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
        Film film = films.get(id);
        enrichFilmForResponse(film);
        return film;
    }

    public Film addFilm(Film film) {
        enrichFilm(film);
        film.setId(nextId++);
        films.put(film.getId(), film);
        likes.put(film.getId(), new HashSet<>());
        enrichFilmForResponse(film);
        log.info("Добавлен фильм: {}", film);
        return film;
    }

    public Film updateFilm(Film film) {
        if (film.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Фильм с таким id не существует");
        }
        enrichFilm(film);
        films.put(film.getId(), film);
        enrichFilmForResponse(film);
        log.info("Обновлён фильм: {}", film);
        return film;
    }

    public void deleteFilm(Long id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
        films.remove(id);
        likes.remove(id);
        log.info("Удалён фильм с id={}", id);
    }

    private void enrichFilm(Film film) {
        if (film.getMpa() != null && film.getMpa().get("id") != null) {
            Long mpaId = ((Number) film.getMpa().get("id")).longValue();
            Map<String, Object> fullMpa = mpaStorage.get(mpaId);
            if (fullMpa != null) {
                film.setMpa(fullMpa);
            }
        }
        if (film.getGenres() != null) {
            List<Map<String, Object>> fullGenres = film.getGenres().stream()
                    .map(g -> {
                        Long id = ((Number) g.get("id")).longValue();
                        return genreStorage.get(id);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            film.setGenres(fullGenres);
        }
    }

    private void enrichFilmForResponse(Film film) {
        if (film.getGenres() == null) {
            film.setGenres(Collections.emptyList());
        }
        if (film.getDirectors() == null) {
            film.setDirectors(Collections.emptyList());
        }
        if (film.getMpa() == null) {
            // Устанавливаем дефолтный MPA, если его нет
            film.setMpa(Map.of("id", 1, "name", "G"));
        }
    }

    public void addLike(Long filmId, Long userId) {
        if (!films.containsKey(filmId)) {
            throw new NotFoundException("Фильм не найден");
        }
        likes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        if (!films.containsKey(filmId)) {
            throw new NotFoundException("Фильм не найден");
        }
        Set<Long> filmLikes = likes.get(filmId);
        if (filmLikes == null || !filmLikes.contains(userId)) {
            throw new NotFoundException("Лайк не найден");
        }
        filmLikes.remove(userId);
        log.info("Пользователь {} удалил лайк у фильма {}", userId, filmId);
    }

    public List<Film> getPopular(Integer count, Long genreId, Integer year) {
        int limit = (count == null || count <= 0) ? 10 : count;
        List<Film> popular = films.values().stream()
                .sorted((f1, f2) -> {
                    int likes1 = likes.getOrDefault(f1.getId(), Collections.emptySet()).size();
                    int likes2 = likes.getOrDefault(f2.getId(), Collections.emptySet()).size();
                    return Integer.compare(likes2, likes1);
                })
                .limit(limit)
                .collect(Collectors.toList());
        popular.forEach(this::enrichFilmForResponse);
        return popular;
    }

    public List<Film> searchFilms(String query, String by) {
        if (query == null || query.isBlank()) {
            List<Film> result = new ArrayList<>(films.values());
            result.forEach(this::enrichFilmForResponse);
            return result;
        }
        String lowerQuery = query.toLowerCase();
        List<Film> result = films.values().stream()
                .filter(f -> f.getName().toLowerCase().contains(lowerQuery) ||
                        f.getDescription().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
        result.forEach(this::enrichFilmForResponse);
        return result;
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        List<Film> result = films.values().stream()
                .filter(f -> {
                    Set<Long> filmLikes = likes.getOrDefault(f.getId(), Collections.emptySet());
                    return filmLikes.contains(userId) && filmLikes.contains(friendId);
                })
                .collect(Collectors.toList());
        result.forEach(this::enrichFilmForResponse);
        return result;
    }

    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        // Заглушка – возвращаем пустой список
        return Collections.emptyList();
    }
}
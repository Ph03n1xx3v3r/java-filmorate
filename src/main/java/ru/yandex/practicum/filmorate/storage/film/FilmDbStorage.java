package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.*;

@Component
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Film> filmRowMapper = (rs, rowNum) -> {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getObject("release_date", LocalDate.class));
        film.setDuration(rs.getInt("duration"));

        Integer mpaId = rs.getObject("mpa_id", Integer.class);
        if (mpaId != null) {
            Mpa mpa = new Mpa();
            mpa.setId(mpaId);
            mpa.setName(rs.getString("mpa_name"));
            film.setMpa(mpa);
        }
        return film;
    };

    @Override
    public List<Film> getAllFilms() {
        String sql = """
            SELECT f.*, m.name AS mpa_name
            FROM films f
            LEFT JOIN mpa m ON f.mpa_id = m.id
            ORDER BY f.id
            """;
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper);
        films.forEach(this::loadGenres);
        return films;
    }

    @Override
    public Optional<Film> getFilm(Long id) {
        String sql = """
            SELECT f.*, m.name AS mpa_name
            FROM films f
            LEFT JOIN mpa m ON f.mpa_id = m.id
            WHERE f.id = ?
            """;
        try {
            Film film = jdbcTemplate.queryForObject(sql, filmRowMapper, id);
            loadGenres(film);
            return Optional.of(film);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Film addFilm(Film film) {
        String sql = """
            INSERT INTO films (name, description, release_date, duration, mpa_id)
            VALUES (?, ?, ?, ?, ?)
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setInt(4, film.getDuration());
            ps.setObject(5, film.getMpa() != null ? film.getMpa().getId() : null);
            return ps;
        }, keyHolder);
        film.setId(keyHolder.getKey().longValue());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveGenres(film.getId(), film.getGenres());
        }
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = """
            UPDATE films
            SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ?
            WHERE id = ?
            """;
        int updated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId()
        );
        if (updated == 0) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }
        jdbcTemplate.update("DELETE FROM film_genre WHERE film_id = ?", film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveGenres(film.getId(), film.getGenres());
        }
        return film;
    }

    @Override
    public void deleteFilm(Long id) {
        int deleted = jdbcTemplate.update("DELETE FROM films WHERE id = ?", id);
        if (deleted == 0) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbcTemplate.update("INSERT INTO likes (film_id, user_id) VALUES (?, ?)", filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        int deleted = jdbcTemplate.update("DELETE FROM likes WHERE film_id = ? AND user_id = ?", filmId, userId);
        if (deleted == 0) {
            throw new NotFoundException("Лайк не найден");
        }
    }

    @Override
    public List<Film> getPopular(int count) {
        String sql = """
        SELECT f.*, m.name AS mpa_name,
               COUNT(l.user_id) AS likes_count
        FROM films f
        LEFT JOIN mpa m ON f.mpa_id = m.id
        LEFT JOIN likes l ON f.id = l.film_id
        GROUP BY f.id, m.name
        ORDER BY likes_count DESC
        LIMIT ?
        """;
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, count);
        films.forEach(this::loadGenres);
        return films;
    }

    private void loadGenres(Film film) {
        String sql = """
            SELECT g.id, g.name
            FROM film_genre fg
            JOIN genres g ON fg.genre_id = g.id
            WHERE fg.film_id = ?
            ORDER BY g.id
            """;
        List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) ->
                new Genre(rs.getInt("id"), rs.getString("name")), film.getId());
        film.setGenres(genres);
    }

    private void saveGenres(Long filmId, List<Genre> genres) {
        String sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
        jdbcTemplate.batchUpdate(sql, genres, genres.size(), (ps, genre) -> {
            ps.setLong(1, filmId);
            ps.setInt(2, genre.getId());
        });
    }
}
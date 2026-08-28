package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenreService {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Genre> genreRowMapper = (rs, rowNum) ->
            new Genre(rs.getInt("id"), rs.getString("name"));

    public List<Genre> getAllGenres() {
        String sql = "SELECT * FROM genres ORDER BY id";
        return jdbcTemplate.query(sql, genreRowMapper);
    }

    public Genre getGenre(int id) {
        String sql = "SELECT * FROM genres WHERE id = ?";
        List<Genre> genres = jdbcTemplate.query(sql, genreRowMapper, id);
        if (genres.isEmpty()) {
            throw new NotFoundException("Жанр с id " + id + " не найден");
        }
        return genres.get(0);
    }
}
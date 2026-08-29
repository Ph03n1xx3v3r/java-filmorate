package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenreService {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Genre> genreRowMapper = (rs, rowNum) ->
            new Genre(rs.getInt("id"), rs.getString("name"));
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private void ensureData() {
        if (initialized.getAndSet(true)) return;
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM genres", Integer.class);
        if (count > 0) {
            log.info("Жанры уже есть, пропускаем инициализацию.");
            return;
        }
        log.info("Инициализация жанров...");
        String sql = """
            INSERT INTO genres (id, name) VALUES
            (1, 'Комедия'), (2, 'Драма'), (3, 'Мультфильм'),
            (4, 'Триллер'), (5, 'Мелодрама'), (6, 'Фантастика')
            """;
        jdbcTemplate.execute(sql);
        log.info("Жанры добавлены.");
    }

    public List<Genre> getAllGenres() {
        ensureData();
        String sql = "SELECT * FROM genres ORDER BY id";
        return jdbcTemplate.query(sql, genreRowMapper);
    }

    public Genre getGenre(int id) {
        ensureData();
        String sql = "SELECT * FROM genres WHERE id = ?";
        List<Genre> result = jdbcTemplate.query(sql, genreRowMapper, id);
        if (result.isEmpty()) {
            throw new NotFoundException("Жанр с id " + id + " не найден");
        }
        return result.get(0);
    }
}
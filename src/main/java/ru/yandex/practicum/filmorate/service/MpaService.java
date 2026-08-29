package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
@RequiredArgsConstructor
public class MpaService {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Mpa> mpaRowMapper = (rs, rowNum) ->
            new Mpa(rs.getInt("id"), rs.getString("name"));
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private void ensureData() {
        if (initialized.getAndSet(true)) return;
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM mpa", Integer.class);
        if (count > 0) {
            log.info("MPA уже есть, пропускаем инициализацию.");
            return;
        }
        log.info("Инициализация MPA...");
        String sql = """
            INSERT INTO mpa (id, name) VALUES
            (1, 'G'), (2, 'PG'), (3, 'PG-13'), (4, 'R'), (5, 'NC-17')
            """;
        jdbcTemplate.execute(sql);
        log.info("MPA добавлены.");
    }

    public List<Mpa> getAllMpa() {
        ensureData();
        String sql = "SELECT * FROM mpa ORDER BY id";
        return jdbcTemplate.query(sql, mpaRowMapper);
    }

    public Mpa getMpa(int id) {
        ensureData();
        String sql = "SELECT * FROM mpa WHERE id = ?";
        List<Mpa> result = jdbcTemplate.query(sql, mpaRowMapper, id);
        if (result.isEmpty()) {
            throw new NotFoundException("MPA с id " + id + " не найден");
        }
        return result.get(0);
    }
}
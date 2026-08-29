package ru.yandex.practicum.filmorate.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class DatabaseInitializer {
    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        log.info("Инициализация начальных данных...");
        initMpa();
        initGenres();
        log.info("Инициализация завершена.");
    }

    private void initMpa() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM mpa", Integer.class);
        if (count != null && count > 0) {
            log.info("MPA уже заполнены (найдено {} записей)", count);
            return;
        }
        String sql = """
            INSERT INTO mpa (id, name) VALUES
            (1, 'G'), (2, 'PG'), (3, 'PG-13'), (4, 'R'), (5, 'NC-17')
            """;
        jdbcTemplate.execute(sql);
        log.info("MPA добавлены.");
    }

    private void initGenres() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM genres", Integer.class);
        if (count != null && count > 0) {
            log.info("Жанры уже заполнены (найдено {} записей)", count);
            return;
        }
        String sql = """
            INSERT INTO genres (id, name) VALUES
            (1, 'Комедия'), (2, 'Драма'), (3, 'Мультфильм'),
            (4, 'Триллер'), (5, 'Мелодрама'), (6, 'Фантастика')
            """;
        jdbcTemplate.execute(sql);
        log.info("Жанры добавлены.");
    }
}
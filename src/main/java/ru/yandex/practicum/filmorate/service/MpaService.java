package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MpaService {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Mpa> mpaRowMapper = (rs, rowNum) ->
            new Mpa(rs.getInt("id"), rs.getString("name"));

    public List<Mpa> getAllMpa() {
        String sql = "SELECT * FROM mpa ORDER BY id";
        return jdbcTemplate.query(sql, mpaRowMapper);
    }

    public Mpa getMpa(int id) {
        String sql = "SELECT * FROM mpa WHERE id = ?";
        List<Mpa> mpaList = jdbcTemplate.query(sql, mpaRowMapper, id);
        if (mpaList.isEmpty()) {
            throw new NotFoundException("Рейтинг с id " + id + " не найден");
        }
        return mpaList.get(0);
    }
}
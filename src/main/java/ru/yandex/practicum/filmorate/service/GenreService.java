package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GenreService {
    private final Map<Integer, Genre> genres = Map.of(
            1, new Genre(1, "Комедия"),
            2, new Genre(2, "Драма"),
            3, new Genre(3, "Мультфильм"),
            4, new Genre(4, "Триллер"),
            5, new Genre(5, "Мелодрама"),
            6, new Genre(6, "Фантастика")
    );

    public List<Genre> getAllGenres() {
        return List.copyOf(genres.values());
    }

    public Genre getGenre(int id) {
        Genre genre = genres.get(id);
        if (genre == null) {
            throw new NotFoundException("Жанр с id " + id + " не найден");
        }
        return genre;
    }
}
package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/reviews")
@Slf4j
public class ReviewController {
    private final Map<Long, Map<String, Object>> reviews = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    @GetMapping
    public List<Map<String, Object>> getReviews(@RequestParam(required = false) Long filmId,
                                                @RequestParam(required = false) Integer count) {
        if (filmId != null) {
            return reviews.values().stream()
                    .filter(r -> r.get("filmId").equals(filmId))
                    .limit(count != null ? count : 10)
                    .toList();
        }
        return reviews.values().stream()
                .limit(count != null ? count : 10)
                .toList();
    }

    @GetMapping("/{id}")
    public Map<String, Object> getReview(@PathVariable Long id) {
        if (!reviews.containsKey(id)) {
            throw new ValidationException("Отзыв не найден");
        }
        return reviews.get(id);
    }

    @PostMapping
    public Map<String, Object> addReview(@RequestBody Map<String, Object> review) {
        if (review.get("content") == null || review.get("isPositive") == null ||
                review.get("userId") == null || review.get("filmId") == null) {
            throw new ValidationException("Не все поля заполнены");
        }
        Long id = idCounter.getAndIncrement();
        review.put("reviewId", id);
        review.put("useful", 0);
        reviews.put(id, review);
        return review;
    }

    @PutMapping
    public Map<String, Object> updateReview(@RequestBody Map<String, Object> review) {
        Long id = ((Number) review.get("reviewId")).longValue();
        if (!reviews.containsKey(id)) {
            throw new ValidationException("Отзыв не найден");
        }
        reviews.put(id, review);
        return review;
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Long id) {
        if (!reviews.containsKey(id)) {
            throw new ValidationException("Отзыв не найден");
        }
        reviews.remove(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        Map<String, Object> review = reviews.get(id);
        if (review == null) throw new ValidationException("Отзыв не найден");
        int useful = (int) review.getOrDefault("useful", 0);
        review.put("useful", useful + 1);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable Long id, @PathVariable Long userId) {
        Map<String, Object> review = reviews.get(id);
        if (review == null) throw new ValidationException("Отзыв не найден");
        int useful = (int) review.getOrDefault("useful", 0);
        review.put("useful", useful - 1);
    }
}
package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final Map<Long, User> users = new HashMap<>();
    private final Map<Long, Set<Long>> friends = new HashMap<>();
    private long nextId = 1L;

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public User getUser(Long id) {
        if (!users.containsKey(id)) {
            throw new ValidationException("Пользователь с id " + id + " не найден");
        }
        return users.get(id);
    }

    public User addUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        user.setId(nextId++);
        users.put(user.getId(), user);
        friends.put(user.getId(), new HashSet<>());
        log.info("Добавлен пользователь: {}", user);
        return user;
    }

    public User updateUser(User user) {
        if (user.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (!users.containsKey(user.getId())) {
            throw new ValidationException("Пользователь с таким id не существует");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        log.info("Обновлён пользователь: {}", user);
        return user;
    }

    public void deleteUser(Long id) {
        if (!users.containsKey(id)) {
            throw new ValidationException("Пользователь с id " + id + " не найден");
        }
        users.remove(id);
        friends.remove(id);
        for (Set<Long> friendSet : friends.values()) {
            friendSet.remove(id);
        }
        log.info("Удалён пользователь с id={}", id);
    }

    public void addFriend(Long userId, Long friendId) {
        if (!users.containsKey(userId) || !users.containsKey(friendId)) {
            throw new ValidationException("Пользователь не найден");
        }
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }
        friends.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
        friends.computeIfAbsent(friendId, k -> new HashSet<>()).add(userId);
        log.info("Пользователи {} и {} стали друзьями", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        if (!users.containsKey(userId) || !users.containsKey(friendId)) {
            throw new ValidationException("Пользователь не найден");
        }
        Set<Long> userFriends = friends.get(userId);
        if (userFriends == null || !userFriends.contains(friendId)) {
            throw new ValidationException("Друг не найден");
        }
        userFriends.remove(friendId);
        friends.get(friendId).remove(userId);
        log.info("Пользователи {} и {} перестали быть друзьями", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        if (!users.containsKey(userId)) {
            throw new ValidationException("Пользователь не найден");
        }
        Set<Long> friendIds = friends.getOrDefault(userId, Collections.emptySet());
        return friendIds.stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        if (!users.containsKey(userId) || !users.containsKey(otherId)) {
            throw new ValidationException("Пользователь не найден");
        }
        Set<Long> userFriends = friends.getOrDefault(userId, Collections.emptySet());
        Set<Long> otherFriends = friends.getOrDefault(otherId, Collections.emptySet());
        Set<Long> common = new HashSet<>(userFriends);
        common.retainAll(otherFriends);
        return common.stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<Object> getFeed(Long userId) {
        return List.of();
    }

    public List<Object> getRecommendations(Long userId) {
        return List.of();
    }
}
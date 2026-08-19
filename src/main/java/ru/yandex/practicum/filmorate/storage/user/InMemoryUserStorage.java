package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private final Map<Long, Set<Long>> friends = new HashMap<>();
    private long nextId = 1L;

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUser(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        return users.get(id);
    }

    @Override
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

    @Override
    public User updateUser(User user) {
        if (user.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("Пользователь с таким id не существует");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        log.info("Обновлён пользователь: {}", user);
        return user;
    }

    @Override
    public void deleteUser(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        users.remove(id);
        friends.remove(id);
        for (Set<Long> friendSet : friends.values()) {
            friendSet.remove(id);
        }
        log.info("Удалён пользователь с id={}", id);
    }

    // === Дополнительные методы для друзей ===

    public void addFriend(Long userId, Long friendId) {
        if (!users.containsKey(userId) || !users.containsKey(friendId)) {
            throw new NotFoundException("Пользователь не найден");
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
            throw new NotFoundException("Пользователь не найден");
        }
        Set<Long> userFriends = friends.get(userId);
        if (userFriends == null || !userFriends.contains(friendId)) {
            throw new NotFoundException("Друг не найден");
        }
        userFriends.remove(friendId);
        friends.get(friendId).remove(userId);
        log.info("Пользователи {} и {} перестали быть друзьями", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        Set<Long> friendIds = friends.getOrDefault(userId, Collections.emptySet());
        List<User> result = new ArrayList<>();
        for (Long id : friendIds) {
            User user = users.get(id);
            if (user != null) result.add(user);
        }
        return result;
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        if (!users.containsKey(userId) || !users.containsKey(otherId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        Set<Long> userFriends = friends.getOrDefault(userId, Collections.emptySet());
        Set<Long> otherFriends = friends.getOrDefault(otherId, Collections.emptySet());
        Set<Long> common = new HashSet<>(userFriends);
        common.retainAll(otherFriends);
        List<User> result = new ArrayList<>();
        for (Long id : common) {
            User user = users.get(id);
            if (user != null) result.add(user);
        }
        return result;
    }
}
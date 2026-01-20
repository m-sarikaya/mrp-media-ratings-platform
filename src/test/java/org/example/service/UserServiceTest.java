package org.example.service;

import org.example.domain.User;
import org.example.domain.UserProfileResponse;
import org.example.domain.UserProfileUpdate;
import org.example.persistence.RatingRepository;
import org.example.persistence.UserRepository;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

// Unit-Tests für UserService (ohne echte Datenbank).
public class UserServiceTest {

    @Test
    void registerFailsWhenUsernameMissing() {
        // Test: Registrierung ohne Username muss fehlschlagen.
        UserService service = new UserService(new FakeUserRepository());
        User user = new User();
        user.setPassword("pw");

        Exception ex = assertThrows(Exception.class, () -> service.register(user));
        assertEquals("Username ist erforderlich", ex.getMessage());
    }

    @Test
    void registerFailsWhenPasswordMissing() {
        // Test: Registrierung ohne Passwort muss fehlschlagen.
        UserService service = new UserService(new FakeUserRepository());
        User user = new User();
        user.setUsername("u1");

        Exception ex = assertThrows(Exception.class, () -> service.register(user));
        assertEquals("Password ist erforderlich", ex.getMessage());
    }

    @Test
    void registerFailsWhenUsernameExists() {
        // Test: Username darf nicht doppelt vergeben werden.
        FakeUserRepository repo = new FakeUserRepository();
        User existing = new User();
        existing.setId(1);
        existing.setUsername("u1");
        existing.setPassword("pw");
        repo.save(existing);

        UserService service = new UserService(repo);
        User user = new User();
        user.setUsername("u1");
        user.setPassword("pw2");

        Exception ex = assertThrows(Exception.class, () -> service.register(user));
        assertEquals("Username existiert bereits", ex.getMessage());
    }

    @Test
    void loginFailsWhenUserMissing() {
        // Test: Login mit unbekanntem User muss fehlschlagen.
        UserService service = new UserService(new FakeUserRepository());

        Exception ex = assertThrows(Exception.class, () -> service.login("u1", "pw"));
        assertEquals("User nicht gefunden", ex.getMessage());
    }

    @Test
    void loginFailsWithWrongPassword() {
        // Test: Falsches Passwort muss fehlschlagen.
        FakeUserRepository repo = new FakeUserRepository();
        User existing = new User();
        existing.setId(1);
        existing.setUsername("u1");
        existing.setPassword("pw");
        repo.save(existing);

        UserService service = new UserService(repo);
        Exception ex = assertThrows(Exception.class, () -> service.login("u1", "wrong"));
        assertEquals("Falsches Passwort", ex.getMessage());
    }

    @Test
    void loginSetsToken() throws Exception {
        // Test: Login erzeugt und speichert Token.
        FakeUserRepository repo = new FakeUserRepository();
        User existing = new User();
        existing.setId(1);
        existing.setUsername("u1");
        existing.setPassword("pw");
        repo.save(existing);

        UserService service = new UserService(repo);
        String token = service.login("u1", "pw");

        assertEquals("u1-mrpToken", token);
        assertEquals("u1-mrpToken", repo.findByUsername("u1").getToken());
    }

    @Test
    void getProfileReturnsStats() throws Exception {
        // Test: Profil liefert Statistiken aus RatingRepository.
        FakeUserRepository userRepo = new FakeUserRepository();
        User user = new User();
        user.setId(7);
        user.setUsername("u7");
        userRepo.save(user);

        FakeRatingRepository ratingRepo = new FakeRatingRepository();
        ratingRepo.countByUserValue = 3;
        ratingRepo.avgByUserValue = 4.5;

        UserService service = new UserService(userRepo, ratingRepo);
        UserProfileResponse profile = service.getProfile(7);

        assertEquals(7, profile.getId());
        assertEquals("u7", profile.getUsername());
        assertEquals(3, profile.getTotalRatings());
        assertEquals(4.5, profile.getAverageScore());
    }

    @Test
    void updateProfileStoresFields() throws Exception {
        // Test: Profil-Update speichert Email und FavoriteGenre.
        FakeUserRepository userRepo = new FakeUserRepository();
        User user = new User();
        user.setId(5);
        user.setUsername("u5");
        userRepo.save(user);

        UserService service = new UserService(userRepo);
        UserProfileUpdate update = new UserProfileUpdate();
        update.setEmail("u5@example.com");
        update.setFavoriteGenre("SCI-FI");
        service.updateProfile(5, update);

        User updated = userRepo.findById(5);
        assertEquals("u5@example.com", updated.getEmail());
        assertEquals("SCI-FI", updated.getFavoriteGenre());
    }

    // Fake-Repository für User (In-Memory).
    static class FakeUserRepository extends UserRepository {
        private final Map<Integer, User> byId = new HashMap<>();
        private int nextId = 1;

        FakeUserRepository() {
            super((Connection) null);
        }

        @Override
        public User save(User user) {
            if (user.getId() == 0) {
                user.setId(nextId++);
            }
            byId.put(user.getId(), user);
            return user;
        }

        @Override
        public User findByUsername(String username) {
            return byId.values().stream()
                    .filter(u -> username.equals(u.getUsername()))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public void updateToken(User user) {
            User stored = byId.get(user.getId());
            if (stored != null) {
                stored.setToken(user.getToken());
            }
        }

        @Override
        public User findByToken(String token) {
            return byId.values().stream()
                    .filter(u -> token.equals(u.getToken()))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public User findById(int userId) {
            return byId.get(userId);
        }

        @Override
        public void updateProfile(int userId, String email, String favoriteGenre) {
            User stored = byId.get(userId);
            if (stored != null) {
                stored.setEmail(email);
                stored.setFavoriteGenre(favoriteGenre);
            }
        }
    }

    // Fake-Repository für Rating-Stats (ohne DB).
    static class FakeRatingRepository extends RatingRepository {
        int countByUserValue = 0;
        double avgByUserValue = 0.0;

        FakeRatingRepository() {
            super((Connection) null);
        }

        @Override
        public int countByUser(int userId) throws SQLException {
            return countByUserValue;
        }

        @Override
        public double averageByUser(int userId) throws SQLException {
            return avgByUserValue;
        }
    }
}

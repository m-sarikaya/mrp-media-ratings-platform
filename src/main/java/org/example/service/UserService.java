package org.example.service;

import org.example.domain.User;
import org.example.persistence.UserRepository;

public class UserService {

    private final UserRepository userRepository;

    // Constructor: Bekommt das Repository
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ========================================
    // REGISTER - Neuen User registrieren
    // ========================================
    public User register(User user) throws Exception {

        // 1. Prüfen ob Username und Password vorhanden sind
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new Exception("Username ist erforderlich");
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new Exception("Password ist erforderlich");
        }

        // 2. Prüfen ob Username schon existiert
        User existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser != null) {
            throw new Exception("Username existiert bereits");
        }

        // 3. User in Datenbank speichern
        User savedUser = userRepository.save(user);

        if (savedUser == null) {
            throw new Exception("Fehler beim Speichern");
        }

        return savedUser;
    }

    // ========================================
    // LOGIN - User einloggen
    // ========================================
    public String login(String username, String password) throws Exception {

        // 1. Prüfen ob Username und Password vorhanden sind
        if (username == null || username.isEmpty()) {
            throw new Exception("Username ist erforderlich");
        }

        if (password == null || password.isEmpty()) {
            throw new Exception("Password ist erforderlich");
        }

        // 2. User in Datenbank suchen
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new Exception("User nicht gefunden");
        }

        // 3. Passwort prüfen
        if (!user.getPassword().equals(password)) {
            throw new Exception("Falsches Passwort");
        }

        // 4. Token generieren
        String token = username + "-mrpToken";
        user.setToken(token);

        // 5. Token in Datenbank speichern
        userRepository.updateToken(user);

        return token;
    }

    // ========================================
    // FIND BY TOKEN - User anhand Token finden
    // ========================================
    public User findByToken(String token) {
        return userRepository.findByToken(token);
    }
}

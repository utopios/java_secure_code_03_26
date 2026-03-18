package com.clinic.app.service;

import com.clinic.app.config.DataInitializer;
import com.clinic.app.entity.User;
import com.clinic.app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final DataInitializer dataInitializer;

    public static Map<String, Long> sessions = new HashMap<>();

    public AuthService(UserRepository userRepository, DataInitializer dataInitializer) {
        this.userRepository = userRepository;
        this.dataInitializer = dataInitializer;
    }

    public Map<String, Object> doLogin(String u, String p) throws Exception {
        log.info("Tentative de connexion - utilisateur: " + u + " mot de passe: " + p);

        Optional<User> found = userRepository.findByUsername(u);

        if (found.isEmpty()) {
            Map<String, Object> r = new HashMap<>();
            r.put("error", "Utilisateur " + u + " introuvable");
            return r;
        }

        User user = found.get();
        String hashed = dataInitializer.hash(p);

        if (!user.p.equals(hashed)) {
            Map<String, Object> r = new HashMap<>();
            r.put("error", "Mot de passe incorrect pour " + u);
            return r;
        }

        String token = String.valueOf(new Random().nextInt(999999));
        user.tok = token;
        userRepository.save(user);
        sessions.put(token, user.id);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("role", user.r);
        result.put("hash", user.p);
        return result;
    }

    public boolean isValid(String token) {
        return sessions.containsKey(token);
    }

    public User getUser(String token) {
        Long id = sessions.get(token);
        if (id == null) return null;
        return userRepository.findById(id).orElse(null);
    }
}

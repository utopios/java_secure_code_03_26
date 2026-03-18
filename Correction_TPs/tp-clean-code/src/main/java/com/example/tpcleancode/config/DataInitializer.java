package com.example.tpcleancode.config;

import com.example.tpcleancode.entity.Patient;
import com.example.tpcleancode.entity.Role;
import com.example.tpcleancode.entity.User;
import com.example.tpcleancode.repository.PatientRepository;
import com.example.tpcleancode.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initialisation des donnees de demonstration.
 *
 * CORRECTION Securite :
 * - Utilise BCrypt (via PasswordEncoder) au lieu de MD5 sans sel
 *
 * AVANT (vulnerable) :
 *   MessageDigest md = MessageDigest.getInstance("MD5");
 *   byte[] hash = md.digest("admin123".getBytes());
 *   user.setPasswordHash(new String(Hex.encode(hash)));
 *   // MD5 sans sel = vulnerable aux rainbow tables et dictionnaires
 *
 * APRES (securise) :
 *   user.setPasswordHash(passwordEncoder.encode("admin123"));
 *   // BCrypt avec sel aleatoire integre et facteur de cout 12
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           PatientRepository patientRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            logger.info("Donnees deja initialisees, skip");
            return;
        }

        // Creer les utilisateurs avec BCrypt
        User admin = new User("Alice", "Martin", "admin@hospital.com",
                passwordEncoder.encode("Admin@2024!Secure"), Role.ADMIN);

        User doctor = new User("Bob", "Dupont", "doctor@hospital.com",
                passwordEncoder.encode("Doctor@2024!Secure"), Role.DOCTOR);

        User user = new User("Charlie", "Durand", "user@hospital.com",
                passwordEncoder.encode("User@2024!Secure"), Role.USER);

        userRepository.save(admin);
        userRepository.save(doctor);
        userRepository.save(user);

        // Creer des patients de demonstration
        Patient patient1 = new Patient("Jean", "Lefebvre", "1850575123456");
        patient1.setDiagnosis("Hypertension arterielle");
        patient1.setDoctorName("Bob Dupont");

        Patient patient2 = new Patient("Marie", "Bernard", "2890180654321");
        patient2.setDiagnosis("Diabete de type 2");
        patient2.setDoctorName("Bob Dupont");

        patientRepository.save(patient1);
        patientRepository.save(patient2);

        logger.info("Donnees de demonstration initialisees : {} utilisateurs, {} patients",
                userRepository.count(), patientRepository.count());
    }
}

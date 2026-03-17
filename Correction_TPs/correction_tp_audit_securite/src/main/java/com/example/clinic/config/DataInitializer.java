package com.example.clinic.config;

import com.example.clinic.entity.Doctor;
import com.example.clinic.entity.Patient;
import com.example.clinic.entity.Prescription;
import com.example.clinic.repository.DoctorRepository;
import com.example.clinic.repository.PatientRepository;
import com.example.clinic.repository.PrescriptionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PrescriptionRepository prescriptionRepository;
    // CORRIGE #9 : Injection du PasswordEncoder (BCrypt) au lieu de SHA-256 maison
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(DoctorRepository doctorRepository, PatientRepository patientRepository,
                           PrescriptionRepository prescriptionRepository, PasswordEncoder passwordEncoder) {
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // CORRIGE #9 : BCrypt au lieu de SHA-256 sans sel
        // CORRIGE #10 : Mot de passe admin robuste (pas "admin")
        Doctor dr1 = new Doctor("dr.martin", passwordEncoder.encode("martin2024"), "Dr. Sophie Martin", "Cardiologie", "DOCTOR");
        Doctor dr2 = new Doctor("dr.dupont", passwordEncoder.encode("dupont2024"), "Dr. Pierre Dupont", "Neurologie", "DOCTOR");
        Doctor admin = new Doctor("admin", passwordEncoder.encode("Adm!n$ecure2024#"), "Administrateur", "Administration", "ADMIN");
        doctorRepository.save(dr1);
        doctorRepository.save(dr2);
        doctorRepository.save(admin);

        Patient p1 = new Patient("Jean", "Lemaire", "jean.lemaire@email.com", "0612345678", "1 85 06 75 108 234 56", "Antecedents cardiaques. Traitement beta-bloquant en cours.");
        Patient p2 = new Patient("Marie", "Durand", "marie.durand@email.com", "0698765432", "2 90 03 13 055 123 78", "Allergique a la penicilline. Suivi neurologique.");
        Patient p3 = new Patient("Luc", "Bernard", "luc.bernard@email.com", "0654321098", "1 78 11 69 012 456 89", "Diabete type 2. Regime strict.");
        patientRepository.save(p1);
        patientRepository.save(p2);
        patientRepository.save(p3);

        Prescription rx1 = new Prescription();
        rx1.setPatientId(1L);
        rx1.setDoctorId(1L);
        rx1.setMedication("Bisoprolol 5mg");
        rx1.setDosage("1 comprime par jour");
        rx1.setPrescriptionDate(LocalDate.now());
        rx1.setNotes("Renouvellement trimestriel");
        prescriptionRepository.save(rx1);

        Prescription rx2 = new Prescription();
        rx2.setPatientId(2L);
        rx2.setDoctorId(2L);
        rx2.setMedication("Paracetamol 1000mg");
        rx2.setDosage("1 a 3 fois par jour si douleur");
        rx2.setPrescriptionDate(LocalDate.now());
        rx2.setNotes("Ne pas depasser 3g/jour");
        prescriptionRepository.save(rx2);
    }

    // SUPPRIME : methode hashPassword() avec SHA-256
    // BCrypt est gere par le bean PasswordEncoder injecte
}

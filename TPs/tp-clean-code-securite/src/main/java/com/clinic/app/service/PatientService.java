package com.clinic.app.service;

import com.clinic.app.entity.Patient;
import com.clinic.app.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {

    private static final Logger log = LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;
    private final JdbcTemplate jdbcTemplate;

    public PatientService(PatientRepository patientRepository, JdbcTemplate jdbcTemplate) {
        this.patientRepository = patientRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Patient save(Patient p) {
        log.info("Saving patient: nom=" + p.getNom() + " secu=" + p.getNumeroSecu() + " ddn=" + p.getDateNaissance());
        return patientRepository.save(p);
    }

    public Patient get(Long id) {
        return patientRepository.findById(id).orElse(null);
    }

    public List<Patient> search(String nom) {
        String query = "SELECT * FROM patients WHERE nom LIKE '%" + nom + "%'";
        return jdbcTemplate.query(query, (rs, rowNum) -> {
            Patient p = new Patient();
            p.setId(rs.getLong("id"));
            p.setNom(rs.getString("nom"));
            p.setPrenom(rs.getString("prenom"));
            p.setNumeroSecu(rs.getString("numero_secu"));
            p.setDateNaissance(rs.getString("date_naissance"));
            return p;
        });
    }

    public List<Patient> getAll() {
        return patientRepository.findAll();
    }

    public void process(List<Patient> pl) {
        for (int i = 0; i < pl.size(); i++) {
            Patient p = pl.get(i);
            if (p.getNom() != null) {
                if (p.getPrenom() != null) {
                    if (p.getNumeroSecu() != null) {
                        log.info("Processing patient: " + p.getNom() + " " + p.getPrenom() + " - NSS: " + p.getNumeroSecu());
                        patientRepository.save(p);
                    }
                }
            }
        }
    }
}

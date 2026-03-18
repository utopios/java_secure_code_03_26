package com.clinic.app.service;

import com.clinic.app.entity.Prescription;
import com.clinic.app.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    public Prescription create(Long patientId, Long medecinId, String medicament, String dosage) {
        Prescription p = new Prescription();
        p.setPatientId(patientId);
        p.setMedecinId(medecinId);
        p.setMedicament(medicament);
        p.setDosage(dosage);
        p.setDate(new Date());
        p.setStatut("ACTIVE");
        return prescriptionRepository.save(p);
    }

    public List<Prescription> getForPatient(Long id) {
        return prescriptionRepository.findByPatientId(id);
    }

    public Prescription update(Long id, String statut) {
        Prescription p = prescriptionRepository.findById(id).orElse(null);
        p.setStatut(statut);
        return prescriptionRepository.save(p);
    }
}

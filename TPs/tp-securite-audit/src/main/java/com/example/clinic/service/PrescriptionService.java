package com.example.clinic.service;

import com.example.clinic.entity.Prescription;
import com.example.clinic.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    public Prescription create(Prescription prescription) {
        return prescriptionRepository.save(prescription);
    }

    public Optional<Prescription> getById(Long id) {
        return prescriptionRepository.findById(id);
    }

    public List<Prescription> getAll() {
        return prescriptionRepository.findAll();
    }

    public List<Prescription> getByDoctor(Long doctorId) {
        return prescriptionRepository.findByDoctorId(doctorId);
    }

    public List<Prescription> getByPatient(Long patientId) {
        return prescriptionRepository.findByPatientId(patientId);
    }

    public void delete(Long id) {
        prescriptionRepository.deleteById(id);
    }
}

package tn.esprit.autoloc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Equipement;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.IEquipementRepository;
import tn.esprit.autoloc.repository.IVehiculeRepository;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehiculeServiceImpl implements IVehiculeService {

    private final IVehiculeRepository vehiculeRepository;
    private final IEquipementRepository equipementRepository;

    @Override
    @Transactional
    public Vehicule create(Vehicule vehicule) {
        if (vehicule.getIdVehicule() != null) {
            throw new IllegalArgumentException("Un nouveau vehicule ne doit pas avoir d'identifiant");
        }
        validate(vehicule);
        resolveEquipements(vehicule);
        return vehiculeRepository.save(vehicule);
    }

    @Override
    public Vehicule findById(Long id) {
        return vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule", id));
    }

    @Override
    public List<Vehicule> findAll() {
        return vehiculeRepository.findAll();
    }

    @Override
    @Transactional
    public Vehicule update(Long id, Vehicule vehicule) {
        validate(vehicule);
        resolveEquipements(vehicule);
        Vehicule existant = findById(id);
        existant.setImmatriuclation(vehicule.getImmatriuclation());
        existant.setMarque(vehicule.getMarque());
        existant.setModele(vehicule.getModele());
        existant.setCategorie(vehicule.getCategorie());
        existant.setStatut(vehicule.getStatut());
        existant.setTarifJournalier(vehicule.getTarifJournalier());
        existant.setAgence(vehicule.getAgence());
        existant.setEquipements(vehicule.getEquipements());
        return vehiculeRepository.save(existant);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!vehiculeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vehicule", id);
        }
        vehiculeRepository.deleteById(id);
    }

    private void validate(Vehicule vehicule) {
        if (vehicule.getTarifJournalier() == null || vehicule.getTarifJournalier().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le tarif journalier du vehicule ne doit pas etre negatif");
        }
    }

    // Les equipements referencees par id doivent pointer vers des entites deja gerees par JPA :
    // sans cette resolution, un Equipement partiellement rempli (juste l'id) ecraserait la ligne
    // existante en base lors du merge (ex. libelle mis a null).
    private void resolveEquipements(Vehicule vehicule) {
        if (vehicule.getEquipements() == null || vehicule.getEquipements().isEmpty()) {
            return;
        }
        List<Long> ids = vehicule.getEquipements().stream()
                .map(Equipement::getIdEquipement)
                .toList();
        vehicule.setEquipements(new HashSet<>(equipementRepository.findAllById(ids)));
    }
}

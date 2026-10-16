package tn.esprit.autoloc.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Equipement;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.repository.IEquipementRepository;
import tn.esprit.autoloc.repository.IVehiculeRepository;

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
    public Vehicule save(Vehicule vehicule) {
        resolveEquipements(vehicule);
        return vehiculeRepository.save(vehicule);
    }

    // Les equipements referencees par id doivent pointer vers des entites deja geres par JPA :
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

    @Override
    public Vehicule findById(Long id) {
        return vehiculeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicule introuvable, id=" + id));
    }

    @Override
    public List<Vehicule> findAll() {
        return vehiculeRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        vehiculeRepository.deleteById(id);
    }
}

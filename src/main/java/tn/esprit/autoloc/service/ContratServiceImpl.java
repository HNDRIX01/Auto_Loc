package tn.esprit.autoloc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.domain.Paiement;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.IContratRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContratServiceImpl implements IContratService {

    private final IContratRepository contratRepository;

    @Override
    @Transactional
    public Contrat create(Contrat contrat) {
        if (contrat.getIdContrat() != null) {
            throw new IllegalArgumentException("Un nouveau contrat ne doit pas avoir d'identifiant");
        }
        validate(contrat);
        return contratRepository.save(contrat);
    }

    @Override
    public Contrat findById(Long id) {
        return contratRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat", id));
    }

    @Override
    public List<Contrat> findAll() {
        return contratRepository.findAll();
    }

    @Override
    @Transactional
    public Contrat update(Long id, Contrat contrat) {
        validate(contrat);
        Contrat existant = findById(id);
        existant.setDateSignature(contrat.getDateSignature());
        existant.setMontantTotal(contrat.getMontantTotal());
        existant.setValide(contrat.isValide());
        // La collection paiements n'est jamais recopiee ici : avec orphanRemoval=true,
        // ecraser cette collection supprimerait les paiements existants.
        return contratRepository.save(existant);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!contratRepository.existsById(id)) {
            throw new ResourceNotFoundException("Contrat", id);
        }
        // La suppression passe par le contexte de persistance (CrudRepository.deleteById) :
        // cascade=ALL + orphanRemoval=true sur Contrat.paiements supprime les paiements associes.
        contratRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Contrat ajouterPaiement(Long idContrat, Paiement paiement) {
        Contrat contrat = findById(idContrat);
        paiement.setContrat(contrat);
        contrat.getPaiements().add(paiement);
        return contratRepository.save(contrat);
    }

    @Override
    @Transactional
    public Contrat retirerPaiement(Long idContrat, Long idPaiement) {
        Contrat contrat = findById(idContrat);
        contrat.getPaiements().removeIf(paiement -> paiement.getIdPaiement().equals(idPaiement));
        return contratRepository.save(contrat);
    }

    private void validate(Contrat contrat) {
        if (contrat.getMontantTotal() == null || contrat.getMontantTotal().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le montant total du contrat ne doit pas etre negatif");
        }
    }
}

package tn.esprit.autoloc.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.domain.Paiement;
import tn.esprit.autoloc.repository.IContratRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContratServiceImpl implements IContratService {

    private final IContratRepository contratRepository;

    @Override
    @Transactional
    public Contrat save(Contrat contrat) {
        return contratRepository.save(contrat);
    }

    @Override
    public Contrat findById(Long id) {
        return contratRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Contrat introuvable, id=" + id));
    }

    @Override
    public List<Contrat> findAll() {
        return contratRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
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
}

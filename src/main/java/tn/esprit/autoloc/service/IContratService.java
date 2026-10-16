package tn.esprit.autoloc.service;

import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.domain.Paiement;

import java.util.List;

public interface IContratService {
    Contrat save(Contrat contrat);
    Contrat findById(Long id);
    List<Contrat> findAll();
    void deleteById(Long id);

    // Le Paiement est une composition de Contrat : il se crée et se retire via le Contrat.
    Contrat ajouterPaiement(Long idContrat, Paiement paiement);
    Contrat retirerPaiement(Long idContrat, Long idPaiement);
}

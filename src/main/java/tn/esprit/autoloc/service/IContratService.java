package tn.esprit.autoloc.service;

import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.domain.Paiement;

import java.util.List;

public interface IContratService {
    Contrat create(Contrat contrat);
    Contrat findById(Long id);
    List<Contrat> findAll();
    Contrat update(Long id, Contrat contrat);
    void deleteById(Long id);

    // Le Paiement est une composition de Contrat : il se cree et se retire via le Contrat.
    Contrat ajouterPaiement(Long idContrat, Paiement paiement);
    Contrat retirerPaiement(Long idContrat, Long idPaiement);
}

package tn.esprit.autoloc.service;

import tn.esprit.autoloc.domain.Paiement;

import java.util.List;

// Lecture seule : un Paiement se crée et se retire via IContratService (composition Contrat -> Paiement).
public interface IPaiementService {
    Paiement findById(Long id);
    List<Paiement> findAll();
}

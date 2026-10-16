package tn.esprit.autoloc.service;

import tn.esprit.autoloc.domain.Vehicule;

import java.util.List;

public interface IVehiculeService {
    Vehicule save(Vehicule vehicule);
    Vehicule findById(Long id);
    List<Vehicule> findAll();
    void deleteById(Long id);
}

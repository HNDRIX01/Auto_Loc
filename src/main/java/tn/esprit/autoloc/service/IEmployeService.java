package tn.esprit.autoloc.service;

import tn.esprit.autoloc.domain.Employe;

import java.util.List;

public interface IEmployeService {
    Employe save(Employe employe);
    Employe findById(Long id);
    List<Employe> findAll();
    void deleteById(Long id);
}

package tn.esprit.autoloc.service;

import tn.esprit.autoloc.domain.Maintenance;

import java.util.List;

public interface IMaintenanceService {
    Maintenance save(Maintenance maintenance);
    Maintenance findById(Long id);
    List<Maintenance> findAll();
    void deleteById(Long id);
}

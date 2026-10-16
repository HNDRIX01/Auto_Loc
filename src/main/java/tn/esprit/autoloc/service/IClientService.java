package tn.esprit.autoloc.service;

import tn.esprit.autoloc.domain.Client;

import java.util.List;

public interface IClientService {
    Client save(Client client);
    Client findById(Long id);
    List<Client> findAll();
    void deleteById(Long id);
}

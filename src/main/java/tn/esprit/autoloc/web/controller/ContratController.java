package tn.esprit.autoloc.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.domain.Paiement;
import tn.esprit.autoloc.service.IContratService;

import java.util.List;

@RestController
@RequestMapping("/api/contrats")
@RequiredArgsConstructor
public class ContratController {

    private final IContratService contratService;

    @GetMapping
    public List<Contrat> findAll() {
        return contratService.findAll();
    }

    @GetMapping("/{id}")
    public Contrat findById(@PathVariable Long id) {
        return contratService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Contrat create(@RequestBody Contrat contrat) {
        contrat.setIdContrat(null);
        return contratService.save(contrat);
    }

    @PutMapping("/{id}")
    public Contrat update(@PathVariable Long id, @RequestBody Contrat contrat) {
        contratService.findById(id);
        contrat.setIdContrat(id);
        return contratService.save(contrat);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        contratService.findById(id);
        contratService.deleteById(id);
    }

    // Composition Contrat -> Paiement : creation et retrait exclusivement via le Contrat.
    @PostMapping("/{id}/paiements")
    @ResponseStatus(HttpStatus.CREATED)
    public Contrat ajouterPaiement(@PathVariable Long id, @RequestBody Paiement paiement) {
        return contratService.ajouterPaiement(id, paiement);
    }

    @DeleteMapping("/{id}/paiements/{idPaiement}")
    public Contrat retirerPaiement(@PathVariable Long id, @PathVariable Long idPaiement) {
        return contratService.retirerPaiement(id, idPaiement);
    }
}

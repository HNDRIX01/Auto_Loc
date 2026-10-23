package tn.esprit.autoloc.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.domain.Paiement;
import tn.esprit.autoloc.service.IContratService;

import java.util.List;

@Tag(name = "Contrats", description = "Gestion des contrats de location")
@RestController
@RequestMapping("/contrats")
@RequiredArgsConstructor
public class ContratController {

    private static final String ID_PARAM = "Identifiant du contrat";

    private final IContratService contratService;

    @Operation(summary = "Lister les contrats")
    @ApiResponse(responseCode = "200", description = "Liste des contrats")
    @GetMapping
    public List<Contrat> findAll() {
        return contratService.findAll();
    }

    @Operation(summary = "Consulter un contrat")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contrat trouvé"),
            @ApiResponse(responseCode = "404", description = "Contrat introuvable")
    })
    @GetMapping("/{id}")
    public Contrat findById(@Parameter(description = ID_PARAM) @PathVariable Long id) {
        return contratService.findById(id);
    }

    @Operation(summary = "Créer un contrat")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Contrat créé"),
            @ApiResponse(responseCode = "400", description = "Identifiant fourni ou montant invalide")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Contrat create(@RequestBody Contrat contrat) {
        return contratService.create(contrat);
    }

    @Operation(summary = "Modifier un contrat")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contrat modifié"),
            @ApiResponse(responseCode = "404", description = "Contrat introuvable"),
            @ApiResponse(responseCode = "400", description = "Montant invalide")
    })
    @PutMapping("/{id}")
    public Contrat update(@Parameter(description = ID_PARAM) @PathVariable Long id,
                           @RequestBody Contrat contrat) {
        return contratService.update(id, contrat);
    }

    @Operation(summary = "Supprimer un contrat (supprime ses paiements par cascade)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Contrat supprimé"),
            @ApiResponse(responseCode = "404", description = "Contrat introuvable")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Parameter(description = ID_PARAM) @PathVariable Long id) {
        contratService.deleteById(id);
    }

    // Composition Contrat -> Paiement : creation et retrait exclusivement via le Contrat.
    @Operation(summary = "Ajouter un paiement a un contrat existant")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Paiement ajouté"),
            @ApiResponse(responseCode = "404", description = "Contrat introuvable")
    })
    @PostMapping("/{id}/paiements")
    @ResponseStatus(HttpStatus.CREATED)
    public Contrat ajouterPaiement(@Parameter(description = ID_PARAM) @PathVariable Long id,
                                    @RequestBody Paiement paiement) {
        return contratService.ajouterPaiement(id, paiement);
    }

    @Operation(summary = "Retirer un paiement d'un contrat")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paiement retiré"),
            @ApiResponse(responseCode = "404", description = "Contrat introuvable")
    })
    @DeleteMapping("/{id}/paiements/{idPaiement}")
    public Contrat retirerPaiement(@Parameter(description = ID_PARAM) @PathVariable Long id,
                                    @Parameter(description = "Identifiant du paiement") @PathVariable Long idPaiement) {
        return contratService.retirerPaiement(id, idPaiement);
    }
}

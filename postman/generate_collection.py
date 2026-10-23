#!/usr/bin/env python3
"""Generates the AutoLoc Postman collection (v2.1) and local environment.

This script is a convenience generator, not part of the application build.
Run it with `python3 generate_collection.py` to regenerate the two JSON
files after changing the entities below; it is not wired into Maven.
"""
import json
import uuid

BASE_URL = "{{baseUrl}}"


def req(name, method, path, body=None, tests=None):
    request = {
        "method": method,
        "header": [{"key": "Content-Type", "value": "application/json"}] if body is not None else [],
        "url": {
            "raw": f"{BASE_URL}/api/{path}",
            "host": [BASE_URL],
            "path": ["api"] + path.split("/"),
        },
    }
    if body is not None:
        request["body"] = {"mode": "raw", "raw": json.dumps(body, indent=2),
                            "options": {"raw": {"language": "json"}}}
    item = {"name": name, "request": request, "response": []}
    if tests:
        item["event"] = [{
            "listen": "test",
            "script": {"type": "text/javascript", "exec": tests},
        }]
    return item


def crud_folder(name, plural, nominal_body, bad_body, id_var):
    return {
        "name": name,
        "item": [
            req(f"Lister les {plural}", "GET", plural,
                tests=['pm.test("Statut 200", () => pm.response.to.have.status(200));']),
            req(f"Consulter {name} inexistant (404)", "GET", f"{plural}/999999",
                tests=['pm.test("Statut 404", () => pm.response.to.have.status(404));',
                       'pm.test("Corps structure", () => {',
                       '  const b = pm.response.json();',
                       '  pm.expect(b).to.have.property("statut", 404);',
                       '  pm.expect(b).to.have.property("message");',
                       '  pm.expect(b).to.have.property("horodatage");',
                       '});']),
            req(f"Créer {name}", "POST", plural, body=nominal_body,
                tests=['pm.test("Statut 201", () => pm.response.to.have.status(201));',
                       f'pm.environment.set("{id_var}", pm.response.json().{id_var});']),
            req(f"Créer {name} invalide (400)", "POST", plural, body=bad_body,
                tests=['pm.test("Statut 400", () => pm.response.to.have.status(400));']),
            req(f"Modifier {name}", "PUT", f"{plural}/{{{{{id_var}}}}}", body=nominal_body,
                tests=['pm.test("Statut 200", () => pm.response.to.have.status(200));']),
            req(f"Modifier {name} inexistant (404)", "PUT", f"{plural}/999999", body=nominal_body,
                tests=['pm.test("Statut 404", () => pm.response.to.have.status(404));']),
            req(f"Supprimer {name}", "DELETE", f"{plural}/{{{{{id_var}}}}}",
                tests=['pm.test("Statut 204", () => pm.response.to.have.status(204));']),
            req(f"Supprimer {name} inexistant (404)", "DELETE", f"{plural}/999999",
                tests=['pm.test("Statut 404", () => pm.response.to.have.status(404));']),
        ],
    }


collection = {
    "info": {
        "_postman_id": str(uuid.uuid4()),
        "name": "AutoLoc",
        "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
    },
    "item": [
        crud_folder("une agence", "agences",
                    {"nom": "Agence Tunis", "ville": "Tunis", "adresse": "Rue 1", "telephone": "71000000"},
                    {"nom": "", "ville": "Tunis", "adresse": "Rue 1", "telephone": "71000000"},
                    "idAgence"),
        crud_folder("un employe", "employes",
                    {"nom": "Ben Ali", "prenom": "Sami", "role": "AGENT"},
                    {"nom": "", "prenom": "Sami", "role": "AGENT"},
                    "idEmploye"),
        crud_folder("un vehicule", "vehicules",
                    {"immatriuclation": "TUN1234", "marque": "Clio", "modele": "5", "categorie": "CITADINE",
                     "statut": "DISPONIBLE", "tarifJournalier": 80.00},
                    {"immatriuclation": "TUN0000", "marque": "Clio", "modele": "5", "categorie": "CITADINE",
                     "statut": "DISPONIBLE", "tarifJournalier": -10},
                    "idVehicule"),
        crud_folder("un equipement", "equipements",
                    {"libelle": "GPS"},
                    {"libelle": ""},
                    "idEquipement"),
        crud_folder("un client", "clients",
                    {"nom": "Trabelsi", "prenom": "Ahmed", "email": "a@t.tn", "telephone": "20000000",
                     "numPermis": "P123", "dateInscription": "2026-10-30"},
                    {"nom": "", "prenom": "Ahmed", "email": "a@t.tn", "telephone": "20000000",
                     "numPermis": "P123", "dateInscription": "2026-10-30"},
                    "idClient"),
        crud_folder("une reservation", "reservations",
                    {"dateDebut": "2026-11-01", "dateFin": "2026-11-05", "statut": "EN_ATTENTE"},
                    {"dateDebut": "2026-11-05", "dateFin": "2026-11-01", "statut": "EN_ATTENTE"},
                    "idReservation"),
        crud_folder("une maintenance", "maintenances",
                    {"dateDebut": "2026-11-01", "dateFin": "2026-11-03", "description": "Vidange"},
                    {"dateDebut": "2026-11-03", "dateFin": "2026-11-01", "description": "Vidange"},
                    "idMaintenance"),
        {
            "name": "Contrats",
            "item": [
                req("Lister les contrats", "GET", "contrats",
                    tests=['pm.test("Statut 200", () => pm.response.to.have.status(200));']),
                req("Consulter un contrat inexistant (404)", "GET", "contrats/999999",
                    tests=['pm.test("Statut 404", () => pm.response.to.have.status(404));']),
                req("Creer un contrat", "POST", "contrats",
                    body={"dateSignature": "2026-11-02", "montantTotal": 480.00, "valide": True},
                    tests=['pm.test("Statut 201", () => pm.response.to.have.status(201));',
                           'pm.environment.set("contratId", pm.response.json().idContrat);']),
                req("Creer un contrat avec un identifiant (400)", "POST", "contrats",
                    body={"idContrat": 5, "dateSignature": "2026-11-02", "montantTotal": 480.00, "valide": True},
                    tests=['pm.test("Statut 400", () => pm.response.to.have.status(400));']),
                req("Creer un contrat avec montant negatif (400)", "POST", "contrats",
                    body={"dateSignature": "2026-11-02", "montantTotal": -10, "valide": True},
                    tests=['pm.test("Statut 400", () => pm.response.to.have.status(400));']),
                req("Creer un contrat avec ses paiements (cascade PERSIST)", "POST", "contrats",
                    body={"dateSignature": "2026-11-02", "montantTotal": 480.00, "valide": True,
                          "paiements": [
                              {"montant": 150.00, "datePaiement": "2026-11-02", "modePaiement": "CARTE"},
                              {"montant": 330.00, "datePaiement": "2026-11-06", "modePaiement": "VIREMENT"},
                          ]},
                    tests=['pm.test("Statut 201", () => pm.response.to.have.status(201));',
                           'pm.test("Deux paiements rattaches", () => pm.expect(pm.response.json().paiements.length).to.eql(2));',
                           'pm.environment.set("contratAvecPaiementsId", pm.response.json().idContrat);']),
                req("Lister les paiements (les 2 paiements du contrat precedent apparaissent)", "GET", "paiements",
                    tests=['pm.test("Statut 200", () => pm.response.to.have.status(200));']),
                req("Modifier un contrat", "PUT", "contrats/{{contratId}}",
                    body={"dateSignature": "2026-11-02", "montantTotal": 520.00, "valide": True},
                    tests=['pm.test("Statut 200", () => pm.response.to.have.status(200));',
                           'pm.test("Montant mis a jour", () => pm.expect(pm.response.json().montantTotal).to.eql(520.0));']),
                req("Modifier un contrat inexistant (404)", "PUT", "contrats/999999",
                    body={"dateSignature": "2026-11-02", "montantTotal": 520.00, "valide": True},
                    tests=['pm.test("Statut 404", () => pm.response.to.have.status(404));']),
                req("Ajouter un paiement au contrat", "POST", "contrats/{{contratId}}/paiements",
                    body={"montant": 100.00, "datePaiement": "2026-11-02", "modePaiement": "ESPECES"},
                    tests=['pm.test("Statut 201", () => pm.response.to.have.status(201));',
                           'pm.environment.set("paiementAjouteId", pm.response.json().paiements[pm.response.json().paiements.length-1].idPaiement);']),
                req("Retirer un paiement du contrat", "DELETE",
                    "contrats/{{contratId}}/paiements/{{paiementAjouteId}}",
                    tests=['pm.test("Statut 200", () => pm.response.to.have.status(200));']),
                req("Supprimer un contrat (cascade sur ses paiements)", "DELETE", "contrats/{{contratId}}",
                    tests=['pm.test("Statut 204", () => pm.response.to.have.status(204));']),
                req("Supprimer un contrat inexistant (404)", "DELETE", "contrats/999999",
                    tests=['pm.test("Statut 404", () => pm.response.to.have.status(404));']),
            ],
        },
        {
            "name": "Paiements",
            "item": [
                req("Lister les paiements", "GET", "paiements",
                    tests=['pm.test("Statut 200", () => pm.response.to.have.status(200));']),
                req("Consulter un paiement inexistant (404)", "GET", "paiements/999999",
                    tests=['pm.test("Statut 404", () => pm.response.to.have.status(404));']),
                req("Creer un paiement (refuse)", "POST", "paiements",
                    body={"montant": 50.00, "datePaiement": "2026-11-02", "modePaiement": "CARTE"},
                    tests=['pm.test("Statut 405", () => pm.response.to.have.status(405));']),
            ],
        },
    ],
}

environment = {
    "id": str(uuid.uuid4()),
    "name": "local",
    "values": [
        {"key": "baseUrl", "value": "http://localhost:8089", "type": "default", "enabled": True},
    ],
    "_postman_variable_scope": "environment",
}

with open("AutoLoc.postman_collection.json", "w", encoding="utf-8") as f:
    json.dump(collection, f, indent=2, ensure_ascii=False)

with open("AutoLoc.postman_environment.json", "w", encoding="utf-8") as f:
    json.dump(environment, f, indent=2, ensure_ascii=False)

print("Generated AutoLoc.postman_collection.json and AutoLoc.postman_environment.json")

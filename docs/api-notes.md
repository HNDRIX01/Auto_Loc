# Atelier 5 — Notes : API REST, Postman et SpringDoc/Swagger

> Remarque de contexte : le port de l'application est `8089` (fixé à l'Atelier 1, conflit de
> port local), pas `8080` comme dans les exemples du support. Les URLs ci-dessous utilisent donc
> `http://localhost:8089/api/...`. `server.servlet.context-path=/api` a été ajouté dans
> `application.properties` comme demandé, et tous les `@RequestMapping` des contrôleurs ont été
> dépouillés du préfixe `/api`.

## A. Endpoints

| Contrôleur | Verbe et route | Service appelé | Succès | Erreurs |
|---|---|---|---|---|
| AgenceController | GET /agences | findAll() | 200 | — |
| AgenceController | GET /agences/{id} | findById(id) | 200 | 404 |
| AgenceController | POST /agences | create(agence) | 201 | 400 |
| AgenceController | PUT /agences/{id} | update(id, agence) | 200 | 404, 400 |
| AgenceController | DELETE /agences/{id} | deleteById(id) | 204 | 404 |
| EmployeController | GET /employes | findAll() | 200 | — |
| EmployeController | GET /employes/{id} | findById(id) | 200 | 404 |
| EmployeController | POST /employes | create(employe) | 201 | 400 |
| EmployeController | PUT /employes/{id} | update(id, employe) | 200 | 404, 400 |
| EmployeController | DELETE /employes/{id} | deleteById(id) | 204 | 404 |
| VehiculeController | GET /vehicules | findAll() | 200 | — |
| VehiculeController | GET /vehicules/{id} | findById(id) | 200 | 404 |
| VehiculeController | POST /vehicules | create(vehicule) | 201 | 400 |
| VehiculeController | PUT /vehicules/{id} | update(id, vehicule) | 200 | 404, 400 |
| VehiculeController | DELETE /vehicules/{id} | deleteById(id) | 204 | 404 |
| EquipementController | GET /equipements | findAll() | 200 | — |
| EquipementController | GET /equipements/{id} | findById(id) | 200 | 404 |
| EquipementController | POST /equipements | create(equipement) | 201 | 400 |
| EquipementController | PUT /equipements/{id} | update(id, equipement) | 200 | 404, 400 |
| EquipementController | DELETE /equipements/{id} | deleteById(id) | 204 | 404 |
| ClientController | GET /clients | findAll() | 200 | — |
| ClientController | GET /clients/{id} | findById(id) | 200 | 404 |
| ClientController | POST /clients | create(client) | 201 | 400 |
| ClientController | PUT /clients/{id} | update(id, client) | 200 | 404, 400 |
| ClientController | DELETE /clients/{id} | deleteById(id) | 204 | 404 |
| ReservationController | GET /reservations | findAll() | 200 | — |
| ReservationController | GET /reservations/{id} | findById(id) | 200 | 404 |
| ReservationController | POST /reservations | create(reservation) | 201 | 400 |
| ReservationController | PUT /reservations/{id} | update(id, reservation) | 200 | 404, 400 |
| ReservationController | DELETE /reservations/{id} | deleteById(id) | 204 | 404 |
| ContratController | GET /contrats | findAll() | 200 | — |
| ContratController | GET /contrats/{id} | findById(id) | 200 | 404 |
| ContratController | POST /contrats | create(contrat) | 201 | 400 |
| ContratController | PUT /contrats/{id} | update(id, contrat) | 200 | 404, 400 |
| ContratController | DELETE /contrats/{id} | deleteById(id) | 204 | 404 |
| ContratController | POST /contrats/{id}/paiements | ajouterPaiement(id, paiement) | 201 | 404 |
| ContratController | DELETE /contrats/{id}/paiements/{idPaiement} | retirerPaiement(id, idPaiement) | 200 | 404 |
| PaiementController | GET /paiements | findAll() | 200 | — |
| PaiementController | GET /paiements/{id} | findById(id) | 200 | 404 |
| PaiementController | POST /paiements | (non exposé) | — | **405** (aucune méthode `@PostMapping` déclarée) |
| MaintenanceController | GET /maintenances | findAll() | 200 | — |
| MaintenanceController | GET /maintenances/{id} | findById(id) | 200 | 404 |
| MaintenanceController | POST /maintenances | create(maintenance) | 201 | 400 |
| MaintenanceController | PUT /maintenances/{id} | update(id, maintenance) | 200 | 404, 400 |
| MaintenanceController | DELETE /maintenances/{id} | deleteById(id) | 204 | 404 |

## B. Annotations Jackson

| Association | Annotation retenue | Justification |
|---|---|---|
| `Contrat.paiements` ↔ `Paiement.contrat` | `@JsonManagedReference("contrat-paiements")` côté `Contrat.paiements`, `@JsonBackReference("contrat-paiements")` côté `Paiement.contrat` | Seule vraie **composition** du modèle (`cascade = ALL`, `orphanRemoval = true`) : le paiement se crée *dans* le JSON de son contrat (`POST /contrats` avec `"paiements": [...]`). Un simple `@JsonIgnore` sur `Paiement.contrat` cassait ce flux : testé et confirmé en reproduisant l'étape 5 de l'atelier — les paiements créés dans le tableau imbriqué étaient bien insérés en base, mais avec `contrat_id_contrat = NULL` (vérifié par requête SQL directe), car rien ne demandait à Jackson de recopier la référence vers le parent pendant la désérialisation. Remplacer la paire d'annotations corrige cela : Jackson assigne lui-même la référence arrière à l'objet parent en cours de construction. |
| `Agence.employes`, `Agence.vehicules` | `@JsonIgnore` | Enfants qui désignent leur agence par un identifiant (`{"agence": {"idAgence": 1}}`), pas l'inverse : ce n'est pas une composition au sens de l'atelier, juste une référence. Lister tous les employés/véhicules à chaque lecture d'une agence serait aussi coûteux qu'inutile. |
| `Client.reservations`, `Employe.reservations`, `Vehicule.reservations`, `Vehicule.maintenances`, `Equipement.vehicules`, `Contrat.reservation` | `@JsonIgnore` | Même logique : côté « liste/inverse » d'une relation où l'enfant (Reservation, Maintenance) se crée seul et référence son parent par id. **Aucun `@JsonBackReference` n'est posé sur `Reservation.client`/`Reservation.vehicule`/`Reservation.employe`/`Reservation.contrat`** : l'indication de l'atelier le précise explicitement — avec `@JsonBackReference`, la propriété serait ignorée à la désérialisation et un `POST /reservations` créerait une réservation sans client. Ces attributs restent donc sans annotation côté `Reservation`, et c'est le côté inverse (ex. `Client.reservations`) qui porte `@JsonIgnore`. |

**Différence `@JsonBackReference` / `@JsonIgnore` :** les deux cachent le champ en **sortie** (sérialisation). Mais `@JsonBackReference` reste actif en **entrée** (désérialisation) : couplé à `@JsonManagedReference` du même nom sur le champ parent, Jackson l'utilise pour relier automatiquement chaque enfant désérialisé à l'instance parent en cours de construction. `@JsonIgnore` est ignoré dans les deux sens : rien ne remplit jamais ce champ depuis le JSON entrant, ce qui est voulu quand le client envoie déjà la référence autrement (`{"id": 1}` sur l'attribut propriétaire côté enfant).

## C. Postman et Swagger

- **Postman** sert à **tester** : on écrit une requête rejouable, on l'exécute contre l'application réelle, on vérifie le code et le corps, on enchaîne des scénarios (créer → lire → modifier → supprimer). C'est l'outil qui a permis de **découvrir** le bug de l'étape 5 (section B) : en reproduisant exactement « POST /contrats avec paiements imbriqués » puis « GET /paiements », la réponse semblait correcte (les deux paiements apparaissaient avec un id), mais une vérification en base (`SELECT contrat_id_contrat FROM paiement`) a montré qu'ils n'étaient pas rattachés à leur contrat — un problème invisible dans la seule réponse JSON, que seul un test bout-en-bout permet de révéler.
- **Swagger UI** sert à **documenter et explorer** : la spécification OpenAPI est générée automatiquement depuis le code des contrôleurs (routes, schémas déduits des entités, codes de réponse annotés). Elle reste synchronisée avec le code sans effort supplémentaire — contrairement à une documentation écrite à la main. Exemple concret : la section *Schemas* de `/api/swagger-ui/index.html` affiche la structure de `Contrat` telle que déduite de l'entité JPA (y compris le fait que `paiements` y figure, grâce à `@JsonManagedReference`).
- Les deux sont complémentaires : Postman vérifie que l'API *fonctionne*, Swagger documente ce qu'elle *expose*.

## D. Anomalies SonarQube for IDE

| # | Anomalie | Règle / explication | Correction apportée |
|---|---|---|---|
| 1 | Chaîne de caractères `"Identifiant de l'agence"` (et son équivalent par entité : `"Identifiant du client"`, `"Identifiant du contrat"`…) répétée 3 fois dans chaque contrôleur CRUD (`findById`, `update`, `delete`) | **S1192** — *"String literals should not be duplicated."* Vérifié par relecture/`grep` systématique (3 occurrences exactes par fichier, sur les 8 contrôleurs CRUD). Une faute de frappe dans l'une des 3 occurrences desynchroniserait silencieusement la documentation Swagger. | Extrait en `private static final String ID_PARAM = "..."` dans chacun des 8 contrôleurs CRUD, réutilisé dans les trois `@Parameter(description = ID_PARAM)`. |
| 2 | Les contrôleurs exposent directement les entités JPA (`Contrat`, `Agence`, `Vehicule`…) comme corps de requête et de réponse | Explicitement nommée par le support de l'atelier lui-même (section 1.3, « couplage API/base, champs internes visibles ») comme anomalie attendue à cette étape. Risque réel : un client peut lire/écrire des champs techniques (proxies Hibernate, collections internes), et toute évolution du schéma de base casse directement le contrat de l'API. | **Non corrigée à ce stade** — volontairement reportée : le Chapitre 10 introduit la couche `web.dto` (DTO + validation déclarative) qui remplace les entités dans les signatures de contrôleur. Corriger maintenant dupliquerait le travail. |
| 3 | Injection par attribut testée lors des Ateliers 3/4 (`@Autowired private IContratRepository ...`) | **S6813** — déjà documentée dans `docs/service-notes.md` (Atelier 4, section A/D) ; mentionnée ici pour mémoire car l'étape 12 demande d'analyser `web.controller`, où la même règle s'applique si une injection par attribut y était introduite — ce n'est pas le cas : tous les contrôleurs utilisent `@RequiredArgsConstructor` + champ `final`, vérifié par relecture de chacun des 10 fichiers du package. | Aucune occurrence trouvée dans `web.controller` — règle respectée par construction, pas de correction nécessaire. |

> Note d'honnêteté (déjà posée à l'Atelier 4, toujours valable) : je n'ai pas accès au plugin
> SonarQube for IDE dans cet environnement (il tourne dans IntelliJ). Les anomalies 1 et 2
> viennent d'une relecture manuelle systématique (grep de littéraux dupliqués, lecture du support
> de cours) et d'une règle dont l'identifiant est connu avec certitude (S1192 est la règle
> standard pour les littéraux dupliqués) ; elles sont donc fiables, mais lancez *Analyze with
> SonarQube for IDE* sur `web.controller` pour obtenir la liste officielle complète et vérifier
> qu'aucune anomalie supplémentaire n'a été manquée.

## E. Questions de compréhension

**1. Quel composant reçoit toutes les requêtes, et comment retrouve-t-il la méthode du
contrôleur à appeler ?**
Le `DispatcherServlet` : c'est le point d'entrée unique de toute application Spring MVC (déclaré
automatiquement par Spring Boot). Il délègue à un `HandlerMapping` qui associe l'URL et le verbe
HTTP de la requête entrante à la méthode de contrôleur correspondante, en se basant sur
`@RequestMapping`/`@GetMapping`/etc. et le `context-path` configuré (`/api`).

**2. Pourquoi `ContratController` dépend-il de `IContratService` et non de `ContratRepository`
ni de `ContratServiceImpl` ?**
Pas de `ContratRepository` : le contrôleur ne doit jamais contourner la couche métier pour
accéder directement aux données (règle « contrôleurs minces », Atelier 4 : « le code métier passe
par le Service »). Pas de `ContratServiceImpl` : même principe de dépendance à l'abstraction déjà
appliqué entre service et repository (Atelier 4) — le contrôleur dépend de l'interface, jamais de
l'implémentation concrète, pour rester remplaçable sans modification.

**3. Pourquoi un POST renvoie-t-il 201 et un DELETE 204 ? Pourquoi une ressource absente
donne-t-elle 404 et non 500 ?**
201 (*Created*) signale qu'une nouvelle ressource a été créée, avec son identifiant dans le
corps de la réponse. 204 (*No Content*) signale un succès qui ne renvoie volontairement aucun
corps : après suppression, il n'y a plus rien à décrire. Une ressource absente est une situation
**normale et prévisible** de l'API (le client a simplement demandé un id qui n'existe pas) ; 404
le dit explicitement au client pour qu'il corrige sa requête. 500 signifierait une erreur
**inattendue côté serveur** (bug, panne) — ce n'est pas le cas ici, et c'est pour cette raison que
le service traduit l'absence en `ResourceNotFoundException` dédiée, interceptée par
`GlobalExceptionHandler` pour produire un 404 au lieu de laisser remonter une exception générique.

**4. Pourquoi `PaiementController` n'expose-t-il que des GET ? Quelle réponse reçoit un POST
/paiements ?**
Parce que `Paiement` est une composition de `Contrat` (Atelier 4) : son cycle de vie complet
(création, retrait) est piloté par `ContratServiceImpl.ajouterPaiement`/`retirerPaiement`, pour
garantir que les règles métier du contrat (validation, cascade, `orphanRemoval`) s'appliquent
systématiquement. Exposer un `POST /paiements` indépendant permettrait de créer un paiement sans
contrat valide. Un `POST /api/paiements` reçoit **405 Method Not Allowed** — Spring MVC le
renvoie automatiquement dès qu'une route existe (`/paiements` a des `@GetMapping`) mais qu'aucune
méthode ne gère le verbe demandé ; vérifié par requête réelle.

**5. Quels risques présente l'exposition directe des entités JPA ? Pourquoi `Contrat.paiements`
utilise-t-il `@JsonManagedReference` alors que `Agence.vehicules` utilise `@JsonIgnore` ?**
Risques : couplage fort entre le schéma de base et le contrat d'API (toute migration de colonne
casse l'API), fuite de champs techniques (proxies Hibernate, `hibernateLazyInitializer`),
absence de validation d'entrée dédiée à l'API (les contraintes JPA ne sont pas conçues pour ça),
et incapacité à exposer une forme différente de la donnée selon le contexte (liste résumée vs
détail complet). `Contrat.paiements` utilise `@JsonManagedReference` parce que les paiements sont
une vraie composition **destinée à être créée dans le JSON du contrat** — Jackson doit pouvoir
reconstruire la relation bidirectionnelle à la désérialisation. `Agence.vehicules` utilise
`@JsonIgnore` parce que ce n'est pas une composition au sens de l'atelier (un véhicule existe
indépendamment, se crée seul et référence son agence par id) : il n'y a jamais besoin que Jackson
reconstruise cette liste à partir d'un JSON entrant, seulement d'éviter qu'elle boucle à la
sérialisation.

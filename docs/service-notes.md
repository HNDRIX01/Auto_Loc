# Atelier 4 — Notes : couche Service, IoC et injection de dépendances

## A. Modes d'injection comparés

Expérience faite sur `AgenceServiceImpl` (étape 2) : remplacement temporaire de
`@RequiredArgsConstructor` + champ `final` par une injection par attribut
(`@Autowired private IAgenceRepository agenceRepository;`), puis restauration.

| Critère | Constructeur (`@RequiredArgsConstructor` + `final`) | Attribut (`@Autowired`) |
|---|---|---|
| Champ `final` possible | Oui | Non — un champ `@Autowired` ne peut pas être `final` (il est assigné après construction par réflexion) |
| Dépendance visible dans la signature de la classe | Oui — visible dans le constructeur généré, donc dans l'API publique de la classe | Non — cachée dans le corps de la classe, invisible sans lire tous les champs |
| Utilisable avec `new` hors conteneur sans dépendance `null` | Oui — `new AgenceServiceImpl(repo)` force à fournir la dépendance ; impossible de construire un objet incomplet | Non — `new AgenceServiceImpl()` compile et donne un objet dont `agenceRepository` vaut `null` ; NPE au premier appel |
| Signalé par SonarQube for IDE | Non | Oui — règle **S6813** *"Field injection is not recommended"* |

**Conclusion :** l'injection par constructeur est restaurée comme convention définitive de tous
les services AutoLoc : dépendances explicites, objet toujours valide, testable sans conteneur.

## B. Services — dépendances injectées

| Service | Dépendances injectées | Mode | Justification |
|---|---|---|---|
| `AgenceServiceImpl` | `IAgenceRepository` | Constructeur | CRUD simple sur une seule table, pas d'association à résoudre. |
| `EmployeServiceImpl` | `IEmployeRepository` | Constructeur | CRUD simple ; `Employe.agence` est une simple référence FK (`@ManyToOne` sans cascade), transmise telle quelle. |
| `VehiculeServiceImpl` | `IVehiculeRepository`, `IEquipementRepository` | Constructeur | Deuxième dépendance nécessaire pour résoudre par id les `Equipement` reçus dans `Vehicule.equipements` (`@ManyToMany`) avant `save` — sans cette résolution, un `Equipement` partiellement rempli (juste l'id) écraserait la ligne existante en base (bug réel rencontré et corrigé pendant l'Atelier 5, voir commit `6dc6d78`). |
| `EquipementServiceImpl` | `IEquipementRepository` | Constructeur | CRUD simple. |
| `ClientServiceImpl` | `IClientRepository` | Constructeur | CRUD simple. |
| `ReservationServiceImpl` | `IReservationRepository` | Constructeur | CRUD simple ; les références `vehicule`/`employe`/`client`/`contrat` sont de simples FK sans cascade, transmises telles quelles (cf. indication de l'atelier : la gestion fine des liens est hors-scope ici). |
| `ContratServiceImpl` | `IContratRepository` | Constructeur | Porte en plus la composition Contrat → Paiement (`ajouterPaiement`/`retirerPaiement`), qui s'appuie sur `cascade=ALL` + `orphanRemoval=true` déjà posés sur `Contrat.paiements` — pas besoin d'un second repository. |
| `PaiementServiceImpl` | `IPaiementRepository` | Constructeur | Lecture seule (`findById`, `findAll`) ; aucune écriture n'est exposée, conformément à la règle « Paiement se crée/se retire via Contrat ». |
| `MaintenanceServiceImpl` | `IMaintenanceRepository` | Constructeur | CRUD simple. |

Aucun service ne dépend d'un autre service : pas de risque de cycle (`BeanCurrentlyInCreationException`).

## C. Messages d'erreur du conteneur

**Message 1 — `NoSuchBeanDefinitionException`** (`IContratService` introuvable pour
`ContratController`)
Deux causes les plus probables côté `ContratServiceImpl` :
1. L'annotation `@Service` a été oubliée (ou mal importée) sur `ContratServiceImpl` : la classe
   n'est alors jamais détectée par le scan, donc aucune définition de bean n'existe pour
   `IContratService`.
2. `ContratServiceImpl` se trouve dans un package hors de `tn.esprit.autoloc` (ou d'un de ses
   sous-packages) : le scan démarré par `@SpringBootApplication` depuis le package racine ne la
   voit jamais, même si elle est bien annotée.

**Message 2 — `NoUniqueBeanDefinitionException`** (deux beans `INotificateur` :
`emailNotificateur` et `smsNotificateur`)
Deux corrections possibles :
1. Poser `@Primary` **sur la classe** du bean qui doit être le candidat par défaut
   (ex. `@Primary` sur `EmailNotificateur`) : il sera choisi automatiquement partout où aucun
   `@Qualifier` n'est précisé.
2. Poser `@Qualifier("smsNotificateur")` **sur le paramètre du constructeur** du consommateur
   (`NotificationController`) : choix explicite et local, sans affecter les autres injections de
   `INotificateur` ailleurs dans l'application.

**Message 3 — Dépendance circulaire** (`clientServiceImpl` ↔ `reservationServiceImpl`)
L'injection par constructeur rend ce cycle impossible à contourner parce que, pour construire
`ClientServiceImpl`, Spring doit d'abord disposer d'une instance complète de
`ReservationServiceImpl` — qui elle-même exige une instance complète de `ClientServiceImpl` :
aucun des deux objets ne peut jamais finir de se construire en premier (contrairement à
l'injection par setter/attribut, où Spring peut créer les deux objets « vides » puis les
câbler l'un à l'autre après coup, ce qui masque le problème de conception au lieu de le
signaler).
**Piste de conception :** extraire la responsabilité partagée dans un troisième service (ou
dans un repository commun) dont `ClientServiceImpl` et `ReservationServiceImpl` dépendraient
tous les deux, au lieu de se référencer mutuellement — cf. règle de l'atelier : « deux services
ne doivent pas se référencer mutuellement ».

## D. Anomalies SonarQube for IDE

| # | Anomalie | Règle / explication | Correction apportée |
|---|---|---|---|
| 1 | Injection par attribut (`@Autowired private IAgenceRepository ...`) testée à l'étape 2 | **S6813** — *"Field injection is not recommended, use constructor injection instead."* Champ non `final`, dépendance cachée, objet instanciable incomplet hors conteneur. | Restauré en injection par constructeur (`@RequiredArgsConstructor` + champ `final`), convention appliquée aux neuf services. |
| 2 | `findById` de `ContratServiceImpl` testé avec `throw new RuntimeException(...)` au lieu de l'exception métier | **S112** — *"Generic exceptions should never be thrown."* Une `RuntimeException` nue ne documente rien sur la cause et ne peut pas être interceptée sélectivement par un `@ExceptionHandler`. | Restauré `ResourceNotFoundException` (package `exception`), qui porte un message explicite et est interceptée spécifiquement par `GlobalExceptionHandler` → 404. |
| 3 | Faute de frappe dans le nom du champ `immatriuclation` de `Vehicule` (pour « immatriculation ») — présente depuis l'Atelier 1, propagée au nom de colonne SQL et à la clé JSON de l'API REST | Relevée par relecture manuelle, pas par SonarLint (cette règle précise n'a pas été vérifiée avec le plugin — à confirmer dans IntelliJ). Un nom de champ incorrect nuit à la lisibilité et sera visible par tout consommateur de l'API. | **Non corrigée pour l'instant** : renommer `immatriuclation` → `immatriculation` casserait la compatibilité de l'API et de la colonne déjà générée en base (Hibernate `ddl-auto=update` dupliquerait la colonne au lieu de la renommer, cf. Atelier 1). À traiter dans une migration dédiée, pas en passant. |

> Note d'honnêteté : les anomalies 1 et 2 sont directement données par l'énoncé de l'atelier
> (règles S6813 et S112 nommées dans le PDF) et ont été vérifiées en reproduisant l'expérience
> décrite. Je n'ai pas accès au plugin SonarQube for IDE dans cet environnement (il tourne dans
> IntelliJ) : l'anomalie 3 vient d'une relecture manuelle du code, pas d'une analyse SonarLint
> réelle. Lancez *Analyze with SonarQube for IDE* sur les packages `service` et `exception` pour
> obtenir la liste officielle et son détail de règles — complétez/corrigez ce tableau avec les
> résultats réels obtenus.

## E. Questions de compréhension

**1. Où est le `new` de `ContratServiceImpl` et qui l'exécute ?**
Il n'y en a pas dans le code applicatif. Le conteneur Spring (`ApplicationContext`), au
démarrage, détecte `ContratServiceImpl` via `@Service` (scan déclenché par
`@SpringBootApplication`), résout sa seule dépendance (`IContratRepository`, dont le proxy
Spring Data est créé avant), puis appelle lui-même le constructeur généré par
`@RequiredArgsConstructor`. Le `new` existe bel et bien, mais à l'intérieur du conteneur, pas
dans notre code.

**2. Pourquoi un contrôleur dépendra-t-il de `IContratService` et non de `ContratServiceImpl` ?**
Parce que chaque couche doit dépendre d'une abstraction, jamais d'une implémentation
concrète (principe appliqué à chaque étage : Controller → `IContratService`,
`ContratServiceImpl` → `IContratRepository`). Cela permet de remplacer l'implémentation (une
version avec cache, une version de test/mock) sans toucher au contrôleur, et garde le
contrôleur ignorant des détails métier et de persistance.

**3. Pourquoi un service singleton doit-il rester sans état ? Exemple de bug.**
Un service `@Service` est un singleton partagé par toutes les requêtes HTTP concurrentes (tous
les threads du serveur utilisent la même instance). S'il stockait un état propre à un appel
(ex. un champ `private Contrat contratCourant;` rempli par `create()` puis relu par une autre
méthode), deux requêtes simultanées de deux clients différents écraseraient ce champ l'une
après l'autre : le client A pourrait recevoir ou agir sur le contrat du client B. C'est une
« race condition » qui ne se voit pas en test mono-utilisateur et qui explose en production
sous charge concurrente.

**4. Pourquoi `update` charge-t-il l'entité existante avant d'appeler `save` au lieu de lui
passer directement l'objet reçu ?**
Parce que l'objet JSON reçu du client est partiel ou différemment structuré que l'entité
complète (ex. collections `@JsonIgnore` jamais envoyées, donc nulles/vides côté client). Passer
directement cet objet à `save` déclencherait un `merge` qui écraserait toutes les colonnes de la
ligne existante avec les valeurs (souvent nulles) de l'objet reçu — et, pour `Contrat`, avec
`orphanRemoval=true` sur `paiements`, cela supprimerait silencieusement tous les paiements du
contrat. Charger l'existant puis recopier explicitement seulement les champs modifiables évite
ces deux pertes de données.

**5. `@Component` vs `@Bean` ? `@Primary` vs `@Qualifier` ? Pourquoi `IPaiementService` n'expose
ni création, ni modification, ni suppression ?**
- `@Component` (et ses spécialisations `@Service`/`@Repository`/`@Controller`) s'applique
  **sur une classe qu'on possède** : Spring instancie lui-même la classe annotée via son
  constructeur. `@Bean` s'applique **sur une méthode** d'une classe `@Configuration` : utile pour
  déclarer comme bean une classe qu'on ne peut pas annoter (bibliothèque tierce, classe du JDK)
  ou dont la construction nécessite de la logique.
- `@Primary` se pose **sur la classe candidate par défaut** : résolution automatique et globale
  de l'ambiguïté. `@Qualifier("nom")` se pose **au point d'injection** (paramètre du
  constructeur) : choix explicite et local, cas par cas.
- `IPaiementService` n'expose que `findById`/`findAll` parce qu'un `Paiement` est une
  **composition** de `Contrat` (`cascade=ALL`, `orphanRemoval=true` sur `Contrat.paiements`) :
  son cycle de vie est entièrement piloté par son parent. Lui donner directement `create`/
  `update`/`deleteById` permettrait de créer un paiement sans contrat, ou de le modifier/
  supprimer en contournant les règles métier portées par `ContratServiceImpl`
  (`ajouterPaiement`/`retirerPaiement`) — c'est exactement la règle énoncée à l'Atelier 3 :
  « le code métier passe par le Contrat ».

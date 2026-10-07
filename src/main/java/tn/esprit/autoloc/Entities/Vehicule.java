package tn.esprit.autoloc.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Vehicule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long idVehicule;

    @Column(nullable = false, unique = true, length = 25)
    String immatriuclation;

    @Column(nullable=false, length=50)
    String marque;

    @Column(nullable = false, length = 25)
    String modele;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    CategorieVehicule categorie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length =25)
    StatutVehicule statut;

    @Column(nullable = false, precision = 10, scale =2)
    BigDecimal tarifJournalier;

    @ToString.Exclude
    @ManyToOne
    private Agence agence;

    @ToString.Exclude
    @OneToMany(mappedBy = "vehicule", cascade = CascadeType.ALL)
    private Set<Maintenance> maintenances = new HashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "vehicule", cascade = CascadeType.ALL)
    private Set<Reservation> reservations = new HashSet<>();

    @ToString.Exclude
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<Equipement> equipements = new HashSet<>();



}

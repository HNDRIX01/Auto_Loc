package tn.esprit.autoloc.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

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






}

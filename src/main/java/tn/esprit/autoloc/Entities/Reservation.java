package tn.esprit.autoloc.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReservation;

    @Column(nullable = false)
    private LocalDate dateDebut;

    @Column(nullable = false)
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutReservation statut;

    @ToString.Exclude
    @ManyToOne
    private Vehicule vehicule;

    @ToString.Exclude
    @ManyToOne
    private Employe employe;

    @ToString.Exclude
    @ManyToOne
    private Client client;

    @ToString.Exclude
    @OneToOne(cascade = CascadeType.ALL)
    private Contrat contrat;
}

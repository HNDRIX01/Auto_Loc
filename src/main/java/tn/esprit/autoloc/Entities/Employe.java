package tn.esprit.autoloc.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Employe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEmploye;

    @Column(nullable = false, length = 50)
    private String nom;

    @Column(nullable = false, length = 50)
    private String prenom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoleEmploye role;

    @ToString.Exclude
    @ManyToOne
    private Agence agence;

    @ToString.Exclude
    @OneToMany(mappedBy = "employe", cascade = CascadeType.ALL)
    private Set<Reservation> reservations = new HashSet<>();
}

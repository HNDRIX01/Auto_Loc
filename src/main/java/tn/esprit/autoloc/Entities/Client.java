package tn.esprit.autoloc.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idClient;

    @Column(nullable = false, length = 50)
    private String nom;

    @Column(nullable = false, length = 50)
    private String prenom;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 20)
    private String telephone;

    @Column(nullable = false, unique = true, length = 25)
    private String numPermis;

    @Column(nullable = false)
    private LocalDate dateInscription;

    @ToString.Exclude
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private Set<Reservation> reservations = new HashSet<>();
}

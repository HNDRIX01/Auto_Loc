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
public class Equipement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEquipement;

    @Column(nullable = false, length = 50)
    private String libelle;

    @ToString.Exclude
    @ManyToMany(mappedBy = "equipements")
    private Set<Vehicule> vehicules = new HashSet<>();
}

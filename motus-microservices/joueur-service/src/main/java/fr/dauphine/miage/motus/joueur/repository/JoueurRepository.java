package fr.dauphine.miage.motus.joueur.repository;

import fr.dauphine.miage.motus.joueur.model.Joueur;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JoueurRepository extends JpaRepository<Joueur, Long> {

    Optional<Joueur> findByPseudo(String pseudo);

    Optional<Joueur> findByEmail(String email);

    boolean existsByPseudo(String pseudo);

    boolean existsByEmail(String email);
}

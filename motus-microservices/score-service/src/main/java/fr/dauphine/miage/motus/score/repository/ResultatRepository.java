package fr.dauphine.miage.motus.score.repository;

import fr.dauphine.miage.motus.score.model.Resultat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResultatRepository extends JpaRepository<Resultat, Long> {

    List<Resultat> findByJoueurId(Long joueurId);
}

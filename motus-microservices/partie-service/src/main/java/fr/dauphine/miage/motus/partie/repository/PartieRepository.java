package fr.dauphine.miage.motus.partie.repository;

import fr.dauphine.miage.motus.partie.model.Partie;
import fr.dauphine.miage.motus.partie.model.Statut;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PartieRepository extends JpaRepository<Partie, Long> {

    List<Partie> findByJoueurId(Long joueurId);

    List<Partie> findByStatut(Statut statut);
}

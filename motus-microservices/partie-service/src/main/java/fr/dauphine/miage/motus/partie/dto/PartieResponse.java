package fr.dauphine.miage.motus.partie.dto;

import fr.dauphine.miage.motus.partie.model.Statut;
import java.time.LocalDateTime;
import java.util.List;

public record PartieResponse(Long id, Long joueurId, int longueur, int essaisMax,
                             int essaisUtilises, Statut statut, LocalDateTime dateDebut,
                             LocalDateTime dateFin, String motMystere, List<EssaiResponse> essais) {
}

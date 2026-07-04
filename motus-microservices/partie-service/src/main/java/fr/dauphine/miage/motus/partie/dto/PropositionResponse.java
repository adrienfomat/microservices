package fr.dauphine.miage.motus.partie.dto;

import fr.dauphine.miage.motus.partie.model.EtatLettre;
import fr.dauphine.miage.motus.partie.model.Statut;
import java.util.List;

public record PropositionResponse(List<EtatLettre> resultat, Statut statut,
                                  int essaisRestants, String motMystere) {
}

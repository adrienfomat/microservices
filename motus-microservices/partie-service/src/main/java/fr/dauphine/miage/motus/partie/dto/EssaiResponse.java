package fr.dauphine.miage.motus.partie.dto;

import fr.dauphine.miage.motus.partie.model.EtatLettre;
import java.util.List;

public record EssaiResponse(int ordre, String mot, List<EtatLettre> resultat) {
}

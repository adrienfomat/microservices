package fr.dauphine.miage.motus.joueur.dto;

import java.time.LocalDateTime;

public record JoueurResponse(Long id, String pseudo, String email, boolean admin,
                             LocalDateTime dateInscription) {
}

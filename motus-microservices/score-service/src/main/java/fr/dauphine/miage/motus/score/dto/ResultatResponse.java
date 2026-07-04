package fr.dauphine.miage.motus.score.dto;

import java.time.LocalDateTime;

public record ResultatResponse(Long id, Long joueurId, Long partieId, boolean gagnee,
                               int nombreEssais, int points, LocalDateTime date) {
}

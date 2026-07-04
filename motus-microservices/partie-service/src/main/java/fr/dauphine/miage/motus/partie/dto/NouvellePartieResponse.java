package fr.dauphine.miage.motus.partie.dto;

public record NouvellePartieResponse(Long partieId, int longueur, char premiereLettre,
                                     int essaisMax, int essaisRestants) {
}

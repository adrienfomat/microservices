package fr.dauphine.miage.motus.partie.dto;

public record ResultatMessage(Long joueurId, Long partieId, boolean gagnee,
                              int nombreEssais, int points) {
}

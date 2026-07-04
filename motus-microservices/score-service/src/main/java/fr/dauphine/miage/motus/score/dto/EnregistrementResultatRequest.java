package fr.dauphine.miage.motus.score.dto;

public record EnregistrementResultatRequest(Long joueurId, Long partieId, boolean gagnee,
                                            int nombreEssais, int points) {
}

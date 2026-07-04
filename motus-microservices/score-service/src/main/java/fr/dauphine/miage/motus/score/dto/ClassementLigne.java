package fr.dauphine.miage.motus.score.dto;

public record ClassementLigne(Long joueurId, String pseudo, long points, long partiesJouees,
                              long partiesGagnees, double pointsParPartie) {
}

package fr.dauphine.miage.motus.joueur.dto;

// identifiant = pseudo OR email
public record ConnexionRequest(String identifiant, String motDePasse) {
}

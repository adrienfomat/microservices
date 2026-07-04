package fr.dauphine.miage.motus.partie.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class JoueurClient {

    private final RestClient restClient;

    public JoueurClient(@Value("${services.joueur.url}") String joueurUrl) {
        this.restClient = RestClient.create(joueurUrl);
    }

    // check that the player exists before starting a game
    public boolean existe(Long joueurId) {
        try {
            restClient.get()
                    .uri("/joueurs/{id}", joueurId)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

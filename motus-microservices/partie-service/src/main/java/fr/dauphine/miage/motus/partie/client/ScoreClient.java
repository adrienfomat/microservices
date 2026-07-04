package fr.dauphine.miage.motus.partie.client;

import fr.dauphine.miage.motus.partie.dto.ResultatMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ScoreClient {

    private final RestClient restClient;

    public ScoreClient(@Value("${services.score.url}") String scoreUrl) {
        this.restClient = RestClient.create(scoreUrl);
    }

    // report a finished game to the score service, best effort so the game never fails
    public void envoyerResultat(ResultatMessage message) {
        try {
            restClient.post()
                    .uri("/resultats")
                    .body(message)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            // ignore: the score service being down must not break the game
        }
    }
}

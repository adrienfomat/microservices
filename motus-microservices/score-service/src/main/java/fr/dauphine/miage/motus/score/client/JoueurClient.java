package fr.dauphine.miage.motus.score.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class JoueurClient {

    private final RestClient restClient;

    public JoueurClient(@Value("${services.joueur.url}") String joueurUrl) {
        this.restClient = RestClient.create(joueurUrl);
    }

    // fetch the pseudo to display in the ranking, fall back to the id if unreachable
    public String pseudo(Long joueurId) {
        try {
            Map<?, ?> body = restClient.get()
                    .uri("/joueurs/{id}", joueurId)
                    .retrieve()
                    .body(Map.class);
            Object pseudo = body == null ? null : body.get("pseudo");
            return pseudo == null ? ("joueur#" + joueurId) : pseudo.toString();
        } catch (Exception e) {
            return "joueur#" + joueurId;
        }
    }
}

package fr.dauphine.miage.motus.partie.service;

import fr.dauphine.miage.motus.partie.model.EtatLettre;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MotusService {

    // compare a guess with the secret word using the classic two-pass Motus rule
    // pass 1 marks letters at the right place, pass 2 marks letters present elsewhere
    public List<EtatLettre> comparer(String proposition, String motMystere) {
        int taille = motMystere.length();
        List<EtatLettre> resultat = new ArrayList<>(taille);
        char[] secret = motMystere.toCharArray();
        char[] essai = proposition.toCharArray();
        boolean[] consomme = new boolean[taille];

        for (int i = 0; i < taille; i++) {
            resultat.add(EtatLettre.ABSENT);
        }

        for (int i = 0; i < taille; i++) {
            if (essai[i] == secret[i]) {
                resultat.set(i, EtatLettre.BIEN_PLACE);
                consomme[i] = true;
            }
        }

        for (int i = 0; i < taille; i++) {
            if (resultat.get(i) == EtatLettre.BIEN_PLACE) {
                continue;
            }
            for (int j = 0; j < taille; j++) {
                if (!consomme[j] && essai[i] == secret[j]) {
                    resultat.set(i, EtatLettre.MAL_PLACE);
                    consomme[j] = true;
                    break;
                }
            }
        }
        return resultat;
    }
}

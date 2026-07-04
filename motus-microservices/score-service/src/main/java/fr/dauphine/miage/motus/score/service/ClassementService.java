package fr.dauphine.miage.motus.score.service;

import fr.dauphine.miage.motus.score.client.JoueurClient;
import fr.dauphine.miage.motus.score.dto.ClassementLigne;
import fr.dauphine.miage.motus.score.model.Resultat;
import fr.dauphine.miage.motus.score.repository.ResultatRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ClassementService {

    private final ResultatRepository repository;
    private final JoueurClient joueurClient;

    public ClassementService(ResultatRepository repository, JoueurClient joueurClient) {
        this.repository = repository;
        this.joueurClient = joueurClient;
    }

    // leaderboard aggregated per player, ordered by total points
    public List<ClassementLigne> classement() {
        Map<Long, List<Resultat>> parJoueur = repository.findAll().stream()
                .collect(Collectors.groupingBy(Resultat::getJoueurId));

        List<ClassementLigne> lignes = new ArrayList<>();
        for (Map.Entry<Long, List<Resultat>> entree : parJoueur.entrySet()) {
            Long joueurId = entree.getKey();
            List<Resultat> resultats = entree.getValue();
            long jouees = resultats.size();
            long gagnees = resultats.stream().filter(Resultat::isGagnee).count();
            long points = resultats.stream().mapToLong(Resultat::getPoints).sum();
            double parPartie = jouees == 0 ? 0.0 : Math.round((double) points / jouees);
            lignes.add(new ClassementLigne(joueurId, joueurClient.pseudo(joueurId),
                    points, jouees, gagnees, parPartie));
        }

        lignes.sort(Comparator.comparingLong(ClassementLigne::points).reversed());
        return lignes;
    }
}

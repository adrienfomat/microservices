package fr.dauphine.miage.motus.partie.service;

import fr.dauphine.miage.motus.partie.client.JoueurClient;
import fr.dauphine.miage.motus.partie.client.ScoreClient;
import fr.dauphine.miage.motus.partie.dto.EssaiResponse;
import fr.dauphine.miage.motus.partie.dto.NouvellePartieResponse;
import fr.dauphine.miage.motus.partie.dto.PartieResponse;
import fr.dauphine.miage.motus.partie.dto.PropositionResponse;
import fr.dauphine.miage.motus.partie.dto.ResultatMessage;
import fr.dauphine.miage.motus.partie.model.Essai;
import fr.dauphine.miage.motus.partie.model.EtatLettre;
import fr.dauphine.miage.motus.partie.model.Partie;
import fr.dauphine.miage.motus.partie.model.Statut;
import fr.dauphine.miage.motus.partie.repository.PartieRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Service
public class GestionPartieService {

    private static final int ESSAIS_MAX = 6;
    private static final int LONGUEUR_DEFAUT = 6;

    private final PartieRepository repository;
    private final DictionnaireService dictionnaire;
    private final MotusService motus;
    private final JoueurClient joueurClient;
    private final ScoreClient scoreClient;

    public GestionPartieService(PartieRepository repository, DictionnaireService dictionnaire,
                                MotusService motus, JoueurClient joueurClient, ScoreClient scoreClient) {
        this.repository = repository;
        this.dictionnaire = dictionnaire;
        this.motus = motus;
        this.joueurClient = joueurClient;
        this.scoreClient = scoreClient;
    }

    // start a new game for a chosen word length, after validating the player
    @Transactional
    public NouvellePartieResponse demarrer(Long joueurId, Integer longueurDemandee) {
        if (joueurId == null || !joueurClient.existe(joueurId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Joueur inconnu");
        }
        int longueur = longueurDemandee == null ? LONGUEUR_DEFAUT : longueurDemandee;
        if (!dictionnaire.longueurDisponible(longueur)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Longueur non disponible (de " + DictionnaireService.LONGUEUR_MIN
                            + " a " + DictionnaireService.LONGUEUR_MAX + " lettres)");
        }
        String mot = dictionnaire.motAleatoire(longueur);
        Partie partie = repository.save(new Partie(joueurId, mot, ESSAIS_MAX));
        return new NouvellePartieResponse(partie.getId(), partie.getLongueur(),
                mot.charAt(0), partie.getNombreEssaisMax(), partie.essaisRestants());
    }

    // handle a guess, update the game and push the result when it is over
    @Transactional
    public PropositionResponse proposer(Long partieId, String motPropose) {
        Partie partie = repository.findById(partieId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partie introuvable"));

        if (partie.getStatut() != Statut.EN_COURS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La partie est deja terminee");
        }
        String mot = dictionnaire.normaliser(motPropose);
        if (mot.length() != partie.getLongueur()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le mot doit contenir " + partie.getLongueur() + " lettres");
        }
        if (!dictionnaire.contient(mot)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ce mot n'existe pas dans le dictionnaire");
        }

        List<EtatLettre> etats = motus.comparer(mot, partie.getMotMystere());
        partie.ajouterEssai(new Essai(partie.getNombreEssaisUtilises() + 1, mot, encoder(etats)));

        boolean gagne = mot.equals(partie.getMotMystere());
        if (gagne) {
            partie.terminer(Statut.GAGNEE);
        } else if (partie.essaisRestants() == 0) {
            partie.terminer(Statut.PERDUE);
        }
        repository.save(partie);

        if (partie.getStatut() != Statut.EN_COURS) {
            int points = calculerPoints(partie);
            scoreClient.envoyerResultat(new ResultatMessage(partie.getJoueurId(), partie.getId(),
                    partie.getStatut() == Statut.GAGNEE, partie.getNombreEssaisUtilises(), points));
        }

        String motRevele = partie.getStatut() == Statut.EN_COURS ? null : partie.getMotMystere();
        return new PropositionResponse(etats, partie.getStatut(), partie.essaisRestants(), motRevele);
    }

    @Transactional(readOnly = true)
    public PartieResponse detail(Long partieId) {
        Partie partie = repository.findById(partieId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partie introuvable"));
        return toResponse(partie);
    }

    // list games with optional filters: player, status and a date range on the start date
    @Transactional(readOnly = true)
    public List<PartieResponse> lister(Long joueurId, Statut statut, LocalDate du, LocalDate au) {
        Stream<Partie> flux = repository.findAll().stream();
        if (joueurId != null) {
            flux = flux.filter(p -> joueurId.equals(p.getJoueurId()));
        }
        if (statut != null) {
            flux = flux.filter(p -> p.getStatut() == statut);
        }
        if (du != null) {
            flux = flux.filter(p -> !p.getDateDebut().toLocalDate().isBefore(du));
        }
        if (au != null) {
            flux = flux.filter(p -> !p.getDateDebut().toLocalDate().isAfter(au));
        }
        return flux.map(this::toResponse).toList();
    }

    // more remaining attempts and longer words are worth more points
    private int calculerPoints(Partie partie) {
        if (partie.getStatut() != Statut.GAGNEE) {
            return 0;
        }
        return (partie.essaisRestants() + 1) * 20 + partie.getLongueur() * 5;
    }

    private String encoder(List<EtatLettre> etats) {
        return etats.stream().map(Enum::name).reduce((a, b) -> a + "," + b).orElse("");
    }

    private List<EtatLettre> decoder(String csv) {
        if (csv == null || csv.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(csv.split(",")).map(EtatLettre::valueOf).toList();
    }

    private PartieResponse toResponse(Partie partie) {
        List<EssaiResponse> essais = partie.getEssais().stream()
                .map(e -> new EssaiResponse(e.getOrdre(), e.getMot(), decoder(e.getResultat())))
                .toList();
        String motRevele = partie.getStatut() == Statut.EN_COURS ? null : partie.getMotMystere();
        return new PartieResponse(partie.getId(), partie.getJoueurId(), partie.getLongueur(),
                partie.getNombreEssaisMax(), partie.getNombreEssaisUtilises(), partie.getStatut(),
                partie.getDateDebut(), partie.getDateFin(), motRevele, essais);
    }
}

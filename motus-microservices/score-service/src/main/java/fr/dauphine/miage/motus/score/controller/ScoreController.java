package fr.dauphine.miage.motus.score.controller;

import fr.dauphine.miage.motus.score.dto.ClassementLigne;
import fr.dauphine.miage.motus.score.dto.EnregistrementResultatRequest;
import fr.dauphine.miage.motus.score.dto.ResultatResponse;
import fr.dauphine.miage.motus.score.model.Resultat;
import fr.dauphine.miage.motus.score.repository.ResultatRepository;
import fr.dauphine.miage.motus.score.service.ClassementService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

@RestController
public class ScoreController {

    private final ResultatRepository repository;
    private final ClassementService classementService;

    public ScoreController(ResultatRepository repository, ClassementService classementService) {
        this.repository = repository;
        this.classementService = classementService;
    }

    // record a finished game, called by the partie-service
    @PostMapping("/resultats")
    public ResponseEntity<ResultatResponse> enregistrer(@RequestBody EnregistrementResultatRequest request) {
        Resultat resultat = repository.save(new Resultat(request.joueurId(), request.partieId(),
                request.gagnee(), request.nombreEssais(), request.points()));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(resultat));
    }

    // list / search results by player and date range
    @GetMapping("/resultats")
    public List<ResultatResponse> lister(
            @RequestParam(required = false) Long joueurId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au) {
        Stream<Resultat> flux = repository.findAll().stream();
        if (joueurId != null) {
            flux = flux.filter(r -> joueurId.equals(r.getJoueurId()));
        }
        if (du != null) {
            flux = flux.filter(r -> !r.getDate().toLocalDate().isBefore(du));
        }
        if (au != null) {
            flux = flux.filter(r -> !r.getDate().toLocalDate().isAfter(au));
        }
        return flux.map(this::toResponse).toList();
    }

    // global ranking of players
    @GetMapping("/classement")
    public List<ClassementLigne> classement() {
        return classementService.classement();
    }

    private ResultatResponse toResponse(Resultat r) {
        return new ResultatResponse(r.getId(), r.getJoueurId(), r.getPartieId(),
                r.isGagnee(), r.getNombreEssais(), r.getPoints(), r.getDate());
    }
}

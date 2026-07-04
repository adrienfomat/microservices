package fr.dauphine.miage.motus.partie.controller;

import fr.dauphine.miage.motus.partie.dto.NouvellePartieRequest;
import fr.dauphine.miage.motus.partie.dto.NouvellePartieResponse;
import fr.dauphine.miage.motus.partie.dto.PartieResponse;
import fr.dauphine.miage.motus.partie.dto.PropositionRequest;
import fr.dauphine.miage.motus.partie.dto.PropositionResponse;
import fr.dauphine.miage.motus.partie.model.Statut;
import fr.dauphine.miage.motus.partie.service.GestionPartieService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/parties")
public class PartieController {

    private final GestionPartieService service;

    public PartieController(GestionPartieService service) {
        this.service = service;
    }

    // start a new game, the body carries the player id and the wanted word length
    @PostMapping
    public ResponseEntity<NouvellePartieResponse> demarrer(@RequestBody NouvellePartieRequest request) {
        NouvellePartieResponse response = service.demarrer(request.joueurId(), request.longueur());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // submit a word for a running game
    @PostMapping("/{id}/essais")
    public PropositionResponse proposer(@PathVariable Long id, @RequestBody PropositionRequest request) {
        return service.proposer(id, request.mot());
    }

    // full detail of a game including every guess
    @GetMapping("/{id}")
    public PartieResponse detail(@PathVariable Long id) {
        return service.detail(id);
    }

    // list / search games by player, status and start-date range (admin and history)
    @GetMapping
    public List<PartieResponse> lister(
            @RequestParam(required = false) Long joueurId,
            @RequestParam(required = false) Statut statut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au) {
        return service.lister(joueurId, statut, du, au);
    }
}

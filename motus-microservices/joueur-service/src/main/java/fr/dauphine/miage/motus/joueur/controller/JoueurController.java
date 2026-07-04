package fr.dauphine.miage.motus.joueur.controller;

import fr.dauphine.miage.motus.joueur.dto.ConnexionRequest;
import fr.dauphine.miage.motus.joueur.dto.InscriptionRequest;
import fr.dauphine.miage.motus.joueur.dto.JoueurResponse;
import fr.dauphine.miage.motus.joueur.model.Joueur;
import fr.dauphine.miage.motus.joueur.repository.JoueurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/joueurs")
public class JoueurController {

    // whoever registers with this code becomes an administrator
    private static final String CODE_ADMIN = "MIAGE-SITN";

    private final JoueurRepository repository;
    private final PasswordEncoder encoder;

    public JoueurController(JoueurRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    // create an account; the admin flag is granted only with the right code
    @PostMapping("/inscription")
    public ResponseEntity<JoueurResponse> inscription(@RequestBody InscriptionRequest request) {
        String pseudo = valeur(request.pseudo());
        String email = valeur(request.email()).toLowerCase();
        String motDePasse = request.motDePasse() == null ? "" : request.motDePasse();

        if (pseudo.isEmpty() || email.isEmpty() || motDePasse.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pseudo, email et mot de passe sont obligatoires");
        }
        if (repository.existsByPseudo(pseudo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce pseudo est deja pris");
        }
        if (repository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet email est deja utilise");
        }
        boolean admin = CODE_ADMIN.equals(valeur(request.codeAdmin()));
        Joueur joueur = repository.save(
                new Joueur(pseudo, email, encoder.encode(motDePasse), admin));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(joueur));
    }

    // log in with a pseudo OR an email plus the password
    @PostMapping("/connexion")
    public JoueurResponse connexion(@RequestBody ConnexionRequest request) {
        String identifiant = valeur(request.identifiant());
        Joueur joueur = repository.findByPseudo(identifiant)
                .or(() -> repository.findByEmail(identifiant.toLowerCase()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides"));

        if (request.motDePasse() == null || !encoder.matches(request.motDePasse(), joueur.getMotDePasseHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides");
        }
        return toResponse(joueur);
    }

    // used by the other microservices to resolve a player
    @GetMapping("/{id}")
    public JoueurResponse parId(@PathVariable Long id) {
        Joueur joueur = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Joueur introuvable"));
        return toResponse(joueur);
    }

    @GetMapping
    public List<JoueurResponse> lister() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    private String valeur(String s) {
        return s == null ? "" : s.trim();
    }

    private JoueurResponse toResponse(Joueur j) {
        return new JoueurResponse(j.getId(), j.getPseudo(), j.getEmail(), j.isAdmin(), j.getDateInscription());
    }
}

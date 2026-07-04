package fr.dauphine.miage.motus.joueur.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "joueur")
public class Joueur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String pseudo;

    @Column(nullable = false, unique = true)
    private String email;

    // BCrypt hash, never the clear password
    @Column(nullable = false)
    private String motDePasseHash;

    private boolean admin;

    private LocalDateTime dateInscription;

    protected Joueur() {
    }

    public Joueur(String pseudo, String email, String motDePasseHash, boolean admin) {
        this.pseudo = pseudo;
        this.email = email;
        this.motDePasseHash = motDePasseHash;
        this.admin = admin;
        this.dateInscription = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getPseudo() {
        return pseudo;
    }

    public String getEmail() {
        return email;
    }

    public String getMotDePasseHash() {
        return motDePasseHash;
    }

    public boolean isAdmin() {
        return admin;
    }

    public LocalDateTime getDateInscription() {
        return dateInscription;
    }
}

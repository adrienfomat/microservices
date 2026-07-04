package fr.dauphine.miage.motus.partie.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "partie")
public class Partie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long joueurId;

    private String motMystere;

    private int longueur;

    private int nombreEssaisMax;

    private int nombreEssaisUtilises;

    @Enumerated(EnumType.STRING)
    private Statut statut;

    private LocalDateTime dateDebut;

    private LocalDateTime dateFin;

    @OneToMany(mappedBy = "partie", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordre ASC")
    private List<Essai> essais = new ArrayList<>();

    protected Partie() {
    }

    public Partie(Long joueurId, String motMystere, int nombreEssaisMax) {
        this.joueurId = joueurId;
        this.motMystere = motMystere;
        this.longueur = motMystere.length();
        this.nombreEssaisMax = nombreEssaisMax;
        this.nombreEssaisUtilises = 0;
        this.statut = Statut.EN_COURS;
        this.dateDebut = LocalDateTime.now();
    }

    // attach a guess to this game and count it
    public void ajouterEssai(Essai essai) {
        essai.setPartie(this);
        this.essais.add(essai);
        this.nombreEssaisUtilises++;
    }

    // close the game with a final status
    public void terminer(Statut statutFinal) {
        this.statut = statutFinal;
        this.dateFin = LocalDateTime.now();
    }

    public int essaisRestants() {
        return nombreEssaisMax - nombreEssaisUtilises;
    }

    public Long getId() {
        return id;
    }

    public Long getJoueurId() {
        return joueurId;
    }

    public String getMotMystere() {
        return motMystere;
    }

    public int getLongueur() {
        return longueur;
    }

    public int getNombreEssaisMax() {
        return nombreEssaisMax;
    }

    public int getNombreEssaisUtilises() {
        return nombreEssaisUtilises;
    }

    public Statut getStatut() {
        return statut;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public List<Essai> getEssais() {
        return essais;
    }
}

package fr.dauphine.miage.motus.score.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "resultat")
public class Resultat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long joueurId;

    private Long partieId;

    private boolean gagnee;

    private int nombreEssais;

    private int points;

    private LocalDateTime date;

    protected Resultat() {
    }

    public Resultat(Long joueurId, Long partieId, boolean gagnee, int nombreEssais, int points) {
        this.joueurId = joueurId;
        this.partieId = partieId;
        this.gagnee = gagnee;
        this.nombreEssais = nombreEssais;
        this.points = points;
        this.date = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getJoueurId() {
        return joueurId;
    }

    public Long getPartieId() {
        return partieId;
    }

    public boolean isGagnee() {
        return gagnee;
    }

    public int getNombreEssais() {
        return nombreEssais;
    }

    public int getPoints() {
        return points;
    }

    public LocalDateTime getDate() {
        return date;
    }
}

package fr.dauphine.miage.motus.partie.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "essai")
public class Essai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "partie_id")
    private Partie partie;

    private int ordre;

    private String mot;

    // result pattern stored as a CSV of EtatLettre codes, one per letter
    @Column(length = 500)
    private String resultat;

    protected Essai() {
    }

    public Essai(int ordre, String mot, String resultat) {
        this.ordre = ordre;
        this.mot = mot;
        this.resultat = resultat;
    }

    public void setPartie(Partie partie) {
        this.partie = partie;
    }

    public Long getId() {
        return id;
    }

    public int getOrdre() {
        return ordre;
    }

    public String getMot() {
        return mot;
    }

    public String getResultat() {
        return resultat;
    }
}

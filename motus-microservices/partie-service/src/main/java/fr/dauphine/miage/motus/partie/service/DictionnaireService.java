package fr.dauphine.miage.motus.partie.service;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

@Service
public class DictionnaireService {

    public static final int LONGUEUR_MIN = 4;
    public static final int LONGUEUR_MAX = 9;

    // every accepted word (large, permissive): used to validate a player's guess
    private final Set<String> validation = new HashSet<>();
    // mystery words grouped by length: common nouns and adjectives only (clean)
    private final Map<Integer, List<String>> mysteres = new TreeMap<>();
    private final Random random = new Random();

    @PostConstruct
    public void charger() throws IOException {
        chargerLignes("dictionnaire.txt", mot -> validation.add(mot));
        chargerLignes("mots-mysteres.txt", mot ->
                mysteres.computeIfAbsent(mot.length(), k -> new ArrayList<>()).add(mot));
        if (validation.isEmpty() || mysteres.isEmpty()) {
            throw new IllegalStateException("Dictionnaire incomplet");
        }
    }

    // read a resource file line by line and hand each normalized word to the consumer
    private void chargerLignes(String fichier, java.util.function.Consumer<String> consumer) throws IOException {
        ClassPathResource resource = new ClassPathResource(fichier);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String ligne;
            while ((ligne = reader.readLine()) != null) {
                String mot = normaliser(ligne);
                if (!mot.isEmpty()) {
                    consumer.accept(mot);
                }
            }
        }
    }

    // upper-case and remove accents so "métier" and "METIER" match
    public String normaliser(String mot) {
        if (mot == null) {
            return "";
        }
        return Normalizer.normalize(mot.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase();
    }

    public boolean longueurDisponible(int longueur) {
        List<String> liste = mysteres.get(longueur);
        return liste != null && !liste.isEmpty();
    }

    // draw a random mystery word of the requested length
    public String motAleatoire(int longueur) {
        List<String> liste = mysteres.get(longueur);
        if (liste == null || liste.isEmpty()) {
            throw new IllegalArgumentException("Aucun mot de " + longueur + " lettres");
        }
        return liste.get(random.nextInt(liste.size()));
    }

    // does the word exist in the dictionary (permissive validation set)
    public boolean contient(String mot) {
        return validation.contains(normaliser(mot));
    }
}

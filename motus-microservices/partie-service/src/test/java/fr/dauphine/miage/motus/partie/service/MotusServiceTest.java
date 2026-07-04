package fr.dauphine.miage.motus.partie.service;

import fr.dauphine.miage.motus.partie.model.EtatLettre;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MotusServiceTest {

    private final MotusService service = new MotusService();

    @Test
    void toutesLesLettresBienPlacees() {
        List<EtatLettre> r = service.comparer("MAISON", "MAISON");
        assertEquals(List.of(EtatLettre.BIEN_PLACE, EtatLettre.BIEN_PLACE, EtatLettre.BIEN_PLACE,
                EtatLettre.BIEN_PLACE, EtatLettre.BIEN_PLACE, EtatLettre.BIEN_PLACE), r);
    }

    @Test
    void lettrePresenteMaisMalPlacee() {
        // 'O' and 'E' exist in ORANGE but not at the guessed positions
        List<EtatLettre> r = service.comparer("SOLEIL", "ORANGE");
        assertEquals(EtatLettre.ABSENT, r.get(0));
        assertEquals(EtatLettre.MAL_PLACE, r.get(1));
    }

    @Test
    void gestionDesLettresEnDoublon() {
        // BALLON has two L, guessing six L must mark only the two at the right place
        List<EtatLettre> r = service.comparer("LLLLLL", "BALLON");
        assertEquals(EtatLettre.ABSENT, r.get(0));
        assertEquals(EtatLettre.ABSENT, r.get(1));
        assertEquals(EtatLettre.BIEN_PLACE, r.get(2));
        assertEquals(EtatLettre.BIEN_PLACE, r.get(3));
        assertEquals(EtatLettre.ABSENT, r.get(4));
        assertEquals(EtatLettre.ABSENT, r.get(5));
    }
}

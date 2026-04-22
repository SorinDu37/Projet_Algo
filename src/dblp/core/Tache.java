package dblp.core;

import java.nio.file.*;
import java.util.*;

/**
 * Classe abstraite servant de base pour les deux tâches du projet.
 * Elle factorise le parsing en flux du fichier DBLP et le mapping auteur <-> id.
 * Chaque tâche concrète (Tache1, Tache2) n'a qu'à implémenter traiterAuteurs().
 */
public abstract class Tache {

    protected final Path xmlPath;
    protected final Path dtdPath;
    
    // Mapping bidirectionnel entre noms d'auteurs et identifiants numériques
    protected Map<String, Integer> nomVersId;
    protected List<String> idVersNom;
    
    protected static final int MAX_AUTEURS = 5_000_000;
    protected static final int MAX_QUEUE_SIZE = 256; // taille du buffer entre le parseur et le consommateur

    public Tache(String fichierXml, String fichierDtd) {
        this.xmlPath = Paths.get(fichierXml);
        this.dtdPath = Paths.get(fichierDtd);
        
        this.nomVersId = new HashMap<>();
        this.idVersNom = new ArrayList<>();
    }

    /**
     * Retourne l'id numérique d'un auteur. Si c'est un nouvel auteur,
     * on lui attribue le prochain id disponible et on l'enregistre.
     */
    protected int obtenirId(String nom) {
        if (!nomVersId.containsKey(nom)) {
            int nouvelId = nomVersId.size();
            nomVersId.put(nom, nouvelId);
            idVersNom.add(nom);
            return nouvelId;
        }
        return nomVersId.get(nom);
    }

    /**
     * Méthode abstraite : chaque tâche définit comment exploiter la liste d'auteurs
     * d'une publication (union pour Tache1, comptage de paires pour Tache2).
     */
    protected abstract void traiterAuteurs(List<String> auteurs);

    /**
     * Parcourt le fichier DBLP publication par publication (traitement en flux).
     * Pour chaque publication ayant >= 2 auteurs distincts, on délègue le
     * traitement à la sous-classe via traiterAuteurs().
     */
    protected void parcourirPublications() throws Exception {
        long nbPubs = 0;
        
        try (DblpPublicationGenerator gen = new DblpPublicationGenerator(xmlPath, dtdPath, MAX_QUEUE_SIZE)) {
            
            while (true) {
                Optional<DblpPublicationGenerator.Publication> pub = gen.nextPublication();
                if (pub.isEmpty()) {
                    break;
                }

                nbPubs++;
                List<String> auteurs = pub.get().authors;
                
                if (auteurs == null || auteurs.size() < 2) {
                    continue;
                }

                // LinkedHashSet supprime les doublons tout en conservant l'ordre d'apparition
                List<String> auteursUniques = new ArrayList<>(new LinkedHashSet<>(auteurs));
                if (auteursUniques.size() < 2) {
                    continue;
                }

                traiterAuteurs(auteursUniques);

                if (nbPubs % 100000 == 0) {
                    afficherProgression(nbPubs);
                }
            }
        }
        System.out.println("Lecture terminée : " + nbPubs + " publications scannées au total.");
    }

    /** Lance l'exécution complète de la tâche. */
    public abstract void executer() throws Exception;

    /** Affiche la progression pendant le parsing. Peut être redéfinie par les sous-classes. */
    protected void afficherProgression(long nbPubs) {
        System.out.println("  " + nbPubs + " publications traitées...");
    }
}
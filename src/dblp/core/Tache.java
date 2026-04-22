package dblp.core;

import java.nio.file.*;
import java.util.*;


public abstract class Tache {

    protected final Path xmlPath;
    protected final Path dtdPath;
    
    protected Map<String, Integer> nomVersId;
    protected List<String> idVersNom;
    
    protected static final int MAX_AUTEURS = 5_000_000;
    protected static final int MAX_QUEUE_SIZE = 256;

    public Tache(String fichierXml, String fichierDtd) {
        this.xmlPath = Paths.get(fichierXml);
        this.dtdPath = Paths.get(fichierDtd);
        
        this.nomVersId = new HashMap<>();
        this.idVersNom = new ArrayList<>();
    }

    protected int obtenirId(String nom) {
        if (!nomVersId.containsKey(nom)) {
            int nouvelId = nomVersId.size();
            nomVersId.put(nom, nouvelId);
            idVersNom.add(nom);
            return nouvelId;
        }
        return nomVersId.get(nom);
    }

    protected abstract void traiterAuteurs(List<String> auteurs);

    protected void parcourirPublications() throws Exception {
        long nbPubs = 0;
        
        try (DblpPublicationGenerator gen = new DblpPublicationGenerator(xmlPath, dtdPath, MAX_QUEUE_SIZE)) {
            
            while (true) {
                Optional<DblpPublicationGenerator.Publication> pub = gen.nextPublication();
                if (pub.isEmpty()) {
                    break; // Fin du fichier
                }

                nbPubs++;
                List<String> auteurs = pub.get().authors;
                
                if (auteurs == null || auteurs.size() < 2) {
                    continue;
                }

                List<String> auteursUniques = new ArrayList<>(new LinkedHashSet<>(auteurs));//Linked pour conserver l'ordre 
                if (auteursUniques.size() < 2) {
                    continue;
                }

                //Déléguer à la classe enfant
                traiterAuteurs(auteursUniques);

                if (nbPubs % 100000 == 0) {
                    afficherProgression(nbPubs);
                }
            }
        }
        System.out.println("Lecture terminée : " + nbPubs + " publications scannées au total.");
    }

    public abstract void executer() throws Exception;

    protected void afficherProgression(long nbPubs) {
        System.out.println("  " + nbPubs + " publications traitées...");
    }
}
package dblp.taches;

import dblp.core.Tache;
import dblp.structures.UF;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Tache1 extends Tache {

    private final UF unionFind;

    public Tache1(String fichierXml, String fichierDtd) {
        super(fichierXml, fichierDtd);
        this.unionFind = new UF(MAX_AUTEURS);
    }


    @Override
    protected void traiterAuteurs(List<String> auteurs) {
        int idPremier = obtenirId(auteurs.get(0)); 

        for (int i = 1; i < auteurs.size(); i++) {
            int idAutre = obtenirId(auteurs.get(i));
            unionFind.union(idPremier, idAutre); 
        }
    }

    private int getNombreVraiesCommunautes() {
        int auteursInutilises = MAX_AUTEURS - nomVersId.size(); //combien sont inutilisés
        return unionFind.count() - auteursInutilises;
    }

    @Override
    protected void afficherProgression(long nbPubs) {
        System.out.println("\n--- Après " + nbPubs + " publications ---");
        System.out.println("Nombre de communautés actives : " + getNombreVraiesCommunautes());

        List<Integer> tailles = new ArrayList<>();
        
        for (int id = 0; id < nomVersId.size(); id++) {
            if (unionFind.find(id) == id) { //c'est une racine
                tailles.add(unionFind.getSize(id)); // On prend sa taille
            }
        }

        tailles.sort(Collections.reverseOrder()); 

        System.out.print("Top 10 des tailles : ");
        int limit = Math.min(10, tailles.size()); //pour s'assurer si il y a moins de 10 communautés
        for (int i = 0; i < limit; i++) {
            System.out.print(tailles.get(i) + " ");
        }
        System.out.println("\n--------------------------------------\n");
    }

    @Override
    public void executer() throws Exception {
        System.out.println("========== TÂCHE 1 ==========");
        long debut = System.currentTimeMillis();

        System.out.println("Étape 1 : Regroupement des auteurs en flux (Union-Find)...");
        parcourirPublications(); 

        genererHistogramme("output/tache1_histogram.csv");

        long fin = System.currentTimeMillis();
        System.out.println("TÂCHE 1 TERMINÉE en " + (fin - debut) + " ms");
    }


    public void genererHistogramme(String nomFichierSortie) throws Exception {
        System.out.println("\nÉtape 2 : Génération de l'histogramme...");
        Files.createDirectories(Paths.get("output"));

        Map<Integer, Integer> histogramme = new TreeMap<>();
        Set<Integer> racinesUniques = new HashSet<>();
        
        for (int id = 0; id < nomVersId.size(); id++) {
            racinesUniques.add(unionFind.find(id));
        }

        for (int racine : racinesUniques) {
            int taille = unionFind.getSize(racine);
            histogramme.put(taille, histogramme.getOrDefault(taille, 0) + 1);
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(nomFichierSortie))) {
            writer.println("TailleCommunauté,NombreDeCommunautés");
            for (Map.Entry<Integer, Integer> entry : histogramme.entrySet()) {
                writer.println(entry.getKey() + "," + entry.getValue());
            }
        }
        System.out.println("  Histogramme sauvegardé dans : " + nomFichierSortie);
    } 
}
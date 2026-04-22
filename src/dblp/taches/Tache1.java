package dblp.taches;

import dblp.core.Tache;
import dblp.structures.UF;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Tâche 1 : Communautés de co-publication (graphe non orienté).
 * Utilise un Union-Find pour regrouper les auteurs en communautés
 * de manière online, publication par publication.
 */
public class Tache1 extends Tache {

    private final UF unionFind;

    public Tache1(String fichierXml, String fichierDtd) {
        super(fichierXml, fichierDtd);
        this.unionFind = new UF(MAX_AUTEURS);
    }

    /**
     * Pour chaque publication, on fusionne tous les co-auteurs
     * dans la même communauté via union(premier, autre).
     */
    @Override
    protected void traiterAuteurs(List<String> auteurs) {
        int idPremier = obtenirId(auteurs.get(0));

        for (int i = 1; i < auteurs.size(); i++) {
            int idAutre = obtenirId(auteurs.get(i));
            unionFind.union(idPremier, idAutre);
        }
    }

    /**
     * Le UF est initialisé avec MAX_AUTEURS slots, mais seuls nomVersId.size()
     * sont réellement utilisés. On soustrait les slots inutilisés du total.
     */
    private int getNombreVraiesCommunautes() {
        int auteursInutilises = MAX_AUTEURS - nomVersId.size();
        return unionFind.count() - auteursInutilises;
    }

    /**
     * Affichage intermédiaire toutes les 100 000 publications (exigence online).
     * Montre le nombre de communautés et les tailles des 10 plus grandes.
     */
    @Override
    protected void afficherProgression(long nbPubs) {
        System.out.println("\n--- Après " + nbPubs + " publications ---");
        System.out.println("Nombre de communautés actives : " + getNombreVraiesCommunautes());

        // Collecter les tailles des communautés en parcourant les racines
        List<Integer> tailles = new ArrayList<>();
        for (int id = 0; id < nomVersId.size(); id++) {
            if (unionFind.find(id) == id) {
                tailles.add(unionFind.getSize(id));
            }
        }

        tailles.sort(Collections.reverseOrder());

        System.out.print("Top 10 des tailles : ");
        int limit = Math.min(10, tailles.size());
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

        genererHistogramme("tache1_histogram");

        long fin = System.currentTimeMillis();
        System.out.println("TÂCHE 1 TERMINÉE en " + (fin - debut) + " ms");
    }

    /**
     * Génère un CSV contenant l'histogramme des tailles de communautés :
     * combien de communautés ont taille 1, taille 2, etc.
     */
    public void genererHistogramme(String nomFichierSortie) throws Exception {
        System.out.println("\nÉtape 2 : Génération de l'histogramme...");
        Files.createDirectories(Paths.get("output"));

        // Trouver toutes les racines uniques et compter par taille
        Map<Integer, Integer> histogramme = new TreeMap<>();
        Set<Integer> racinesUniques = new HashSet<>();

        for (int id = 0; id < nomVersId.size(); id++) {
            racinesUniques.add(unionFind.find(id));
        }

        for (int racine : racinesUniques) {
            int taille = unionFind.getSize(racine);
            histogramme.put(taille, histogramme.getOrDefault(taille, 0) + 1);
        }

        String horodatage = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String nomComplet = "output/" + nomFichierSortie + "_" + horodatage + ".csv";

        try (PrintWriter writer = new PrintWriter(new FileWriter(nomComplet))) {
            writer.println("Nombre_Total_Communautes");
            writer.println(getNombreVraiesCommunautes());
            writer.println("Nombre_de_Communautes,Taille_de_Communautes");
            for (Map.Entry<Integer, Integer> entry : histogramme.entrySet()) {
                writer.println(entry.getValue() + " , " + entry.getKey());
            }
        }
        System.out.println("  Histogramme sauvegardé dans : " + nomComplet);
    }
}
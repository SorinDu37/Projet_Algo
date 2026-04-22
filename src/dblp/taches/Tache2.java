package dblp.taches;

import dblp.core.Tache;
import dblp.structures.GrapheOriente;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Tâche 2 : Communautés orientées dans DBLP.
 * On compte les paires (premier auteur -> co-auteur) en flux,
 * puis on construit un graphe orienté filtré (>= 6 co-publications),
 * on cherche les CFC avec Tarjan, et on calcule les diamètres.
 */
public class Tache2 extends Tache {

    // Compteur de paires ordonnées. Clé : deux ids encodés dans un long, Valeur : nombre de co-publications
    private Map<Long, Integer> compteurPaires;
    private GrapheOriente graphe;

    public Tache2(String fichierXml, String fichierDtd) {
        super(fichierXml, fichierDtd);
        this.compteurPaires = new HashMap<>();
    }

    /**
     * Pour chaque publication, on incrémente le compteur (premier auteur -> chaque co-auteur).
     * Les deux ids sont encodés dans un seul long pour servir de clé dans le HashMap.
     */
    @Override
    protected void traiterAuteurs(List<String> auteurs) {
        int idPremier = obtenirId(auteurs.get(0));

        for (int i = 1; i < auteurs.size(); i++) {
            int idAutre = obtenirId(auteurs.get(i));
            long cle = ((long) idPremier << 32) | (idAutre & 0xFFFFFFFFL);
            compteurPaires.put(cle, compteurPaires.getOrDefault(cle, 0) + 1);
        }
    }

    @Override
    protected void afficherProgression(long nbPubs) {
        System.out.println("  " + nbPubs + " publications traitées... ("
            + nomVersId.size() + " auteurs, "
            + compteurPaires.size() + " paires uniques en RAM)");
    }

    @Override
    public void executer() throws Exception {
        System.out.println("========== TÂCHE 2 ==========");
        long debut = System.currentTimeMillis();

        System.out.println("\nÉtape 1 : Comptage des paires en flux...");
        parcourirPublications();
        System.out.println("  Bilan Étape 1 : " + compteurPaires.size() + " paires totales trouvées.");

        etape2_construireGraphe();
        List<List<Integer>> sccs = etape3_trouverSCC();
        etape4_ecrireResultats(sccs);

        long fin = System.currentTimeMillis();
        System.out.println("TÂCHE 2 TERMINÉE en " + (fin - debut) + " ms");
    }

    /** Construit le graphe orienté en ne gardant que les arêtes avec >= 6 co-publications. */
    private void etape2_construireGraphe() {
        System.out.println("\nÉtape 2 : Construction du graphe filtré (poids >= 6)...");
        graphe = new GrapheOriente();

        for (Map.Entry<Long, Integer> entry : compteurPaires.entrySet()) {
            if (entry.getValue() >= 6) {
                long cle = entry.getKey();
                int src = (int) (cle >> 32);   // décoder l'id source
                int dst = (int) cle;            // décoder l'id destination
                graphe.ajouterArete(src, dst);
            }
        }

        compteurPaires = null; // libérer la mémoire

        System.out.println("  Graphe construit : " + graphe.getSommets().size() + " sommets conservés.");
    }

    /** Trouve les composantes fortement connexes via Tarjan. */
    private List<List<Integer>> etape3_trouverSCC() {
        System.out.println("\nÉtape 3 : Recherche des CFC (Algorithme de Tarjan)...");
        List<List<Integer>> sccs = graphe.obtenirCFC();
        System.out.println("  " + sccs.size() + " communautés fortement connexes (CFC) trouvées.");
        return sccs;
    }

    /** Écrit l'histogramme des tailles et le top 10 avec diamètres et noms des membres. */
    private void etape4_ecrireResultats(List<List<Integer>> sccs) throws IOException {
        System.out.println("\nÉtape 4 : Calcul des diamètres et écriture des résultats...");
        Files.createDirectories(Paths.get("output"));

        String horodatage = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String nomHisto = "output/tache2_histogram_" + horodatage + ".csv";
        String nomTop10 = "output/tache2_top10_" + horodatage + ".txt";

        // Histogramme : taille -> nombre de CFC de cette taille
        Map<Integer, Integer> histogramme = new TreeMap<>();
        for (List<Integer> scc : sccs) {
            int t = scc.size();
            histogramme.put(t, histogramme.getOrDefault(t, 0) + 1);
        }

        try (PrintWriter pw1 = new PrintWriter(new FileWriter(nomHisto))) {
            pw1.println("taille_communaute,nombre_communautes");
            for (Map.Entry<Integer, Integer> entry : histogramme.entrySet()) {
                pw1.println(entry.getKey() + "," + entry.getValue());
            }
        }
        System.out.println("  Histogramme -> " + nomHisto);

        // Top 10 : taille, diamètre et liste des membres pour chaque CFC
        int nbTop = Math.min(10, sccs.size());
        try (PrintWriter pw2 = new PrintWriter(new FileWriter(nomTop10))) {
            for (int i = 0; i < nbTop; i++) {
                List<Integer> scc = sccs.get(i);
                System.out.print("  Calcul du diamètre pour la CFC " + (i + 1) + " (taille " + scc.size() + ")... ");

                int diametre = graphe.calculerDiametre(scc);
                System.out.println("-> Diamètre = " + diametre);

                pw2.println("=== Communauté " + (i + 1) + " ===");
                pw2.println("Taille : " + scc.size());
                pw2.println("Diamètre : " + diametre);
                pw2.println("Membres :");
                for (int id : scc) {
                    pw2.println("  - " + idVersNom.get(id));
                }
                pw2.println();
            }
        }
        System.out.println("  Top 10 -> " + nomTop10);
    }
}
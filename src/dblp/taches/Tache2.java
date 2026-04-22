package dblp.taches;

import dblp.core.Tache;
import dblp.structures.GrapheOriente;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * TÂCHE 2 : Communautés orientées dans DBLP
 * Utilise un graphe orienté et l'algorithme de Tarjan pour trouver les CFC.
 */
public class Tache2 extends Tache {

    // Clé: (idSource << 32) | idDest -> Valeur: nombre de co-publications
    private Map<Long, Integer> compteurPaires; 
    private GrapheOriente graphe;

    public Tache2(String fichierXml, String fichierDtd) {
        super(fichierXml, fichierDtd);
        this.compteurPaires = new HashMap<>();
    }

    @Override
    protected void traiterAuteurs(List<String> auteurs) {
        // Le premier auteur est la source (A -> B, A -> C)
        int idPremier = obtenirId(auteurs.get(0));

        for (int i = 1; i < auteurs.size(); i++) {
            int idAutre = obtenirId(auteurs.get(i));
            
            //Encoder les deux 'int' dans un seul 'long'
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
        parcourirPublications(); // Déclenche traiterAuteurs() et afficherProgression() en boucle
        
        System.out.println("  Bilan Étape 1 : " + compteurPaires.size() + " paires totales trouvées.");

        etape2_construireGraphe();
        
        List<List<Integer>> sccs = etape3_trouverSCC();
        
        etape4_ecrireResultats(sccs);

        long fin = System.currentTimeMillis();
        System.out.println("TÂCHE 2 TERMINÉE en " + (fin - debut) + " ms");
    }

    private void etape2_construireGraphe() {
        System.out.println("\nÉtape 2 : Construction du graphe filtré (poids >= 6)...");
        graphe = new GrapheOriente();

        for (Map.Entry<Long, Integer> entry : compteurPaires.entrySet()) {
            if (entry.getValue() >= 6) {
                long cle = entry.getKey();
                
                // Opération inverse : Décoder le long en deux int
                int src = (int) (cle >> 32);
                int dst = (int) cle;

                graphe.ajouterArete(src, dst);
            }
        }

        //Libérer la mémoire occupée par les millions de paires
        compteurPaires = null; 
        
        System.out.println("  Graphe construit : " + graphe.getSommets().size() + " sommets conservés.");
    }

    private List<List<Integer>> etape3_trouverSCC() {
        System.out.println("\nÉtape 3 : Recherche des CFC (Algorithme de Tarjan)...");
        
        // Tout le travail complexe est encapsulé dans GrapheOriente !
        List<List<Integer>> sccs = graphe.obtenirCFC();
        
        System.out.println("  " + sccs.size() + " communautés fortement connexes (CFC) trouvées.");
        return sccs;
    }

    private void etape4_ecrireResultats(List<List<Integer>> sccs) throws IOException {
        System.out.println("\nÉtape 4 : Calcul des diamètres et écriture des résultats...");
        Files.createDirectories(Paths.get("output"));

        String horodatage = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    
        String nomHisto = "output/tache2_histogram_" + horodatage + ".csv";
        String nomTop10 = "output/tache2_top10_" + horodatage + ".txt";
        
        //histogramme
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
        System.out.println("  Histogramme ->" + nomHisto);

        //Top 10 des plus grandes communautés
        int nbTop = Math.min(10, sccs.size());
        try (PrintWriter pw2 = new PrintWriter(new FileWriter(nomTop10))) {
            for (int i = 0; i < nbTop; i++) {
                List<Integer> scc = sccs.get(i);
                System.out.print("  Calcul du diamètre pour la CFC " + (i + 1) + " (taille " + scc.size() + ")... ");
                
                // Le calcul du BFS est lui aussi encapsulé proprement
                int diametre = graphe.calculerDiametre(scc);
                System.out.println("-> Diamètre = " + diametre);

                pw2.println("=== Communauté " + (i + 1) + " ===");
                pw2.println("Taille : " + scc.size());
                pw2.println("Diamètre : " + diametre);
                pw2.println("Membres :");
                
                // Utilisation de la liste idVersNom de la superclasse pour retrouver les textes
                for (int id : scc) {
                    pw2.println("  - " + idVersNom.get(id)); 
                }
                pw2.println();
            }
        }
        System.out.println("  Top 10 ->" + nomTop10);
    }
}
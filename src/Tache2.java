import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * TÂCHE 2 : Communautés orientées dans DBLP
 * 
 * 4 étapes :
 *   1) Compter les paires (premier auteur -> co-auteur) en flux
 *   2) Garder seulement les liens avec compteur >= 6
 *   3) Trouver les composantes fortement connexes (Tarjan)
 *   4) Calculer le diamètre des 10 plus grandes + écrire les résultats
 */
public class Tache2 {

    private Map<String, Integer> nomVersId;
    private List<String> idVersNom;
    private int prochainId;

    private Map<String, Integer> compteurPaires; // clé "idA,idB" -> nombre de co-publications
    private Map<Integer, List<Integer>> graphe;  // graphe orienté filtré

    private Path xmlPath;
    private Path dtdPath;

    public Tache2(String fichierXml, String fichierDtd) {
        this.xmlPath = Paths.get(fichierXml);
        this.dtdPath = Paths.get(fichierDtd);
        this.nomVersId = new HashMap<>();
        this.idVersNom = new ArrayList<>();
        this.prochainId = 0;
        this.compteurPaires = new HashMap<>();
    }

    private int obtenirId(String nom) {
        if (nomVersId.containsKey(nom)) {
            return nomVersId.get(nom);
        }
        int id = prochainId;
        prochainId++;
        nomVersId.put(nom, id);
        idVersNom.add(nom);
        return id;
    }

    // ================================================================
    // ÉTAPE 1 : Compter les paires (A -> B) en flux
    // ================================================================
    private void etape1_compterPaires() throws Exception {
        System.out.println("Étape 1 : Comptage des paires en flux...");
        long nbPubs = 0;

        DblpPublicationGenerator gen = new DblpPublicationGenerator(xmlPath, dtdPath, 256);

        while (true) {
            Optional<DblpPublicationGenerator.Publication> pub = gen.nextPublication();
            if (pub.isEmpty()) break;

            nbPubs++;
            List<String> auteurs = pub.get().authors;

            if (auteurs == null || auteurs.size() < 2) continue;

            // Enlever les doublons éventuels
            List<String> auteursUniques = new ArrayList<>(new LinkedHashSet<>(auteurs));
            if (auteursUniques.size() < 2) continue;

            int idPremier = obtenirId(auteursUniques.get(0));

            for (int i = 1; i < auteursUniques.size(); i++) {
                int idAutre = obtenirId(auteursUniques.get(i));
                String cle = idPremier + "," + idAutre;
                int ancien = compteurPaires.getOrDefault(cle, 0);
                compteurPaires.put(cle, ancien + 1);
            }

            if (nbPubs % 100000 == 0) {
                System.out.println("  " + nbPubs + " publications lues...");
            }
        }
        gen.close();

        System.out.println("  Terminé : " + nbPubs + " publications, "
                + prochainId + " auteurs, " + compteurPaires.size() + " paires");
    }

    // ================================================================
    // ÉTAPE 2 : Construire le graphe filtré (seuil >= 6)
    // ================================================================
    private void etape2_construireGraphe() {
        System.out.println("Étape 2 : Construction du graphe filtré...");
        graphe = new HashMap<>();

        for (Map.Entry<String, Integer> entry : compteurPaires.entrySet()) {
            if (entry.getValue() >= 6) {
                String[] parts = entry.getKey().split(",");
                int src = Integer.parseInt(parts[0]);
                int dst = Integer.parseInt(parts[1]);

                if (!graphe.containsKey(src)) {
                    graphe.put(src, new ArrayList<>());
                }
                graphe.get(src).add(dst);
            }
        }

        compteurPaires = null; // libérer la mémoire

        Set<Integer> sommets = new HashSet<>(graphe.keySet());
        int nbAretes = 0;
        for (List<Integer> voisins : graphe.values()) {
            nbAretes += voisins.size();
            sommets.addAll(voisins);
        }
        System.out.println("  " + sommets.size() + " sommets, " + nbAretes + " arêtes");
    }

    // ================================================================
    // ÉTAPE 3 : SCC avec Tarjan (version itérative)
    // ================================================================
    private int compteurTarjan;
    private int[] indice;
    private int[] lienBas;
    private boolean[] dansLaPile;
    private Deque<Integer> pileTarjan;
    private List<List<Integer>> toutesLesSCC;

    private List<List<Integer>> etape3_trouverSCC() {
        System.out.println("Étape 3 : Recherche des SCC (Tarjan)...");

        Set<Integer> sommets = new HashSet<>(graphe.keySet());
        for (List<Integer> voisins : graphe.values()) {
            sommets.addAll(voisins);
        }

        int maxId = 0;
        for (int v : sommets) {
            if (v > maxId) maxId = v;
        }
        maxId++;

        compteurTarjan = 0;
        indice = new int[maxId];
        lienBas = new int[maxId];
        dansLaPile = new boolean[maxId];
        Arrays.fill(indice, -1);
        pileTarjan = new ArrayDeque<>();
        toutesLesSCC = new ArrayList<>();

        for (int v : sommets) {
            if (indice[v] == -1) {
                tarjan(v);
            }
        }

        // Trier par taille décroissante
        toutesLesSCC.sort((a, b) -> b.size() - a.size());

        System.out.println("  " + toutesLesSCC.size() + " SCC trouvées");
        return toutesLesSCC;
    }

    /**
     * Tarjan itératif : on simule la récursion avec une pile manuelle
     * pour éviter les StackOverflow sur de grands graphes.
     */
    private void tarjan(int depart) {
        // Pile d'appels simulés : [sommet, indexVoisinActuel]
        Deque<int[]> appels = new ArrayDeque<>();

        indice[depart] = compteurTarjan;
        lienBas[depart] = compteurTarjan;
        compteurTarjan++;
        dansLaPile[depart] = true;
        pileTarjan.push(depart);
        appels.push(new int[]{depart, 0});

        while (!appels.isEmpty()) {
            int[] courant = appels.peek();
            int v = courant[0];
            List<Integer> voisins = graphe.getOrDefault(v, Collections.emptyList());

            if (courant[1] < voisins.size()) {
                int w = voisins.get(courant[1]);
                courant[1]++;

                if (indice[w] == -1) {
                    // w pas visité : on descend
                    indice[w] = compteurTarjan;
                    lienBas[w] = compteurTarjan;
                    compteurTarjan++;
                    dansLaPile[w] = true;
                    pileTarjan.push(w);
                    appels.push(new int[]{w, 0});
                } else if (dansLaPile[w]) {
                    lienBas[v] = Math.min(lienBas[v], indice[w]);
                }
            } else {
                // Tous les voisins traités : vérifier si v est racine d'une SCC
                if (lienBas[v] == indice[v]) {
                    List<Integer> scc = new ArrayList<>();
                    int w;
                    do {
                        w = pileTarjan.pop();
                        dansLaPile[w] = false;
                        scc.add(w);
                    } while (w != v);
                    toutesLesSCC.add(scc);
                }

                // Remonter vers le parent
                appels.pop();
                if (!appels.isEmpty()) {
                    int[] parent = appels.peek();
                    lienBas[parent[0]] = Math.min(lienBas[parent[0]], lienBas[v]);
                }
            }
        }
    }

    // ================================================================
    // ÉTAPE 4 : Diamètres + écriture des résultats
    // ================================================================

    /**
     * Diamètre = plus grande distance entre 2 sommets dans le sous-graphe induit.
     * On fait un BFS depuis chaque sommet (ou 500 max pour les grandes SCC).
     */
    private int calculerDiametre(List<Integer> communaute) {
        if (communaute.size() <= 1) return 0;

        Set<Integer> membres = new HashSet<>(communaute);

        // Sous-graphe : garder seulement les arêtes entre membres de la SCC
        Map<Integer, List<Integer>> sousGraphe = new HashMap<>();
        for (int v : communaute) {
            List<Integer> voisins = graphe.getOrDefault(v, Collections.emptyList());
            List<Integer> voisinsDedans = new ArrayList<>();
            for (int w : voisins) {
                if (membres.contains(w)) {
                    voisinsDedans.add(w);
                }
            }
            sousGraphe.put(v, voisinsDedans);
        }

        // Échantillonner si trop grand
        List<Integer> sources;
        if (communaute.size() > 500) {
            sources = new ArrayList<>(communaute);
            Collections.shuffle(sources, new Random(42));
            sources = sources.subList(0, 500);
        } else {
            sources = communaute;
        }

        int diametre = 0;
        for (int source : sources) {
            int distMax = bfs(source, sousGraphe, membres);
            if (distMax > diametre) diametre = distMax;
        }
        return diametre;
    }

    /** BFS depuis un sommet, retourne la distance max atteinte. */
    private int bfs(int source, Map<Integer, List<Integer>> sousGraphe, Set<Integer> membres) {
        Map<Integer, Integer> distances = new HashMap<>();
        distances.put(source, 0);
        Queue<Integer> file = new ArrayDeque<>();
        file.add(source);
        int distMax = 0;

        while (!file.isEmpty()) {
            int u = file.poll();
            int d = distances.get(u);
            for (int v : sousGraphe.getOrDefault(u, Collections.emptyList())) {
                if (!distances.containsKey(v)) {
                    distances.put(v, d + 1);
                    if (d + 1 > distMax) distMax = d + 1;
                    file.add(v);
                }
            }
        }
        return distMax;
    }

    /** Écrit l'histogramme CSV et le fichier top 10 avec taille, diamètre et membres. */
    private void etape4_ecrireResultats(List<List<Integer>> sccs) throws IOException {
        System.out.println("Étape 4 : Écriture des résultats...");
        Files.createDirectories(Paths.get("output"));

        // Histogramme des tailles
        Map<Integer, Integer> histogramme = new TreeMap<>();
        for (List<Integer> scc : sccs) {
            int t = scc.size();
            histogramme.put(t, histogramme.getOrDefault(t, 0) + 1);
        }

        PrintWriter pw1 = new PrintWriter(new FileWriter("output/task2_histogram.csv"));
        pw1.println("taille_communaute,nombre_communautes");
        for (Map.Entry<Integer, Integer> entry : histogramme.entrySet()) {
            pw1.println(entry.getKey() + "," + entry.getValue());
        }
        pw1.close();
        System.out.println("  Histogramme -> output/task2_histogram.csv");

        // Top 10
        int nbTop = Math.min(10, sccs.size());
        PrintWriter pw2 = new PrintWriter(new FileWriter("output/task2_top10.txt"));

        for (int i = 0; i < nbTop; i++) {
            List<Integer> scc = sccs.get(i);
            System.out.println("  Diamètre communauté " + (i + 1) + " (taille " + scc.size() + ")...");
            int diametre = calculerDiametre(scc);
            System.out.println("    -> diamètre = " + diametre);

            pw2.println("=== Communauté " + (i + 1) + " ===");
            pw2.println("Taille : " + scc.size());
            pw2.println("Diamètre : " + diametre);
            pw2.println("Membres :");
            for (int id : scc) {
                pw2.println("  - " + idVersNom.get(id));
            }
            pw2.println();
        }
        pw2.close();
        System.out.println("  Top 10 -> output/task2_top10.txt");
    }

    // ================================================================
    // Lancer toute la tâche 2
    // ================================================================
    public void executer() throws Exception {
        System.out.println("========== TÂCHE 2 ==========");
        long debut = System.currentTimeMillis();

        etape1_compterPaires();
        etape2_construireGraphe();
        List<List<Integer>> sccs = etape3_trouverSCC();
        etape4_ecrireResultats(sccs);

        long fin = System.currentTimeMillis();
        System.out.println("TÂCHE 2 TERMINÉE en " + (fin - debut) + " ms");
    }
}
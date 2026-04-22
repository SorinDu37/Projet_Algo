package dblp.structures;

import java.util.*;

/**
 * Graphe orienté représenté par liste d'adjacence.
 * Utilisé dans la Tâche 2 pour stocker les relations filtrées (poids >= 6)
 * et calculer les CFC via Tarjan ainsi que les diamètres via BFS.
 */
public class GrapheOriente {
    private final Map<Integer, List<Integer>> adjacence;

    public GrapheOriente() {
        this.adjacence = new HashMap<>();
    }

    /** Ajoute une arête orientée u -> v dans le graphe. */
    public void ajouterArete(int u, int v) {
        List<Integer> voisins = adjacence.get(u);

        if (voisins == null) {
            voisins = new ArrayList<>();
            adjacence.put(u, voisins);
        }

        voisins.add(v);
    }

    /** Retourne les voisins sortants de u (liste vide si aucun). */
    public List<Integer> getVoisins(int u) {
        List<Integer> voisins = adjacence.get(u);
        if (voisins == null) {
            return Collections.emptyList();
        }
        return voisins;
    }

    /** Retourne l'ensemble de tous les sommets (sources et destinations). */
    public Set<Integer> getSommets() {
        Set<Integer> sommets = new HashSet<>(adjacence.keySet());
        for (List<Integer> voisins : adjacence.values()) {
            sommets.addAll(voisins);
        }
        return sommets;
    }

    /** Trouve toutes les composantes fortement connexes via l'algorithme de Tarjan. */
    public List<List<Integer>> obtenirCFC() {
        Tarjan tarjan = new Tarjan(adjacence);
        return tarjan.trouverCFC();
    }

    /**
     * Calcule le diamètre d'une CFC dans le sous-graphe induit par ses membres.
     * On lance un BFS depuis chaque membre et on garde la distance max.
     * Pour les CFC > 500 membres, on échantillonne 500 sources pour rester raisonnable.
     */
    public int calculerDiametre(List<Integer> membresCFC) {
        if (membresCFC.size() <= 1) return 0;
        
        Set<Integer> setMembres = new HashSet<>(membresCFC);
        int diametreMax = 0;

        // Échantillonnage si la communauté est trop grande
        List<Integer> sources = new ArrayList<>(membresCFC);
        if (sources.size() > 500) {
            Collections.shuffle(sources, new Random(42));
            sources = sources.subList(0, 500);
        }

        for (int source : sources) {
            diametreMax = Math.max(diametreMax, bfsDistanceMax(source, setMembres));
        }
        return diametreMax;
    }

    /**
     * BFS depuis un sommet source, restreint aux sommets de l'ensemble 'membres'.
     * Retourne la plus grande distance atteinte (= excentricité du sommet).
     */
    private int bfsDistanceMax(int source, Set<Integer> membres) {
        Queue<Integer> file = new ArrayDeque<>();
        Map<Integer, Integer> distances = new HashMap<>();

        file.add(source);
        distances.put(source, 0);
        int dMax = 0;

        while (!file.isEmpty()) {
            int u = file.poll();
            int d = distances.get(u);
            dMax = Math.max(dMax, d);

            for (int v : getVoisins(u)) {
                if (membres.contains(v) && !distances.containsKey(v)) {
                    distances.put(v, d + 1);
                    file.add(v);
                }
            }
        }
        return dMax;
    }
}
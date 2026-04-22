package dblp.structures;

import java.util.*;

/**
 * Implémentation de l'algorithme de Tarjan pour trouver les 
 * Composantes Fortement Connexes (CFC) d'un graphe orienté.
 * 
 * Version ITÉRATIVE pour éviter les StackOverflow sur de grands graphes.
 */
public class Tarjan {

    private int index;
    private final Map<Integer, List<Integer>> adjacence;
    private final Map<Integer, Integer> vIndex;
    private final Map<Integer, Integer> vLowlink;
    private final Deque<Integer> stack;
    private final Set<Integer> onStack;
    private final List<List<Integer>> sccs;

    public Tarjan(Map<Integer, List<Integer>> adjacence) {
        this.adjacence = adjacence;
        this.vIndex = new HashMap<>();
        this.vLowlink = new HashMap<>();
        this.stack = new ArrayDeque<>();
        this.onStack = new HashSet<>();
        this.sccs = new ArrayList<>();
        this.index = 0;
    }

    /**
     * Calcule et retourne la liste des CFC, triées par taille décroissante.
     */
    public List<List<Integer>> trouverCFC() {
        for (Integer v : adjacence.keySet()) {
            if (!vIndex.containsKey(v)) {
                strongConnect(v);
            }
        }
        sccs.sort((a, b) -> Integer.compare(b.size(), a.size()));
        return sccs;
    }

    /**
     * Version itérative de strongConnect.
     * On simule la récursion avec une pile d'appels manuelle.
     * Chaque "frame" = {sommet, index du prochain voisin à traiter}.
     */
    private void strongConnect(int depart) {
        Deque<int[]> appels = new ArrayDeque<>();

        // Initialiser le sommet de départ
        vIndex.put(depart, index);
        vLowlink.put(depart, index);
        index++;
        stack.push(depart);
        onStack.add(depart);
        appels.push(new int[]{depart, 0});

        while (!appels.isEmpty()) {
            int[] frame = appels.peek();
            int v = frame[0];
            List<Integer> voisins = adjacence.getOrDefault(v, Collections.emptyList());

            if (frame[1] < voisins.size()) {
                // Il reste des voisins à explorer
                int w = voisins.get(frame[1]);
                frame[1]++;

                if (!vIndex.containsKey(w)) {
                    // w pas encore visité : on "descend" dedans
                    vIndex.put(w, index);
                    vLowlink.put(w, index);
                    index++;
                    stack.push(w);
                    onStack.add(w);
                    appels.push(new int[]{w, 0});
                } else if (onStack.contains(w)) {
                    // w est sur la pile : cycle détecté
                    vLowlink.put(v, Math.min(vLowlink.get(v), vIndex.get(w)));
                }
            } else {
                // Tous les voisins de v sont traités

                // Si v est racine d'une CFC, on dépile
                if (vLowlink.get(v).equals(vIndex.get(v))) {
                    List<Integer> scc = new ArrayList<>();
                    int w;
                    do {
                        w = stack.pop();
                        onStack.remove(w);
                        scc.add(w);
                    } while (w != v);
                    sccs.add(scc);
                }

                // Remonter : mettre à jour le lowlink du parent
                appels.pop();
                if (!appels.isEmpty()) {
                    int parent = appels.peek()[0];
                    vLowlink.put(parent, Math.min(vLowlink.get(parent), vLowlink.get(v)));
                }
            }
        }
    }
}
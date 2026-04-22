package dblp.structures;

import java.util.*;

/**
 * Implémentation de l'algorithme de Tarjan pour trouver les 
 * Composantes Fortement Connexes (CFC) d'un graphe orienté.
 */
public class Tarjan {

    private int index; // Compteur pour l'ordre de visite
    private final Map<Integer, List<Integer>> adjacence;
    private final Map<Integer, Integer> vIndex; // Indice de visite du sommet
    private final Map<Integer, Integer> vLowlink; // Plus petit indice accessible (racine de la CFC)
    private final Stack<Integer> stack; // Pile des sommets explorés
    private final Set<Integer> onStack; // Pour vérifier rapidement si un sommet est sur la pile
    private final List<List<Integer>> sccs; // Liste finale des CFC trouvées

    public Tarjan(Map<Integer, List<Integer>> adjacence) {
        this.adjacence = adjacence;
        this.vIndex = new HashMap<>();
        this.vLowlink = new HashMap<>();
        this.stack = new Stack<>();
        this.onStack = new HashSet<>();
        this.sccs = new ArrayList<>();
        this.index = 0;
    }

    /**
     * Calcule et retourne la liste des CFC.
     */
    public List<List<Integer>> trouverCFC() {
        // On lance le parcours pour chaque sommet non encore visité
        for (Integer v : adjacence.keySet()) {
            if (!vIndex.containsKey(v)) {
                strongConnect(v);
            }
        }
        
        // Optionnel : Trier les CFC par taille décroissante (utile pour le Top 10)
        sccs.sort((a, b) -> Integer.compare(b.size(), a.size()));
        
        return sccs;
    }

    /**
     * Fonction récursive de parcours en profondeur (DFS)
     */
    private void strongConnect(Integer v) {
        // Initialiser l'indice de visite et le lowlink
        vIndex.put(v, index);
        vLowlink.put(v, index);
        index++;
        
        stack.push(v);
        onStack.add(v);

        // Parcourir les voisins
        List<Integer> voisins = adjacence.getOrDefault(v, Collections.emptyList());
        for (Integer w : voisins) {
            if (!vIndex.containsKey(w)) {
                // Successeur non encore visité : récursion
                strongConnect(w);
                vLowlink.put(v, Math.min(vLowlink.get(v), vLowlink.get(w)));
            } 
            else if (onStack.contains(w)) {
                // Successeur est dans la pile (fait partie de la CFC actuelle)
                vLowlink.put(v, Math.min(vLowlink.get(v), vIndex.get(w)));
            }
        }

        // Si v est un sommet racine, on dépile tous les sommets de sa CFC
        if (vLowlink.get(v).equals(vIndex.get(v))) {
            List<Integer> scc = new ArrayList<>();
            Integer w;
            do {
                w = stack.pop();
                onStack.remove(w);
                scc.add(w);
            } while (!v.equals(w));
            
            sccs.add(scc);
        }
    }
}
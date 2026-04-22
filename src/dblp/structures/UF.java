package dblp.structures;

/**
 * Structure Union-Find (Disjoint Set Union) avec compression de chemins
 * et union pondérée par taille. Utilisée dans la Tâche 1 pour maintenir
 * les communautés de co-publication de manière online.
 */
public class UF {

    private int[] parent; // parent[i] = parent du sommet i dans l'arbre
    private int[] size;   // size[i] = taille de la communauté si i est racine
    private int count;    // nombre total de communautés distinctes

    /**
     * Initialise n éléments, chacun dans sa propre communauté.
     */
    public UF(int n) {
        count = n;
        parent = new int[n];
        size = new int[n];

        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }
    }

    /**
     * Fusionne les communautés de p et q (union pondérée : la plus petite
     * est rattachée à la plus grande pour garder les arbres équilibrés).
     * @return true si une fusion a eu lieu, false si p et q étaient déjà ensemble
     */
    public boolean union(int p, int q) {
        int i = find(p);
        int j = find(q);
        if (i == j) {
            return false;
        }

        if (size[i] < size[j]) {
            parent[i] = j;
            size[j] += size[i];
        } else {
            parent[j] = i;
            size[i] += size[j];
        }
        count--;
        return true;
    }

    /**
     * Trouve la racine de la communauté de p, avec compression de chemin :
     * tous les nœuds traversés sont directement rattachés à la racine.
     */
    public int find(int p) {
        int root = p;
        while (root != parent[root]) {
            root = parent[root];
        }
        // Compression : rattacher chaque nœud du chemin directement à la racine
        while (p != root) {
            int next = parent[p];
            parent[p] = root;
            p = next;
        }
        return root;
    }

    /** Vérifie si p et q sont dans la même communauté. */
    public boolean connected(int p, int q) {
        return find(p) == find(q);
    }

    /** Retourne la taille de la communauté dont root est la racine. */
    public int getSize(int root) {
        return size[root];
    }

    /** Retourne le nombre total de communautés distinctes. */
    public int count() {
        return count;
    }
}
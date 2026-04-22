package dblp.structures;

import java.util.HashSet;
import java.util.Set;

public class UF {
/**
 * Structure Union-Find comme vue au début du cours d'Algorithmique 2. 
 * Celle-ci utilisera la compression de chemins et l'union rapide pondérée afin d'optimiser au mieux les opérations.
 * Une classe d'équivalence est ici commentée comme "communauté".
 */
    private int[] parent; //chaque index pointera vers son parent
    private int[] size; //stocke la taille des communautés
    private int count;  //Nombre total de communautés

    public UF (int n) {
        count = n; 
        parent = new int[n]; 
        size = new int[n];

        for (int i = 0; i < n; i++) {
            parent[i] = i; //chacun est son propre parent
            size[i] = 1; //chaque communauté est de taille 1
        }
    }
    /**
     * Unit la communauté de p et de q, en choississant la plus petite pour la rattacher à la plus grande.
     * @param p : élément de p
     * @param q : élément de q
     */
    public boolean union(int p, int q) {
        int i = find(p); //communauté de p
        int j = find(q); //communauté de q
        if (i == j){ //si appartiennent déjà à la même
            return false;
        }
        
        //faire pointer la communauté la plus petite à la plus grande
        if (size[i] < size[j]) {
            parent[i] = j; 
            size[j] += size[i]; //incrémenter la taille de la communauté
        }
        else {
            parent[j] =i;
            size[i] += size[j];
        }
        count--; //nombre de communautés baisse du coup
        return true;
    }

    /**
     * Retrouve la communauté de p
     * @param p : élément p
     * @return : la communauté correspondant à p
     */
    public int find(int p) {
        int root = p;
        while (root != parent[root]) {
            root = parent[root]; //on remonte
        } //ici donc root contient le parent
        while (p != root) { //processus de compression
            int newp = parent[p]; //on récupère le parent de p actuel
            parent[p] = root; // on dit que root devient le parent de p
            p = newp; //on remonte
        }
        return root;
    }

    /**
     * Teste si p appartient à la même classe d'équivalence (communauté) que q.
     * @param p : Premier élément
     * @param q : Deuxième élément
     * @return : true si appartient à la même classe d'équivalence, false sinon.
     */
    public boolean connected (int p, int q) {
        return find(p) == find(q);
    }

    public int getSize(int root) {
        return size[root];
    }

    public int count() {
        return count;
    }
}

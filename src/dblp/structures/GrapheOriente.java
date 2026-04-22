package dblp.structures;
import java.util.*;

public class GrapheOriente {
    private final Map<Integer, List<Integer>> adjacence;
    private int MaxId = -1;

    public GrapheOriente() {
        this.adjacence = new HashMap<>();
    }

    public void ajouterArete(int u, int v) {
        List<Integer> voisins = adjacence.get(u); //tous les voisins de u

        if (voisins == null) { //si pas encore rencontré u
            voisins = new ArrayList<>(); //liste de voisins vide
            adjacence.put(u, voisins);
        }

        voisins.add(v);
    }

    public List<Integer> getVoisins(int u) {
        List<Integer> voisins = adjacence.get(u);

        if (voisins == null) { // si aucun
            return Collections.emptyList(); //liste qui prend pas de place en mémoire
        }

        return voisins;
    }

    public Set<Integer> getSommets() {
        Set<Integer> sommets = new HashSet<>(adjacence.keySet()); //voisins sortants

        for (List<Integer> voisins : adjacence.values()) {
            sommets.addAll(voisins); //on les rajoute
        }

        return sommets;
    }

    public List<List<Integer>> obtenirCFC() {
        Tarjan tarjan = new Tarjan(adjacence);
        return tarjan.trouverCFC();
    }

    public int calculerDiametre(List<Integer> membresCFC) {
        if (membresCFC.size() <= 1) return 0;
        
        Set<Integer> setMembres = new HashSet<>(membresCFC);
        int diametreMax = 0;

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

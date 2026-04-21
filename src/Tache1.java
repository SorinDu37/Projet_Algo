import java.io.PrintWriter;
import java.nio.file.*;
import java.util.*;

public class Tache1 {
    //constantes
    private final int MAX_AUTEURS = 5000000;
    private final int MAX_QUEUE_SIZE = 256;
    
    //globales
    private Path xmlPath;
    private Path dtdPath;
    
    private UF unionFind;
    private Map<String, Integer> nomVersId;
    private Set<Integer> racinesActives;

    public Tache1(String fichierXml, String fichierDtd) {
        this.xmlPath = Paths.get(fichierXml); 
        this.dtdPath = Paths.get(fichierDtd);
        
        this.unionFind = new UF(MAX_AUTEURS);
        this.nomVersId = new HashMap<>();
        this.racinesActives = new HashSet<>();

    }

    private int obtenirId(String nomAuteur) {
        if (!nomVersId.containsKey(nomAuteur)) {
            int nouvelId = nomVersId.size(); //prochain id sera la taille du nomversId
            nomVersId.put(nomAuteur, nouvelId);

            racinesActives.add(nouvelId);
        }
        return nomVersId.get(nomAuteur);
    }

    public void analyserDonnees() throws Exception {

        long pubCount = 0;

        try (DblpPublicationGenerator gen = new DblpPublicationGenerator(xmlPath, dtdPath, MAX_QUEUE_SIZE)) { //pour gérer erreurs
            while (true) {
                Optional<DblpPublicationGenerator.Publication> publication = gen.nextPublication(); //prochaine publication
                if (publication.isEmpty()) break; //si y'a plus rien
                
                pubCount++;

                List<String> auteurs = publication.get().authors; //retire tous les auteurs
                
                if (auteurs == null || auteurs.size() < 2) { //on passe à la prochaine boucle si trop peu d'auteurs
                    continue;
                }

                String first_autheur = auteurs.get(0); 
                int idCorrespondant = obtenirId(first_autheur);

                for (int i = 1; i < auteurs.size(); i++) { //on relie au reste des autheurs
                    String autre_auteur = auteurs.get(i);
                    int idAutre = obtenirId(autre_auteur);

                    // On récupère les racines AVANT la fusion
                    int racine1 = unionFind.find(idCorrespondant);
                    int racine2 = unionFind.find(idAutre);

                    // Si la méthode union renvoie 'true', c'est qu'il y a eu une fusion
                    if (unionFind.union(racine1, racine2)) {
                        // On cherche qui a gagné (la nouvelle racine)
                        int nouvelleRacine = unionFind.find(racine1);
                        int racineAbsorbee;
                        if (nouvelleRacine == racine1) {
                            racineAbsorbee = racine2; // Si racine1 a gagné, racine2 a été absorbée
                        } else {
                            racineAbsorbee = racine1; // Sinon, c'est racine1 qui a été absorbée
                        } //on retire l'autre
                            racinesActives.remove(racineAbsorbee);
                    }
                }

                if (pubCount % 100000 == 0) {
                    afficherStatistiques(pubCount);
                }
            }
        }
    }

    public int getNombreVraiesCommunautes() {
        int nombreInutiles = MAX_AUTEURS - nomVersId.size(); //obtenir combien sont inutilisés
        return unionFind.count() - nombreInutiles; //nombre total - inutilisés = total de communautés
    }

    public void genererHistogramme(String nomFichierSortie) throws Exception {
        // Clé = Taille de la communauté, Valeur = Nombre de communautés de cette taille
        Map<Integer, Integer> histogramme = new HashMap<>();

        for (int racine : racinesActives) {
            int taille = unionFind.getSize(racine);
            // On incrémente le compteur pour cette taille
            histogramme.put(taille, histogramme.getOrDefault(taille, 0) + 1);
        }

        // Écriture dans un fichier 
        try (PrintWriter writer = new PrintWriter(nomFichierSortie)) {
            writer.println("TailleCommunauté,NombreDeCommunautés");
            for (Map.Entry<Integer, Integer> entry : histogramme.entrySet()) {
                writer.println(entry.getKey() + "," + entry.getValue());
            }
        }
        System.out.println("Histogramme sauvegardé dans : " + nomFichierSortie);
    }   


    private void afficherStatistiques(long pubCount) {
        System.out.println("Après " + pubCount + " publications");
        System.out.println("Nombre de communautés actives : " + getNombreVraiesCommunautes());

        List<Integer> tailles = new ArrayList<>();
        for (int racine : racinesActives) {
            tailles.add(unionFind.getSize(racine)); //retrouver toutes les communautés
        }

        tailles.sort(Collections.reverseOrder()); 

        System.out.print("Top 10 des tailles : ");
        int limit = Math.min(10, tailles.size()); //si y'a moins de 10 communautés
        for (int i = 0; i < limit; i++) {
            System.out.print(tailles.get(i) + " ");
        }
        System.out.println("\n");
    }


    public static void main(String[] args) throws Exception {
        System.out.println("Démarrage de l'analyse DBLP...");
        

        String xmlPath = "../lib/dblp-2026-01-01.xml.gz";
        String dtdPath = "../lib/dblp.dtd";
        Tache1 analyseur = new Tache1(xmlPath, dtdPath);
        
        analyseur.analyserDonnees();
        
        analyseur.genererHistogramme("histogramme_tailles.csv");
        
        System.out.println("Traitement terminé avec succès !");
    }
}
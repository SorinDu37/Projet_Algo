package dblp;

import dblp.taches.Tache1;
import dblp.taches.Tache2;

/**
 * Point d'entrée du programme. Lance successivement la Tâche 1 (Union-Find)
 * puis la Tâche 2 (graphe orienté + Tarjan) sur le fichier DBLP.
 */
public class Main {
    public static void main(String[] args) {
        String xml = "lib/dblp-2026-01-01.xml.gz";
        String dtd = "lib/dblp.dtd";

        // Désactiver les limites d'expansion d'entités XML (nécessaire pour le DTD de DBLP)
        System.setProperty("jdk.xml.entityExpansionLimit", "0");
        System.setProperty("jdk.xml.totalEntitySizeLimit", "0");
        System.setProperty("jdk.xml.maxGeneralEntitySizeLimit", "0");
        System.setProperty("jdk.xml.maxParameterEntitySizeLimit", "0");

        try {
            System.out.println("LANCEMENT DU PROGRAMME D'ANALYSE DBLP\n");

            Tache1 tache1 = new Tache1(xml, dtd);
            tache1.executer();

            System.out.println("\n=========================================\n");

            Tache2 tache2 = new Tache2(xml, dtd);
            tache2.executer();

            System.out.println("\nTOUTES LES TÂCHES SONT TERMINÉES !");

        } catch (Exception e) {
            System.err.println("Une erreur critique est survenue !");
            e.printStackTrace();
        }
    }
}
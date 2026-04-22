package dblp;

import dblp.taches.Tache1;
import dblp.taches.Tache2;
import dblp.core.Tache;

public class Main {
    public static void main(String[] args) {
        String xml = "lib/dblp-2026-01-01.xml.gz";
        String dtd = "lib/dblp.dtd";

        try {
            System.out.println("LANCEMENT DU PROGRAMME D'ANALYSE DBLP\n");

            Tache tache1 = new Tache1(xml, dtd);
            tache1.executer();

            System.out.println("\n=========================================\n");

            Tache tache2 = new Tache2(xml, dtd);
            tache2.executer();

            System.out.println("\nTOUTES LES TÂCHES SONT TERMINÉES !");

        } catch (Exception e) {
            System.err.println("Une erreur critique est survenue !");
            e.printStackTrace();
        }
    }
}

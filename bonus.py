"""
Tâche Bonus : Identifier les pays des auteurs des 10 plus grandes communautés (CFC).
Utilise l'API OpenAlex pour trouver l'institution de chaque auteur.

Usage : python3 bonus.py output/tache2_top10_XXXXXXXX_XXXXXX.txt
"""

import sys
import os
import time
import requests
import matplotlib.pyplot as plt
from collections import defaultdict

OPENALEX_EMAIL = "projet-algo2@ulb.be"
BASE_URL = "https://api.openalex.org"


def lire_communautes(fichier):
    communautes = []
    comm = None
    with open(fichier, "r", encoding="utf-8") as f:
        for ligne in f:
            ligne = ligne.strip()
            if ligne.startswith("=== Communauté"):
                num = int(ligne.split()[2])
                comm = {"numero": num, "taille": 0, "diametre": 0, "membres": []}
                communautes.append(comm)
            elif ligne.startswith("Taille :") and comm:
                comm["taille"] = int(ligne.split(":")[1].strip())
            elif ligne.startswith("Diamètre :") and comm:
                comm["diametre"] = int(ligne.split(":")[1].strip())
            elif ligne.startswith("- ") and comm:
                comm["membres"].append(ligne[2:].strip())
    return communautes


def chercher_pays(nom):
    try:
        r = requests.get(f"{BASE_URL}/authors", params={"search": nom, "per_page": 1, "mailto": OPENALEX_EMAIL}, timeout=10)
        if r.status_code != 200:
            return "Inconnu"
        results = r.json().get("results", [])
        if not results:
            return "Inconnu"
        insts = results[0].get("last_known_institutions", [])
        if insts and insts[0].get("country_code"):
            return insts[0]["country_code"]
        return "Inconnu"
    except:
        return "Inconnu"


def main():
    if len(sys.argv) < 2:
        fichiers = sorted([f for f in os.listdir("output") if f.startswith("tache2_top10")])
        if not fichiers:
            print("Usage : python3 bonus.py output/tache2_top10_XXXXXXXX.txt")
            sys.exit(1)
        fichier = os.path.join("output", fichiers[-1])
    else:
        fichier = sys.argv[1]

    print("=== TÂCHE BONUS : Pays des auteurs (OpenAlex) ===")
    communautes = lire_communautes(fichier)
    print(f"{len(communautes)} communautés lues depuis {fichier}")

    resultats = {}
    for comm in communautes:
        pays_compteur = defaultdict(int)
        print(f"\nCommunauté {comm['numero']} ({len(comm['membres'])} membres) :")
        for i, nom in enumerate(comm["membres"]):
            time.sleep(0.15)
            pays = chercher_pays(nom)
            pays_compteur[pays] += 1
            if (i+1) % 10 == 0 or (i+1) == len(comm["membres"]):
                print(f"  {i+1}/{len(comm['membres'])} traités...")
        resultats[comm["numero"]] = dict(pays_compteur)
        print(f"  -> {dict(pays_compteur)}")

    # CSV
    tous_pays = sorted(set(p for d in resultats.values() for p in d))
    with open("output/bonus_pays.csv", "w") as f:
        f.write("communaute,taille," + ",".join(tous_pays) + "\n")
        for c in communautes:
            f.write(f"{c['numero']},{c['taille']}")
            for p in tous_pays:
                f.write(f",{resultats[c['numero']].get(p,0)}")
            f.write("\n")
    print("\nCSV -> output/bonus_pays.csv")

    # Graphique
    fig, ax = plt.subplots(figsize=(14, 7))
    labels = [f"CFC {c['numero']}\n(n={c['taille']})" for c in communautes]
    couleurs = plt.cm.tab20.colors
    bottom = [0] * len(labels)
    for i, pays in enumerate(tous_pays):
        vals = [resultats[c["numero"]].get(pays, 0) / sum(resultats[c["numero"]].values()) * 100 for c in communautes]
        ax.bar(labels, vals, bottom=bottom, label=pays, color=couleurs[i % len(couleurs)])
        bottom = [b+v for b, v in zip(bottom, vals)]
    ax.set_ylabel("Proportion (%)")
    ax.set_xlabel("Communauté")
    ax.set_title("Proportion d'auteurs par pays dans les 10 plus grandes CFC")
    ax.legend(bbox_to_anchor=(1.05, 1), loc='upper left', fontsize=8)
    plt.tight_layout()
    plt.savefig("output/bonus_pays.png", dpi=150, bbox_inches='tight')
    print("Graphique -> output/bonus_pays.png")
    print("\n=== TÂCHE BONUS TERMINÉE ===")


if __name__ == "__main__":
    main()

# DBLP Research Community Analyzer

This project is a high-performance Java suite designed to process the **DBLP Computer Science Bibliography** dataset. It identifies and analyzes research communities using large-scale graph algorithms, optimized for datasets with millions of entries. It includes a Python-based extension for geographical analysis of the results.

---

## Project Architecture

The project follows a modular package structure to ensure scalability and maintainability:

* **`dblp`** (Root): Contains the `Main.java` entry point.
* **`dblp.core`**: Handles data ingestion, XML parsing via `DblpPublicationGenerator`, and the base analysis engine.
* **`dblp.structures`**: Contains pure algorithmic implementations:
    * **Union-Find**: Optimized with path compression and weighted union.
    * **GrapheOriente**: Adjacency-based graph with BFS capabilities.
    * **Tarjan**: Linear-time algorithm for Strongly Connected Components (SCC).
* **`dblp.taches`**: Specific implementation logic for Tasks 1 and 2.
* **`bonus.py`**: Python script for geographical metadata enrichment using the OpenAlex API.

---

## Algorithms & Logic

### Task 1: Undirected Co-authorship
Analyzes co-authorship as an undirected relationship where every publication connects all its authors into a single set.
* **Mechanism**: Union-Find.
* **Optimization**: Instead of tracking active roots during the stream, the system performs a single linear pass at the end to identify unique communities.

### Task 2: Directed Influence & SCC
Analyzes directed relationships where the first author is considered the "source" of the tie.
* **Filter**: Only relationships with $\ge 6$ co-publications are retained.
* **SCC (Strongly Connected Components)**: Uses **Tarjan's Algorithm** to find groups where every author can reach every other author.
* **Diameter**: Calculates the "longest shortest path" using BFS.

### Bonus: Geographical Analysis
Identifies the country of origin for authors in the top 10 largest SCCs.
* **API**: Queries [OpenAlex](https://openalex.org/) to retrieve the `last_known_institution` for each author.
* **Visualization**: Generates a distribution histogram (CSV) and a stacked bar chart (PNG) of national representation within communities.

---

## Setup & Execution

### 1. Data Preparation
Place the following files in the `lib/` directory:
* `dblp.xml.gz` (Official DBLP dataset)
* `dblp.dtd` (XML Structure definition)

### 2. Java Compilation & Run
Compile and run the main analysis suite (Requires at least 8GB RAM):
```bash
# Compile
javac -d bin src/dblp/Main.java src/dblp/core/*.java src/dblp/structures/*.java src/dblp/taches/*.java

# Run
java -cp bin -Xmx8G dblp.Main

### 3. Bonus Task (Python)
The bonus script requires Python 3.x and the `requests` and `matplotlib` libraries.

```bash
# Install dependencies
pip install requests matplotlib

# Run analysis (automatically detects the latest output file)
python3 bonus.py
```

### 3. Bonus Task (Python)
The bonus script requires Python 3.x and the `requests` and `matplotlib` libraries.

```bash
# Install dependencies
pip install requests matplotlib

# Run analysis (automatically detects the latest output file)
python3 bonus.py
```

## Output Results

All results are exported to the `output/` directory:
* **tache1_histogram.csv**: Distribution of undirected community sizes.
* **tache2_histogram.csv**: Distribution of SCC sizes.
* **tache2_top10.txt**: Detailed breakdown of the 10 largest directed communities.
* **bonus_pays.csv**: Matrix of country counts per community.
* **bonus_pays.png**: Stacked bar chart showing the geographical proportions of the top communities.

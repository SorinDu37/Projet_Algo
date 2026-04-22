# DBLP Research Community Analyzer

This project is a high-performance Java suite designed to process the **DBLP Computer Science Bibliography** dataset. It identifies and analyzes research communities using large-scale graph algorithms, optimized for datasets with millions of entries.

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

---

## Algorithms & Logic

### Task 1: Undirected Co-authorship
Analyzes co-authorship as an undirected relationship where every publication connects all its authors into a single set.
* **Mechanism**: Union-Find.
* **Optimization**: Instead of tracking active roots during the stream (high overhead), the system performs a single linear pass at the end to identify unique communities.

### Task 2: Directed Influence & SCC
Analyzes directed relationships where the first author is considered the "source" of the tie.
* **Filter**: Only relationships with $\ge 6$ co-publications are retained.
* **SCC (Strongly Connected Components)**: Uses **Tarjan's Algorithm** to find groups where every author can reach every other author in the directed subgraph.
* **Diameter**: Calculates the "longest shortest path" within a community using BFS. For a community $C$, the diameter $D$ is defined as the maximum distance between any two nodes.

---

## Setup & Execution

### 1. Data Preparation
Place the following files in the `lib/` directory:
* `dblp.xml.gz` (Official DBLP dataset)
* `dblp.dtd` (XML Structure definition)

If done via github, run :
```bash
git lfs install
git lfs pull
```

### 2. Compilation
Compile all packages from the project root using the following command:
```bash
javac -d bin src/dblp/Main.java src/dblp/core/*.java src/dblp/structures/*.java src/dblp/taches/*.java
```

### 3. Execution
The application requires significant heap space to store millions of author IDs and graph edges. Use the -Xmx flag to allocate at least 8GB of RAM:
```bash
java -cp bin -Xmx8G dblp.Main
```
## Output Results

All results are exported to the `output/` directory:
* **tache1_histogram.csv**: Distribution of undirected community sizes.
* **tache2_histogram.csv**: Distribution of SCC sizes.
* **tache2_top10.txt**: Detailed breakdown of the 10 largest directed communities, including member names and diameters.

(This Readme has been written with the help of Gemini AI on 22/04/2026)

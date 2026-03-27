# 🔍 Moteur de Recherche Local - Projet Info4B

Un moteur de recherche de fichiers locaux léger et performant développé en Java. Ce projet implémente une architecture **Client-Serveur** communiquant via des Sockets TCP et repose sur un **Index Inversé** pour effectuer des recherches textuelles optimisées (avec calcul de pertinence TF-IDF).

![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Maven](https://img.shields.io/badge/Build-Maven-blue.svg)

## ✨ Fonctionnalités Principales

- **Indexation automatique** : Parcours et indexe le contenu des fichiers `.txt` et `.pdf` (nécessite `pdftotext`).
- **Recherche avancée (TF-IDF)** : Recherche de mots-clés avec tri par pertinence.
- **Filtres d'exclusion** : Possibilité de rechercher des mots tout en excluant certains résultats.
- **Persistance des données** : Système de journalisation (`journal.csv`) fonctionnant en arrière-plan (Producteur/Consommateur) pour sauvegarder et restaurer l'état de l'index sans ralentir le serveur.
- **Client interactif CLI** : Interface en ligne de commande pour interroger le serveur en temps réel.

---

## 🛠️ Prérequis

Avant de lancer le projet, assurez-vous d'avoir installé :
- **Java JDK 17** (ou version supérieure)
- **Maven** (pour la gestion des dépendances et la compilation)
- **Poppler-utils / pdftotext** (nécessaire pour l'extraction du texte des fichiers PDF)

### Installation des prérequis

- **Java JDK 17** (Environnement de développement et d'exécution) :
    - Mac OS (via Homebrew) : `brew install openjdk@17`
    - Linux (Ubuntu/Debian) : `sudo apt install openjdk-17-jdk`


- **Maven** (Outil de gestion des dépendances et de compilation) :
    - Mac OS (via Homebrew) : `brew install maven`
    - Linux (Ubuntu/Debian) : `sudo apt install maven`


- **Poppler-utils / pdftotext** (Nécessaire pour l'indexation des fichiers PDF) :
    - Mac OS (via Homebrew) : `brew install poppler`
    - Linux (Ubuntu/Debian) : Généralement présent par défaut. Si ce n'est pas le cas : `sudo apt install poppler-utils`


- **exiv2** (Nécessaire pour obtenir les données exif des images) :
  - Mac OS (via Homebrew) : `brew install exiv2`
  - Linux (Ubuntu/Debian) : Généralement présent par défaut. Si ce n'est pas le cas : `sudo apt install exiv2`

---

## 🚀 Compilation et Exécution

### 1. Compiler le projet
`mvn clean compile`

### 2. Démarrer le Serveur
*`mvn exec:java -Dexec.mainClass="Serveur.Main"`*

Le serveur démarrera sur le port `12345` et lancera la restauration/l'indexation du répertoire cible.

### 3. Démarrer le Client
Ouvrez un nouveau terminal et lancez le client :
*`mvn exec:java -Dexec.mainClass="Client.Main"`*

Par défaut le client démarrera et se connectera sur `localhost` port `12345`

---
### 4. Script automatisé
*`chmod +x run.sh`*

*`./run.sh`*

## 📖 Guide des Commandes (Client)

Une fois le client connecté, utilisez l'interface en ligne de commande (`>`) :

| Commande | Action | Exemple |
| :--- | :--- | :--- |
| `-h` | Affiche le menu d'aide complet. | `> -h` |
| `-s <mots>` | Recherche un ou plusieurs mots (séparés par des virgules). | `> -s linux,systeme` |
| `-s <mots> -- <exclus>` | Recherche des mots en excluant les documents contenant les mots après le `--`. | `> -s reseau -- windows` |
| `-m <chemin>` | Affiche les métadonnées d'un document indexé (poids, date, mots totaux). | `> -m src/test.txt` |
| `-p <chemin>` | Affiche le texte brut extrait du document. | `> -p src/test.pdf` |
| `-t <msg>` | Test (Ping) de la communication avec le serveur. | `> -t Bonjour Serveur` |
| `q` | Quitte proprement le client et ferme la connexion. | `> q` |

---
*Projet réalisé dans le cadre de l'UE Info4B (L2).*
# Moteur de Recherche Local - Projet Info4B

Un moteur de recherche de fichiers locaux performant et robuste développé en Java. Ce projet implémente une architecture **Client-Serveur** communiquant via des flux binaires sécurisés (Sockets TCP) et repose sur un **Index Inversé** en RAM pour effectuer des recherches textuelles ultra-rapides basées sur l'algorithme **TF-IDF**.

![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Maven](https://img.shields.io/badge/Build-Maven-blue.svg)
![Release](https://img.shields.io/badge/Release-v1.0-success.svg)

## Fonctionnalités Principales

- **Indexation Automatique & WatchService** : Parcours initial et surveillance en temps réel (anti-crash) des modifications de fichiers (`.txt`, `.pdf`).
- **Recherche Avancée (TF-IDF & Booléen)** : Tri par pertinence mathématique, filtres d'exclusion, et support des opérateurs logiques (`ET`, `OU`, `SAUF`).
- **Transfert Binaire Sécurisé** : Téléchargement de fichiers du serveur vers le client sans aucune corruption de données.
- **Interface CLI Interactive (JLine)** : Client en ligne de commande offrant une expérience native (historique des commandes avec la flèche du haut, autocomplétion, design clair).
- **Persistance** : Système de journalisation (`journal.csv`) via le pattern Producteur/Consommateur pour ne jamais perdre l'état de l'index.

---

## Documentation Complète

Pour découvrir en détail l'architecture réseau, l'explication complète des commandes et des exemples d'utilisation avancés, consultez notre **[Documentation Web Interactive](searchengine.antoineragot.com/docs/)** *(Ouvrez le fichier `docs/index.html` dans votre navigateur)*.

---

## Prérequis

Avant de lancer le projet, assurez-vous d'avoir installé les composants suivants sur votre machine :

- **Java JDK 17** (ou version supérieure)
- **Maven** (pour la compilation et les dépendances)
- **Poppler-utils / pdftotext** (nécessaire pour lire le contenu des PDF)
- **exiv2** (nécessaire pour l'extraction des métadonnées d'images)

### Commandes d'installation rapide :
- **Mac OS (via Homebrew)** : `brew install openjdk@17 maven poppler exiv2`
- **Linux (Ubuntu/Debian)** : `sudo apt update && sudo apt install openjdk-17-jdk maven poppler-utils exiv2`

---

## Lancement Rapide (Recommandé)

Plutôt que de taper les commandes Maven manuellement, utilisez notre script d'orchestration intégré. Il gère la compilation, le lancement asynchrone et le nettoyage des ports réseau en cas de problème.

    # 1. Donner les droits d'exécution au script
    chmod +x run.sh
    
    # 2. Lancer le menu interactif
    ./run.sh

*Depuis le menu, appuyez sur `1` pour compiler, `2` pour lancer le serveur en arrière-plan, puis `3` pour ouvrir le client interactif.*

### Lancement Manuel (Alternative)
Si vous préférez utiliser des terminaux séparés :
1. `mvn clean compile`
2. **Terminal 1 (Serveur)** : `mvn exec:java -Dexec.mainClass="Serveur.Main"` *(Port 12345)*
3. **Terminal 2 (Client)** : `mvn exec:java -Dexec.mainClass="Client.Main"`

---

##  Guide Rapide des Commandes (CLI)

Une fois le client connecté au serveur, utilisez le prompt interactif `> ` :

### Indexation & Recherche
| Commande | Action | Exemple |
| :--- | :--- | :--- |
| `-s <dossier>` | Indexe un dossier entier et active la surveillance temporelle. | `> -s src/documents` |
| `-r <mots> -- <exclus>` | Recherche TF-IDF standard avec exclusion optionnelle (`--`). | `> -r reseau -- windows` |
| `-ar <requête>` | Recherche logique stricte avec opérateurs. | `> -ar java ET reseau SAUF cpp` |

### Gestion des Fichiers & Réseau
| Commande | Action | Exemple |
| :--- | :--- | :--- |
| `-dl <chemin>` | **Télécharge** le fichier distant vers le dossier `downloads/` local. | `> -dl src/cours.pdf` |
| `-m <chemin>` | Affiche les métadonnées internes du document. | `> -m src/notes.txt` |
| `-m -rn <old> <new>` | **Renomme** un fichier sur le serveur et met à jour l'index. | `> -m -rn vieux.txt neuf.txt` |
| `-m -rm <chemin>` | **Supprime** le fichier du serveur et purge l'index. | `> -m -rm src/secret.txt` |

### Outils Avancés
| Commande | Action | Exemple |
| :--- | :--- | :--- |
| `-exif <image>` | Extrait les métadonnées cachées (GPS, Date) d'une image. | `> -exif image.jpg` |
| `-d <fich1> <fich2>` | Détecte si deux fichiers sont des doublons stricts. | `> -d f1.txt f2.txt` |
| `-sw -add <mots>` | Ajoute des Stop-Words (mots exclus du calcul de pertinence). | `> -sw -add donc,mais` |

---
*Projet académique réalisé dans le cadre de l'UE Info4B (L2) - Université de Bourgogne.*
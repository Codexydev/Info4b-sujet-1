# Moteur de Recherche Info4B - Release v1.0

Un moteur de recherche de fichiers locaux (PDF, TXT, Images) performant, basé sur une architecture **Client-Serveur** robuste et un système d'indexation intelligent.

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Build-Maven-blue.svg)](https://maven.apache.org/)
[![Website](https://img.shields.io/badge/Documentation-Web-brightgreen.svg)](https://searchengine.antoineragot.com)

## Site Web et Documentation

Le projet dispose d'un site web officielle et d'une documentation interactive accessible à l'adresse suivante :  
[https://searchengine.antoineragot.com](https://searchengine.antoineragot.com)

---

## Fonctionnalités Majeures

- **Recherche de mot clé et analyse de Pertinence TF-IDF** : Algorithme de tri des résultats par score d'importance des mots-clés.
- **Recherche Avancée** : Support des opérateurs logiques `ET`, `OU` et `SAUF` pour des requêtes précises.
- **Surveillance en Temps Réel** : Détection automatique des nouveaux fichiers ou des modifications dans le répertoire indexé via un `WatchService` dédié.
- **Téléchargement de fichiers** : Téléchargement sécurisé de fichiers du serveur vers le client avec un retour visuel fluide dans la console.
- **Gestion Distante de Fichiers** : Possibilité de renommer, déplacer ou supprimer des fichiers directement depuis le client.
- **Persistance par Journalisation** : Sauvegarde de l'état de l'index dans `journal.csv` permettant une restauration instantanée sans ré-indexation complète au démarrage.
- **Extraction Multimédia** : Lecture du texte des PDF et extraction des métadonnées EXIF des images.

---

# Guide d'exécution du Moteur de Recherche (Projet Info4B)

Ce guide détaille la procédure pour lancer le serveur et le client à partir de l'archive `.jar` unique générée par Maven.

## 1. Prérequis
* Avoir installé le **Java Development Kit (JDK)** version 17 ou supérieure.
* S'assurer que les outils système nécessaires (`pdftotext`, `exiv2`) sont installés et accessibles dans le PATH de la machine.
* Le répertoire cible à indexer (par défaut `src/testIndexed`) doit exister au même niveau que le `.jar`.

## 2. Lancement (via jar)
Le serveur doit être lancé en premier pour initialiser l'**Index Inversé** et le **Journal (WAL)**. Comme le `.jar` pointe par défaut sur le client, vous devez spécifier la classe de démarrage.

Ouvrez un terminal et tapez :
```bash
java -cp projet.jar Serveur.Main
```
puis pour le côté client tapez :
```bash
java -cp projet.jar Client.Main
```

## Lancement Rapide (via run.sh)

Le projet inclut un script d'orchestration `run.sh` pour automatiser toutes les étapes.

### 2. Utilisation du script

    chmod +x run.sh
    ./run.sh

*Le script propose un menu interactif pour compiler, lancer le serveur et démarrer le client.*

---

## Guide des Commandes Client

Une fois connecté, utilisez les commandes suivantes :

### Recherche et Indexation
| Commande | Action | Exemple |
| :--- | :--- | :--- |
| `-s <mots>` | Recherche TF-IDF (séparateur `,`) | `> -s reseau,java` |
| `-ar <requête>` | Recherche logique (ET, OU, SAUF) | `> -ar java ET reseau SAUF cpp` |
| `-l` | Liste tous les fichiers indexés | `> -l` |

### Gestion de fichiers
| Commande                       | Action | Exemple |
|:-------------------------------| :--- | :--- |
| `-dl <chemin>`                 | **Télécharger** un fichier avec barre de progression | `> -dl src/docs/cours.pdf` |
| `-m -rn <old> <new>`           | **Renommer ou Déplacer** un fichier | `> -m -rn test.txt archive/test.txt` |
| `-m -rm <chemin>`              | **Supprimer** un fichier du serveur | `> -m -rm src/temp.txt` |
| `-tag <action> <chemin> [tags]`| **Annoter des fichiers** (-add, -rm, -l) pour la recherche sémantique. | `> -tag -add image.jpg vacances` |

### Outils
| Commande              | Action                                                       | Exemple |
|:----------------------|:-------------------------------------------------------------| :--- |
| `-exif <image>`       | Extraire les métadonnées d'une image                         | `> -exif photo.jpg` |
| `-exif -set <chemin>` | **modifier** les métadonnées (EXIF/IPTC) d'une image | `> -exif -set photo.jpg "Plage"` |
| `-d <f1> <f2>`        | Détecter si deux fichiers sont des **doublons**              | `> -d a.txt b.txt` |
| `-sw -add <mot>`      | Ajouter un mot à la liste des **Stop Words**                 | `> -sw -add donc` |

---
*Projet réalisé dans le cadre de l'UE Info4B - Université de Bourgogne.*
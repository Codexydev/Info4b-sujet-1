#!/bin/bash
# Script interactif pour le Moteur de Recherche Info4B

echo "========================================"
echo " MENU DE LANCEMENT - PROJET INFO4B "
echo "========================================"
echo "1) Recompiler tout le projet (Maven)"
echo "2) Démarrer le Serveur"
echo "3) Démarrer le Client interactif"
echo "4) Quitter"
echo "========================================"
read -p "-> Entrez votre choix (1-4) : " choix

case $choix in
    1)
        echo -e "\n Compilation en cours via Maven..."
        mvn clean compile
        echo -e " Compilation terminée ! Relancez le script pour démarrer le serveur.\n"
        ;;
    2)
        echo -e "\n Démarrage du Serveur sur le port 12345...\n"
        mvn exec:java -Dexec.mainClass="Serveur.Main"
        ;;
    3)
        echo -e "\n Démarrage du Client...\n"
        mvn exec:java -Dexec.mainClass="Client.Main"
        ;;
    4)
        echo -e "\n À bientôt !"
        exit 0
        ;;
    *)
        echo -e "\n Choix invalide. Veuillez relancer le script."
        ;;
esac
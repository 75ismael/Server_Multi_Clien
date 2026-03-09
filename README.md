# 🚀 Server Multi-Client (Java NIO)

Ce projet implémente un serveur multi-client performant utilisant les **Selector** de Java NIO (Non-blocking I/O). Il permet de gérer des centaines de connexions simultanées sur un seul thread de sélection, optimisant ainsi l'utilisation des ressources système.

## ✨ Points Forts Technologiques

- **Java NIO (Non-blocking I/O)** : Utilisation de canaux (`SocketChannel`) et de sélecteurs pour une gestion asynchrone des entrées/sorties.
- **Gestion d'État** : Système de sessions pour suivre l'état de chaque client connecté.
- **Performance** : Conçu pour minimiser la latence et maximiser le débit de données.

## 🛠 Tech Stack

- **Langage** : Java
- **Bibliothèques** : `java.nio`, `java.net`
- **Architecture** : Pattern Reactor (Single-threaded Selector).

## 📦 Utilisation

1. **Compilation** :
   ```bash
   javac ServeurSelect.java
   ```
2. **Lancement du Serveur** :
   ```bash
   java ServeurSelect
   ```

---
*Développé dans le cadre des travaux pratiques de Réseaux L3.*

# TP4 - Gestion de clients multiples au Mastermind

## 1. Serveur simple 

**Question : Reprendre le serveur du TP précédent, expliquer précisément ce qui se passe lorsque deux clients se connectent en même temps au serveur.**

Dans la version simple du serveur (mono-thread), la boucle principale attend l'arrivée d'un nouveau client avec `serverSocket.accept()`. Lorsqu'un premier client se connecte, la méthode `accept()` retourne le socket correspondant et le programme entre dans une série de lectures bloquantes `in.readLine()` pour communiquer avec lui.
Si un deuxième client essaie de se connecter pendant ce temps, la demande de connexion est mise en attente (dans la queue du système d'exploitation associée au port d'écoute), mais le serveur ne créera pas de socket pour lui et n'interagira pas avec lui car il est occupé ("bloqué") avec le premier client. Le deuxième client devra attendre que le premier ait terminé et déconnecté (ce qui termine la boucle d'interaction et permet au serveur de rappeler `serverSocket.accept()`).

**Question : Quelles solutions connaissez-vous pour gérer les entrées/sorties concurrentes et asynchrones ?**

Il existe plusieurs solutions pour gérer des clients connectés concurrents de façon asynchrone :
1. **Les threads (Multi-threading)** : Pour chaque client se connectant (retour de `accept()`), le serveur crée un nouveau thread (ou tâche `Runnable` pour un pool de threads) à qui il passe le socket du client. Ainsi, le thread principal peut tout de suite re-boucler et accepter une nouvelle connexion pendant que les threads secondaires s'occupent des entrées/sorties avec les clients en parallèle.
2. **Le multiplexage I/O (Asynchrone non-bloquant - NIO en Java)** : Utiliser des Sélecteurs (`java.nio.channels.Selector`). Le serveur demande d'être notifié uniquement lorsqu'un des sockets a des données prêtes à être lues ou écrites. Cela permet à un seul thread de gérer un grand nombre de connexions simultanées sans jamais bloquer sur une lecture ou écriture.

## 2. Test de performances

### 2.2 Stress 1 sans fermeture immédiate

**Observations du serveur avec plusieurs clients `n` (faisant une requête et restant connectés indéfiniment) :**

- **n = 1** : Le serveur crée 1 thread qui répond et attend, se comportant normalement.
- **n = 2, 10, 100** : Tous les threads sont créés, le système gère bien la charge grâce au multi-threading Java.
- **n = 1000, n = 5000** : Un grand nombre de threads et de sockets ouverts en même temps qui ne se ferment pas. Selon l'OS (notamment macOS ou Linux), lancer 1000 à 5000 requêtes bloquantes peut atteindre la limite du système concernant le nombre maximal de processeurs gérables par JVM ou le nombre maximal de descripteurs de fichiers (`ulimit -n`). Des exceptions de type "Too many open files" ou "Could not create OS Thread" peuvent survenir.

### Stress 1 avec fermeture immédiate

**Question : Y a-t-il une différence de comportement de votre serveur selon que vous fermez la connexion immédiatement ou non ?**

**Oui.** Si les clients ferment immédiatement la connexion après réception de la réponse, le nombre de connexions TCP actives (et donc le nombre de threads simultanément vivants côté serveur) chute très vite au fur et à mesure que les requêtes passent. Le système récupère immédiatement les ressources (descripteurs de sockets, mémoire allouée et threads OS).
En revanche, s'ils restent ouverts, ces ressources s'accumulent ce qui peut amener à l'effondrement du système sur la durée.

**Question : Que se passe-t-il si vous lancez 5 clients Stress1 simultanément avec comme paramètre 1 000 ?**

Lancer 5 instances de `Stress1(1000)` en parallèle revient à tenter 5000 connexions avec le serveur dans un temps très réduit. Que ces connexions se referment rapidement ou pas, l'acceptation très conjointe au niveau du serveur peut mener à un goulot d'étranglement qui ralentit sérieusement le délai d'acceptation, voire à l'abandon de paquets dans le backlog (TCP SYN floods l'OS local) si c'était un pool externe massif, ou simplement conduire plus rapidement à des exceptions d'accès ou à des décalages sur le scheduler des threads.
Cependant, si la fermeture est immédiate et les programmes s’exécutant depuis `localhost`, le serveur devrait tenir la charge sans s'effondrer car les threads terminent leur exécution presque instantanément après l'envoi de l'indice et libèrent les ressources.

### 2.3 Latence

Pour mesurer la latence, nous utilisons `System.nanoTime()` dans un test d'envoi. On note que la latence moyenne (du client : juste avant le print, jusqu'à l'obtention de la réponse) augmente proportionnellement avec $n$. Pour générer les statistiques de base du graphique, le programme génère un fichier `latence.csv` enregistrant cette valeur moyenne de réponse pour les tests passés avec un $n$ variable.

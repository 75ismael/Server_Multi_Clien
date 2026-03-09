import java.io.*;
import java.net.*;
import java.util.Random;

public class Codeur {

  public static void main(String[] args) {
    int port = 1234;

    try (ServerSocket serverSocket = new ServerSocket(port)) {
      System.out.println("Serveur Mastermind Multi-thread (TCP) lancé sur le port " + port);

      while (true) {
        // Attente d'un nouveau décodeur
        Socket clientSocket = serverSocket.accept();
        System.out.println("Nouveau client connecté : " + clientSocket.getInetAddress());

        // Créer un nouveau thread pour le client
        new Thread(new ClientHandler(clientSocket)).start();
      }
    } catch (IOException e) {
      System.err.println("Erreur serveur : " + e.getMessage());
    }
  }

  // Classe interne statique gérant un client
  private static class ClientHandler implements Runnable {
    private final Socket clientSocket;

    public ClientHandler(Socket socket) {
      this.clientSocket = socket;
    }

    @Override
    public void run() {
      try (
          BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
          PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
      ) {
        Random rand = new Random();
        Code secret = new Code(rand);
        int tentatives = 0;
        boolean gagne = false;

        while (!gagne) {
          String input = in.readLine();
          if (input == null) {
            System.out.println("Client déconnecté proprement : " + clientSocket.getInetAddress());
            break; 
          }

          if (input.length() != Code.CODE_LENGTH) {
            out.println("Erreur : La combinaison doit faire 4 caractères.");
            continue;
          }

          tentatives++;
          Code guess = new Code(input.toUpperCase());

          int bienPlaces = secret.numberOfColorsWithCorrectPosition(guess);
          int malPlaces = secret.numberOfColorsWithIncorrectPosition(guess);

          out.println(bienPlaces + " " + malPlaces);

          if (bienPlaces == Code.CODE_LENGTH) {
            System.out.println("Victoire du client " + clientSocket.getInetAddress() + " !");
            System.out.println("Dernière combinaison : " + guess);
            System.out.println("Tentatives : " + tentatives);
            gagne = true;
          }
        }
      } catch (IOException e) {
        // Ignorer les connexions réinitialisées par le pair dans un stress test
        System.err.println("Erreur lors de la session client " + clientSocket.getInetAddress() + " : " + e.getMessage());
      } finally {
        try {
          clientSocket.close();
        } catch (IOException e) {
          System.err.println("Erreur à la fermeture du socket : " + e.getMessage());
        }
      }
    }
  }
}
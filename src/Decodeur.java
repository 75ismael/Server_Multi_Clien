import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Decodeur {
  public static void main(String[] args) {
    String host = "localhost";
    int port = 1234;

    try (Socket socket = new Socket(host, port);
         PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         Scanner sc = new Scanner(System.in)) {

      System.out.println("Connecté au Mastermind ! Couleurs : B, G, O, R, W, Y");
      boolean fini = false;

      while (!fini) {
        System.out.print("Votre proposition (ex: RGOB) : ");
        String proposition = sc.nextLine();

        out.println(proposition);

        // Lecture de la réponse "X Y"
        String reponse = in.readLine();
        if (reponse == null) {
          System.out.println("Le serveur a fermé la connexion.");
          break;
        }

        System.out.println("Réponse du codeur : " + reponse);

        // Analyse de la victoire
        if (reponse.equals(Code.CODE_LENGTH + " 0")) {
          System.out.println("Bravo ! Vous avez trouvé la combinaison secrète.");
          fini = true;
        }
      }
    } catch (IOException e) {
      System.err.println("Erreur de connexion : " + e.getMessage());
    }
  }
}

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Stress1 {
    public static void main(String[] args) throws InterruptedException {
        if (args.length < 1) {
            System.out.println("Usage: java Stress1 <n> [closeImmediately] [testLatence] [csv_path]");
            System.out.println("Exemples :");
            System.out.println("  java Stress1 100                 # Connecte 100 clients (ne ferment pas)");
            System.out.println("  java Stress1 100 true            # Connecte 100 clients (ferment immÃ©diatement)");
            System.out.println("  java Stress1 100 true true latence.csv # Test de latence avec 100 clients");
            return;
        }

        int n = Integer.parseInt(args[0]);
        boolean closeImmediately = args.length > 1 && Boolean.parseBoolean(args[1]);
        boolean testLatence = args.length > 2 && Boolean.parseBoolean(args[2]);
        String csvPath = args.length > 3 ? args[3] : "latence.csv";

        List<Thread> threads = new ArrayList<>();
        AtomicLong totalLatency = new AtomicLong(0);
        AtomicLong successCount = new AtomicLong(0);

        IdleClient.DataCollector collector = latencyNs -> {
            totalLatency.addAndGet(latencyNs);
            successCount.incrementAndGet();
        };

        System.out.println("Lancement de " + n + " clients...");
        for (int i = 0; i < n; i++) {
            Thread t = new Thread(new IdleClient("localhost", 1234, closeImmediately, testLatence, collector));
            threads.add(t);
            t.start();
        }

        if (testLatence) {
            // Attendre la fin de tous les threads
            for (Thread t : threads) {
                t.join(10000); // Marge maximum de 10 sec par thread
            }
            long sCount = successCount.get();
            if (sCount > 0) {
                long avgLatencyNs = totalLatency.get() / sCount;
                System.out.println("Latence moyenne pour n=" + n + " : " + avgLatencyNs + " ns ("
                        + (avgLatencyNs / 1_000_000.0) + " ms)");

                // Exporter dans le CSV
                try (PrintWriter writer = new PrintWriter(new FileWriter(csvPath, true))) {
                    writer.println(n + ";" + avgLatencyNs);
                } catch (IOException e) {
                    System.err.println("Erreur ecriture CSV : " + e.getMessage());
                }
            } else {
                System.out.println("Aucune reponse recu pour calculer la latence.");
            }

            // On quitte proprement après le test de latence
            System.exit(0);
        } else {
            System.out.println("Threads lances.");
        }
    }
}

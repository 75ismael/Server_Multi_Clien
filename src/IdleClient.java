import java.io.*;
import java.net.*;
import java.util.Random;

public class IdleClient implements Runnable {
    private final String host;
    private final int port;
    private final boolean closeImmediately;
    private final boolean recordLatence;
    private final DataCollector collector;

    public interface DataCollector {
        void addLatency(long latencyNs);
    }

    public IdleClient(String host, int port, boolean closeImmediately, boolean recordLatence, DataCollector collector) {
        this.host = host;
        this.port = port;
        this.closeImmediately = closeImmediately;
        this.recordLatence = recordLatence;
        this.collector = collector;
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket(host, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            Code randomCode = new Code(new Random());
            String combination = randomCode.toString();

            long start = 0;
            if (recordLatence) {
                start = System.nanoTime();
            }

            out.println(combination);
            String reponse = in.readLine(); // On lit la rÃ©ponse

            if (recordLatence && reponse != null) {
                long end = System.nanoTime();
                if (collector != null) {
                    collector.addLatency(end - start);
                }
            }

            if (closeImmediately) {
                socket.close();
            } else {
                // On attend indÃ©finiment pour stresser le serveur
                Thread.sleep(Long.MAX_VALUE);
            }
        } catch (IOException | InterruptedException e) {
            // Ignorer les erreurs pour le test de stress
        }
    }
}

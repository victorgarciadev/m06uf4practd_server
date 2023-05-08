package common;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import jdk.internal.org.jline.utils.Log;

/**
 *
 * @author pmorante
 */
public class SelectorParaules {

    private static final Logger log = Logger.getLogger(SelectorParaules.class.getName());

    /**
     * Retorna un llistat de paraules dependent de la dificultat
     * @param dificultat
     * @return 
     * @author Pablo Morante
     */
    public static List<String> getLlistatParaules(String dificultat) {
        List<String> ret = new ArrayList();

        try {
            if (dificultat.equals("Mig")) {
                ret = getRandomWords(5);
            } else if (dificultat.equals("Alt")) {
                ret = getRandomWords(6);
            } else {
                ret = getRandomWords(4);
            }
        } catch (Exception ex) {
            log.info("Error de connexió extern: " + ex.toString());
        }

        return ret;
    }

    /**
     * Mètode que retorna una llista de 20 paraules amb una quantitat de caràcters
     * definida per paràmetre. Funciona a través de l'API Random Word
     * @param characters
     * @return
     * @throws Exception 
     * @author Pablo Morante
     */
    public static List<String> getRandomWords(int characters) throws Exception {
        List<String> words = new ArrayList<>();

        URL url = new URL("https://random-word-api.vercel.app/api?words=20&length=" + characters);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("Error de connexió a l'API: " + responseCode);
        }

        Scanner scanner = new Scanner(url.openStream());
        while (scanner.hasNext()) {
            String word = scanner.next();
            words.add(word);
        }
        scanner.close();

        return words;
    }

}

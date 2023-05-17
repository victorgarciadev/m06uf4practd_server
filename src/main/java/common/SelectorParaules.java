package common;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

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
            switch (dificultat) {
                case "Mig":
                    ret = getRandomWords(5);
                    break;
                case "Alt":
                    ret = getRandomWords(6);
                    break;
                default:
                    ret = getRandomWords(4);
                    break;
            }
        } catch (Exception ex) {
            log.log(Level.WARNING, "Error de connexi\u00f3 extern a l''API: {0}", ex.toString());
        }

        return ret;
    }

    /**
     * M�tode que retorna una llista de 20 paraules amb una quantitat de caràcters
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
            throw new RuntimeException("Codi d'error de connexió a l'API: " + responseCode);
        }

        Scanner scanner = new Scanner(url.openStream());
        while (scanner.hasNext()) {
            String word = scanner.next();
            words.add(word);
        }
        scanner.close();
        log.log(Level.INFO, "Seleccionades correctament 20 paraules per la nova partida: \n{0}", words);

        return words;
    }

}

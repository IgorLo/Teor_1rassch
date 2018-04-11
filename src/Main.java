import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {

        Map<String, String> trueAlphabet = loadAlphabet(true);
        Map<String, String> encodedAlphabet = loadAlphabet(false);

        String[] settings = loadSettings();
        //0 - кол-во слов
        //1 - кол-во посланий
        //2 - шум (Q)

        List<String[]> multiMessage = loadMessages(Integer.parseInt(settings[0]), Integer.parseInt(settings[1]));

        String[] codedAlphabet = new String[trueAlphabet.size()];
        int counter = 0;
        for (String encodedWord: trueAlphabet.keySet()) {
            codedAlphabet[counter] = encodedWord;
            counter++;
        }

        double Q = Double.parseDouble(settings[2]);

        OneSymbolDecoder defaultDecoder = new OneSymbolDecoder();

        for (String[] strings: multiMessage) {
            defaultDecoder.update(codedAlphabet, strings, Q);
            String word = defaultDecoder.decodeWord();
            String realWord = trueAlphabet.get(word);
            System.out.print(realWord);
        }

        System.out.println();

        Map<String, Double> russianQuantities = loadRussian();
        double[] basePossibilities = calculateRussianPoss(codedAlphabet, trueAlphabet, encodedAlphabet, russianQuantities);

        OneSymbolDecoder russPossDecoder = new OneSymbolDecoder(basePossibilities);

        for (String[] strings: multiMessage) {
            russPossDecoder.update(codedAlphabet, strings, Q);
            String word = russPossDecoder.decodeWord();
            String realWord = trueAlphabet.get(word);
            System.out.print(realWord);
        }

    }

    private static double[] calculateRussianPoss(String[] codedAlphabet,
                                                 Map<String, String> trueAlphabet,
                                                 Map<String, String> encodedAlphabet,
                                                 Map<String, Double> russianQuantities) {

        double[] russPoss = new double[trueAlphabet.size()];

        for (String encodedWord: trueAlphabet.keySet()) {
            codedAlphabet[counter] = encodedWord;
            counter++;
        }

        return russPoss;
    }

    private static Map<String, Double> loadRussian() {
        Map<String, Double> russian = new HashMap<>();
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader("resources/alphabet.txt"));
            String line = reader.readLine();
            while (line != null){
                String[] args = line.split("\t");
                russian.put(args[0], Double.parseDouble(args[1]));
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return russian;
    }

    private static String[] loadSettings() {

        BufferedReader reader;
        String[] settings = null;

        try {
            reader = new BufferedReader(new FileReader("resources/settings.txt"));
            String line = reader.readLine();
            settings = line.split(" ");
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return settings;

    }

    private static List<String[]> loadMessages(int numberOfWords, int numberOfMessages) {

        List<String[]> multiMessages = new ArrayList<>();
        BufferedReader reader = null;

        for (int i = 0; i < numberOfWords; i++){
            multiMessages.add(new String[numberOfMessages]);
        }

        try {
            reader = new BufferedReader(new FileReader("resources/messages.txt"));
            String line = reader.readLine();
            for (int i = 0; i < numberOfMessages; i++){
                String[] words = line.split(" ");
                for (int j = 0; j < multiMessages.size(); j++) {
                    String[] strings = multiMessages.get(j);
                    strings[i] = words[j];
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return multiMessages;

    }

    private static Map<String,String> loadAlphabet(boolean fromEncodedToOriginal) {

        Map<String, String> alphabet = new HashMap<>();
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader("resources/alphabet.txt"));
            String line = reader.readLine();
            while (line != null){
                String[] args = line.split("\t");
                if (fromEncodedToOriginal){
                    alphabet.put(args[1], args[0]);
                } else {
                    alphabet.put(args[0], args[1]);
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return alphabet;

    }

}

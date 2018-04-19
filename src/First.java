import java.io.*;
import java.util.*;

public class First {

    public static void main(String[] args) {

        long startTime = System.currentTimeMillis();

        StringBuilder totalReport = new StringBuilder();

        SortedMap<String, String> trueAlphabet = loadAlphabet(true);


        String[] settings = loadSettings();
        //0 - кол-во слов
        //1 - кол-во посланий
        //2 - шум (Q)
        //3 - индекс символа для которого нужно сделать графики вероятностей
        //4 - индекс символа для которого нужно сделать графики Энтропии и Информации

        int printPossInfo = Integer.parseInt(settings[3]);
        int printEntroInfo = Integer.parseInt(settings[4]);

        totalReport.append(
                "Количество cлов:     " + settings[0] + "\n").append(
                "Количество посланий: " + settings[1] + "\n").append(
                "Шум (Q):             " + settings[2] + "\n\n").append(
                "Графики напечатаны для " + settings[3] + " символа." + "\n").append(
                "Энтропия и Информация пощитаны для " + settings[4] + " символа." + "\n(Нумерация с нулевого)\n"
        );

        totalReport.append("\n~~~ Расшифрованные послания ~~~\n");

        List<String[]> multiMessage = loadMessages(Integer.parseInt(settings[0]), Integer.parseInt(settings[1]));

        String[] codedAlphabet = new String[trueAlphabet.size()];
        int counter = 0;
        for (String encodedWord: trueAlphabet.keySet()) {
            //System.out.println(counter + " -> " + encodedWord + " -> " + trueAlphabet.get(encodedWord));
            codedAlphabet[counter] = encodedWord;
            counter++;
        }

        double Q = Double.parseDouble(settings[2]);

        OneSymbolDecoder defaultDecoder = new OneSymbolDecoder();

        StringBuilder realWord1 = new StringBuilder();

        //System.out.println("\n\n\n");
        System.out.println("--------------------------------------");
        System.out.println("Считая априорные вероятности равными:");
        System.out.println("--------------------------------------");

        counter = 0;
        for (String[] strings: multiMessage) {
            defaultDecoder.update(codedAlphabet, strings, Q);
            String word;
            if (printPossInfo == counter){
                System.out.println("Строю графики пункта 1.1 для символа с индексом " + printPossInfo);
                word = defaultDecoder.decodeWord(true, false);
            }
            else
                word = defaultDecoder.decodeWord(false, false);
            if (printEntroInfo == counter){
                System.out.print("Считаю Энтропию и Информацию для символа с индексом " + printEntroInfo + " (АПРИОРНЫЕ ОДИНАКОВЫЕ)");
                EntropyInfoCalculator calculator = new EntropyInfoCalculator();
                calculator.update(codedAlphabet, strings, Q);
                calculator.calculateAndSave(true, false);
            }
            String realWord = trueAlphabet.get(word);
            realWord1.append(realWord);
            counter++;
        }

        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("РАСШИФРОВАНОЕ ПОСЛАНИЕ:");
        totalReport.append(
                "\nПункт 1 с равными вероятностями:\n" + realWord1.toString() + "\n"
        );
        System.out.println(realWord1.toString());
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~");

        System.out.println();

        realWord1 = new StringBuilder();

        //System.out.println("\n\n\n");
        System.out.println("--------------------------------------");
        System.out.println("С использованием частот русских букв: ");
        System.out.println("--------------------------------------");
        //System.out.println();

        Map<String, Double> russianQuantities = loadRussian();
        double[] basePossibilities = calculateRussianPoss(codedAlphabet, trueAlphabet, russianQuantities);

        OneSymbolDecoder russPossDecoder = new OneSymbolDecoder(basePossibilities);

        counter = 0;
        for (String[] strings: multiMessage) {
            russPossDecoder.update(codedAlphabet, strings, Q);
            String word;

            if (printPossInfo == counter){
                System.out.println("Строю графики пункта 1.1 для символа с индексом " + printPossInfo);
                word = russPossDecoder.decodeWord(true, false);
            }
            else
                word = russPossDecoder.decodeWord(false, false);

            if (printEntroInfo == counter){
                System.out.print("Считаю Энтропию и Информацию для символа с индексом " + printEntroInfo + " (С УЧЁТОМ РУССКИХ ЧАСТОТ)");
                EntropyInfoCalculator calculator = new EntropyInfoCalculator();
                calculator.update(codedAlphabet, strings, Q, basePossibilities);
                calculator.calculateAndSave(false, false);
            }

            String realWord = trueAlphabet.get(word);
            realWord1.append(realWord);
            counter++;
        }

        System.out.println();
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("РАСШИФРОВАНОЕ ПОСЛАНИЕ:");
        totalReport.append(
                "\nПункт 1 с учётом частот русских букв:\n" + realWord1.toString() + "\n"
        );
        System.out.println(realWord1.toString());
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~");
        //System.out.println();


        System.out.println();
        System.out.println("2.2 - Считаем все пересылки одной длинной посылкой");

        String[] extendedAlphabet = new String[codedAlphabet.length];
        for (int i = 0; i < extendedAlphabet.length; i++){
            StringBuilder longCodedWord = new StringBuilder();
            for (int j = 0; j < Integer.parseInt(settings[1]); j++){
                longCodedWord.append(codedAlphabet[i]);
            }
            extendedAlphabet[i] = longCodedWord.toString();
            //System.out.println(extendedAlphabet[i]);
        }

        String[] extendedMessages = new String[multiMessage.size()];
        for (int i = 0; i < multiMessage.size(); i++){
            StringBuilder longMessage = new StringBuilder();
            for (int j = 0; j < Integer.parseInt(settings[1]); j++){
                longMessage.append(multiMessage.get(i)[j]);
            }
            extendedMessages[i] = longMessage.toString();
            //System.out.println(extendedMessages[i]);
        }

        OneSymbolDecoder longDefaultDecoder = new OneSymbolDecoder();

        StringBuilder longRealWord1 = new StringBuilder();

        //System.out.println("\n\n\n");
        System.out.println("--------------------------------------");
        System.out.println("Считая априорные вероятности равными:");
        System.out.println("--------------------------------------");

        counter = 0;
        for (String message: extendedMessages) {
            String[] mess = {message};
            longDefaultDecoder.update(extendedAlphabet, mess, Q);
            String word;
            if (printPossInfo == counter){
                System.out.println("Строю графики пункта 2.1 для символа с индексом " + printPossInfo);
                word = longDefaultDecoder.decodeWord(true, true);
            }
            else
                word = longDefaultDecoder.decodeWord(false, true);
            if (printEntroInfo == counter){
                System.out.print("Считаю Энтропию и Информацию для символа с индексом " + printEntroInfo + " (АПРИОРНЫЕ ОДИНАКОВЫЕ)");
                EntropyInfoCalculator calculator = new EntropyInfoCalculator();
                calculator.update(extendedAlphabet, mess, Q);
                calculator.calculateAndSave(true, true);
            }
            String realWord = trueAlphabet.get(word.substring(0, 7));
            longRealWord1.append(realWord);
            counter++;
        }

        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("РАСШИФРОВАНОЕ ПОСЛАНИЕ:");
        totalReport.append(
                "\nПункт 2 с равными вероятностями:\n" + longRealWord1.toString() + "\n"
        );
        System.out.println(longRealWord1.toString());
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~");

        System.out.println();

        longRealWord1 = new StringBuilder();

        //System.out.println("\n\n\n");
        System.out.println("--------------------------------------");
        System.out.println("С использованием частот русских букв: ");
        System.out.println("--------------------------------------");
        //System.out.println();


        OneSymbolDecoder longRussPossDecoder = new OneSymbolDecoder(basePossibilities);

        counter = 0;
        for (String message: extendedMessages) {
            String[] mess = {message};
            longRussPossDecoder.update(extendedAlphabet, mess, Q);
            String word;
            if (printPossInfo == counter){
                System.out.println("Строю графики пункта 2.1 для символа с индексом " + printPossInfo);
                word = longRussPossDecoder.decodeWord(true, true);
            }
            else
                word = longRussPossDecoder.decodeWord(false, true);
            if (printEntroInfo == counter){
                System.out.print("Считаю Энтропию и Информацию для символа с индексом " + printEntroInfo + " (АПРИОРНЫЕ ОДИНАКОВЫЕ)");
                EntropyInfoCalculator calculator = new EntropyInfoCalculator();
                calculator.update(extendedAlphabet, mess, Q);
                calculator.calculateAndSave(false, true);
            }
            String realWord = trueAlphabet.get(word.substring(0, 7));
            longRealWord1.append(realWord);
            counter++;
        }

        //System.out.println();
        //System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("РАСШИФРОВАНОЕ ПОСЛАНИЕ:");
        totalReport.append(
                "\nПункт 2 с учётом частот русских букв:\n" + longRealWord1.toString() + "\n"
        );
        System.out.println(longRealWord1.toString());
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~");
        //System.out.println();

        totalReport.append("\n~~~~~~~~~\n");
        totalReport.append("Времени на работу потрачено: " + (System.currentTimeMillis() - startTime) + " мс.\n" +
                "\nНе забудте поблагодарить Игоря");

        saveTotalReport(totalReport);

    }

    private static void saveTotalReport(StringBuilder totalReport) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new FileOutputStream(new File("Report/total/totalReport.txt")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        writer.write(totalReport.toString());
        writer.flush();
        writer.close();
    }

    private static double[] calculateRussianPoss(String[] codedAlphabet,
                                                 Map<String, String> trueAlphabet,
                                                 Map<String, Double> russianQuantities) {

        double[] letterWeights = new double[codedAlphabet.length];

        for (int i = 0; i < codedAlphabet.length; i++) {
            String actualLetterLowered = trueAlphabet.get(codedAlphabet[i]).toLowerCase();
            letterWeights[i] = russianQuantities.getOrDefault(actualLetterLowered, 2.5);
            //System.out.println(letterWeights[i]);
        }

        double weightsSum = 0.0;
        for (double weight: letterWeights) {
            weightsSum += weight;
        }

        double[] russPoss = new double[trueAlphabet.size()];

        for (int i = 0; i < russPoss.length; i++){
            russPoss[i] = letterWeights[i]/weightsSum;
        }


        //System.out.println(Arrays.toString(russPoss));

        return russPoss;
    }

    private static Map<String, Double> loadRussian() {
        Map<String, Double> russian = new HashMap<>();
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader("resources/russianPossibilities.txt"));
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

    private static SortedMap<String,String> loadAlphabet(boolean fromEncodedToOriginal) {

        SortedMap<String, String> alphabet = new TreeMap<>();
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

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class EntropyInfoCalculator {

    String[] alphabet;
    String[] words;

    double q;

    String path;

    boolean longLogic;

    double[] aprioriPoss;

    double[][] aposteriorPoss;
    double[][] realistic;
    double[] pyj;
    double[] entropy;
    double[] information;

    double midEntropy;
    double midInformation;

    public void update(String[] alphabet, String[] words, double q){
        this.alphabet = alphabet;
        this.words = words;

        this.q = q;

        aprioriPoss = new double[alphabet.length];
        for (int i = 0; i < aprioriPoss.length; i++){
            aprioriPoss[i] = 1.0/(aprioriPoss.length);
        }

        aposteriorPoss = new double[words.length][aprioriPoss.length];
        realistic = new double[words.length][aprioriPoss.length];
        pyj = new double[words.length];
        entropy = new double[words.length];
        information = new double[words.length];

    }

    public void update(String[] alphabet, String[] words, double q, double[] basePossibilities){
        this.alphabet = alphabet;
        this.words = words;

        this.q = q;

        aprioriPoss = basePossibilities;

        aposteriorPoss = new double[words.length][aprioriPoss.length];
        realistic = new double[words.length][aprioriPoss.length];
        pyj = new double[words.length];
        entropy = new double[words.length];
        information = new double[words.length];

    }

    public void calculateAndSave(boolean isDefault, boolean longLogic){

        this.longLogic = longLogic;

        path = "";

        if (longLogic)
            path += "Report/2.";
        else
            path += "Report/1.";

        if (isDefault)
            path += "2_default/";
        else
            path += "2_russian/";

        aposteriorPoss = new double[words.length][aprioriPoss.length];

        for (int i = 0; i < words.length; i++){
            aposteriorPoss[i] = calculateAposterior(words[i], i);
            entropy[i] = calculateConditionEntropy(aposteriorPoss[i]);
            information[i] = calculateConditionInformation(entropy[i], aposteriorPoss[i]);
        }

        calculatePYJ();
        midEntropy = calculateMidEntropy();
        midInformation = calculateMidInfo();
        if (longLogic)
            printLongLogicReport();
        else {
            buildReport();
            printSmallReport();
        }

    }

    private void printLongLogicReport() {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + "H&I_Отчёт.txt"), Charset.forName("UTF-8")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            writer.write("-------------------------\n");
            writer.newLine();
            writer.write("Отчёт по вычислению H и I\n");
            writer.newLine();
            writer.write("-------------------------\n");
            writer.newLine();
            writer.write("Условная энтропия:   " + entropy[0] + "\n");
            writer.newLine();
            writer.write("Условная информация: " + information[0] + "\n");
            writer.newLine();
            writer.write("Средняя энтропия:    " + midEntropy + "\n");
            writer.newLine();
            writer.write("Средняя информация:  " + midInformation + "\n");
            writer.newLine();
            writer.write("-------------------------");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void printSmallReport() {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + "H&I_Отчёт.txt"), Charset.forName("UTF-8")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            writer.write("-------------------------\n");
            writer.newLine();
            writer.write("Отчёт по вычислению H и I\n");
            writer.newLine();
            writer.write("-------------------------\n");
            writer.newLine();
            writer.write("Средняя энтропия:    " + midEntropy + "\n");
            writer.newLine();
            writer.write("Средняя информация:  " + midInformation + "\n");
            writer.newLine();
            writer.write("-------------------------");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void buildReport() {

        XYSeries seriesEntropy = new XYSeries("Энтропия");
        for (int i = 0; i < words.length; i++) {
            seriesEntropy.add(i, entropy[i]);
        }
        XYSeriesCollection datasetEntropy = new XYSeriesCollection(seriesEntropy);
        JFreeChart chartEntropy = ChartFactory.createXYBarChart(
                "Условная Энтропия",
                "Посылка",
                false,
                "Энтропия",
                datasetEntropy,
                PlotOrientation.VERTICAL,
                false, false, false);

        XYSeries seriesInfo = new XYSeries("Информация");
        for (int i = 0; i < words.length; i++) {
            seriesInfo.add(i, information[i]);
        }
        XYSeriesCollection datasetInfo = new XYSeriesCollection(seriesInfo);
        JFreeChart chartInfo = ChartFactory.createXYBarChart(
                "Условное кол-во информации",
                "Посылка",
                false,
                "Информация",
                datasetInfo,
                PlotOrientation.VERTICAL,
                false, false, false);

        try {
            ChartUtilities.saveChartAsPNG(new File(path + "Энтропия.png"), chartEntropy, 1000, 800);
            ChartUtilities.saveChartAsPNG(new File(path + "Информация.png"), chartInfo, 1000, 800);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private double calculateMidInfo() {
        double sum = 0;
        for (int i = 0; i < words.length; i++){
            sum += (pyj[i] * information[i]);
        }
        return sum;
    }

    private void calculatePYJ() {
        for (int i = 0; i < words.length; i++){
            calculatePYJforIndex(i);
        }
    }

    private void calculatePYJforIndex(int index) {
        double sum = 0;
        for (int i = 0; i < aprioriPoss.length; i++){
            sum += realistic[index][i] * aprioriPoss[i];
        }
        pyj[index] = sum;
    }

    private double calculateMidEntropy() {
        double sum = 0;
        for (int i = 0; i < words.length; i++){
            sum += (pyj[i] * entropy[i]);
        }
        return sum;
    }

    private double calculateConditionInformation(double entropy, double[] aposterior) {
        double sum = 0;
        for (int i = 0; i < aposterior.length; i++){
            sum -= (aposterior[i] * Math.log(aprioriPoss[i]));
        }
        return sum - entropy;
    }

    private double calculateConditionEntropy(double[] aposterior) {
        double sum = 0;
        for (int i = 0; i < aposterior.length; i++){
            sum -= (aposterior[i] * (Math.log(aposterior[i])/Math.log(2)));
        }
        return sum;
    }

    private double[] calculateAposterior(String word, int wordIndex) {

        realistic[wordIndex] = new double[alphabet.length];

        double[] aposteriorForWord = new double[aprioriPoss.length];


        double divider = 0.0;
        for (int i = 0; i < alphabet.length; i++){
            realistic[wordIndex][i] = calculateRealistic(alphabet[i], word);
            divider += realistic[wordIndex][i] * aprioriPoss[i];
        }

        for (int i = 0; i < alphabet.length; i++){
            aposteriorForWord[i] = realistic[wordIndex][i] * aprioriPoss[i]/divider;
        }

        return aposteriorForWord;

    }

    private double calculateRealistic(String s, String word) {
        double realistic = 1.0;
        for (int i = 0; i < word.length(); i++){
            if (s.charAt(i) == word.charAt(i))
                realistic *= (1.0 - q);
            else
                realistic *= q;
        }
        return realistic;
    }

    private String mostPossibleWord(boolean print) {
        double maxPoss = 0.0;
        int indexOfMax = 0;
        for (int i = 0; i < aprioriPoss.length; i++){
            if (aprioriPoss[i] > maxPoss){
                indexOfMax = i;
                maxPoss = aprioriPoss[i];
            }
        }
        aprioriPoss = null;
        if (print)
            System.out.println("НАИБОЛЕЕ ВЕРОЯТНЫЙ СИМВОЛ: " + alphabet[indexOfMax]);
        return alphabet[indexOfMax];
    }


}

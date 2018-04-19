import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.File;
import java.io.IOException;

public class OneSymbolDecoder {

    String[] alphabet;
    String[] words;

    double q;

    final double[] basePossibilities;

    double[] aprioriPoss;
    double[] aposteriorPoss;

    public OneSymbolDecoder(double[] basePossibilities) {
        this.basePossibilities = basePossibilities;
    }

    public OneSymbolDecoder() {
        this.basePossibilities = null;
    }

    public void update(String[] alphabet, String[] words, double q){
        this.alphabet = alphabet;
        this.words = words;

        this.q = q;

        if (basePossibilities == null){
            aprioriPoss = new double[alphabet.length];
            for (int i = 0; i < aprioriPoss.length; i++){
                aprioriPoss[i] = 1.0/(aprioriPoss.length);
            }
        } else {
            aprioriPoss = basePossibilities;
        }

        aposteriorPoss = null;
    }

    public String decodeWord(boolean print, boolean longLogic){

        if (print)
            printArrayOfPoss(aprioriPoss, "Априорные 0", longLogic);

        int counter = 1;
        for (String word: words) {
            //if (print)
                //System.out.println("Номер посылки: " + counter);
            aposteriorPoss = new double[aprioriPoss.length];
            calculateAposterior(word);
            if (print){
                printArrayOfPoss(aposteriorPoss, "Апостериорные вероятности " + counter, longLogic);
            }
            aprioriPoss = aposteriorPoss;
            counter++;
        }

        return mostPossibleWord(print);
    }

    private void printArrayOfPoss(double[] array, String name, boolean longLogic) {

        XYSeries series = new XYSeries(name);
        for (int i = 0; i < alphabet.length; i++) {
            series.add(i, Math.pow(array[i], 1/25.0));
            //series.add(i, array[i]);
        }
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYBarChart(
                "(Вероятности)^(1/25)",
                "Слова",
                false,
                "Вероятность",
                dataset,
                PlotOrientation.VERTICAL,
                false, false, false);

        String path = "";

        if (longLogic){
            path += "Report/2.";
        } else {
            path += "Report/1.";
        }

        if (basePossibilities == null){
            path += "1_default/";
        } else {
            path += "1_russian/";
        }

        try {
            ChartUtilities.saveChartAsPNG(new File(path + name + ".png"), chart, 1000, 800);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void calculateAposterior(String word) {

        double[] realisticPoss = new double[alphabet.length];
        double divider = 0.0;
        for (int i = 0; i < alphabet.length; i++){
            realisticPoss[i] = calculateRealistic(alphabet[i], word);
            divider += realisticPoss[i] * aprioriPoss[i];
        }

        for (int i = 0; i < alphabet.length; i++){
            aposteriorPoss[i] = realisticPoss[i] * aprioriPoss[i]/divider;
        }

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
        //if (print)
            //System.out.println("НАИБОЛЕЕ ВЕРОЯТНЫЙ СИМВОЛ: " + alphabet[indexOfMax]);
        return alphabet[indexOfMax];
    }


}

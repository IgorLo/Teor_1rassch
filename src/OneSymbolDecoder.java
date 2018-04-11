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

    public String decodeWord(){

        for (String word: words) {
            aposteriorPoss = new double[aprioriPoss.length];
            calculateAposterior(word);
            aprioriPoss = aposteriorPoss;
        }

        return mostPossibleWord();
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

    private String mostPossibleWord() {
        double maxPoss = 0.0;
        int indexOfMax = 0;
        for (int i = 0; i < aprioriPoss.length; i++){
            if (aprioriPoss[i] > maxPoss){
                indexOfMax = i;
                maxPoss = aprioriPoss[i];
            }
        }
        aprioriPoss = null;
        return alphabet[indexOfMax];
    }


}

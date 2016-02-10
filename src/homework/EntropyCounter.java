package homework;

/**
 * This class computes the entropy from given probabilities.
 *
 * @author Jakub Naplava
 */
public class EntropyCounter {

    private final ProbabilityContainer probs;

    public EntropyCounter(ProbabilityContainer probs) {
        this.probs = probs;
    }

    /**
     * Returns conditional entropy H(y|x).
     */
    public double getConditionalEntropy(String y, String x) {
        //System.out.println("x,y: " + x + "," + y );
        if ((probs.getBigramProbability(x, y) == 0) || (probs.getUnigramProbability(x) == 0)) {
            return 0;
        }

        return (-1) * probs.getBigramProbability(x, y) * log2(probs.getBigramConditionalProbability(y, x));
    }

    private double log2(double num) {
        return Math.log(num) / Math.log(2);
    }
}

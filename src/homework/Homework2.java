package homework;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Source code for computing cross-entropy and language modeling (Second task of
 * the homework).
 *
 * The results are this time printed into console.
 *
 * @author Jakub Naplava
 */
public class Homework2 {

    private static final String englishText = "TEXTEN1.txt";
    private static final String czechText = "TEXTCZ1.txt";

    public static void main(String[] args) throws IOException {
        performEMSmoothingAndTweak(englishText);
        performEMSmoothingAndTweak(czechText);
    }

    /**
     * This method loads given file, splits it into test, heldout and train
     * data, performs EM smoothing algorithm on the original text and then
     * tweaks lambda[3] to prove that the original results were correct (best
     * possible).
     */
    private static void performEMSmoothingAndTweak(String fileName) throws IOException {
        Text text = new Text(new FileInputStream(fileName));

        //split data
        List<String> testData = text.getWords().subList(text.getLength() - 20000, text.getLength());
        List<String> heldoutData = text.getWords().subList(text.getLength() - 60000, text.getLength() - 20000);
        List<String> trainData = text.getWords().subList(0, text.getLength() - 60000);

        Text trainDataText = new Text(trainData);
        ProbabilityContainer probs = new ProbabilityContainer(trainDataText);

        int vocabularySize = probs.getDistinctWords().size();

        //EM smoothing algorithm
        double[] lambdas = {0.7, 0.1, 0.1, 0.1};
        double error = 0.0001;

        String history2 = "<s>";
        String history1 = "<s>";
        String word;

        int iteration = 0;
        while (true) {
            iteration++;
            //System.out.println("iteration" + iteration);
            double[] expectedCounts = new double[4];

            for (int i = 0; i < heldoutData.size(); i++) {
                word = heldoutData.get(i);
                double smoothedProb = getSmoothedProbability(probs, lambdas, word, history2, history1);

                expectedCounts[3] += lambdas[3] * probs.getTrigramConditionalProbability(word, history2, history1) / smoothedProb;
                expectedCounts[2] += lambdas[2] * probs.getBigramConditionalProbability(word, history1) / smoothedProb;
                expectedCounts[1] += lambdas[1] * probs.getUnigramProbability(word) / smoothedProb;
                expectedCounts[0] += lambdas[0] * (1.0 / vocabularySize) / smoothedProb;

                history2 = history1;
                history1 = word;
            }

            //System.out.println("Expected counts:" + Arrays.toString(expectedCounts));
            double sumExpectedCounts = 0;
            for (double count : expectedCounts) {
                sumExpectedCounts += count;
            }

            double[] newLambdas = new double[4];
            for (int i = 0; i < 4; i++) {
                newLambdas[i] = expectedCounts[i] / sumExpectedCounts;
            }

            boolean terminationConditionMet = true;
            for (int i = 0; i < 4; i++) {
                if (Math.abs(lambdas[i] - newLambdas[i]) > error) {
                    terminationConditionMet = false;
                }
            }

            if (terminationConditionMet) {
                break;
            }

            lambdas = newLambdas;
            //System.out.println(Arrays.toString(lambdas));
        }

        System.out.println("final lambdas: " + Arrays.toString(lambdas));

        System.out.println("Computed cross-entropy" + getSmoothedCrossEntropy(probs, lambdas, testData));

        System.out.println("Boosting:");
        double[] boostPercentage = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.95, 0.99};

        for (double perc : boostPercentage) {
            double[] newLambdas = boostTrigramLambda(lambdas, perc);
            //System.out.println(Arrays.toString(newLambdas));
            System.out.println((perc * 100) + "%: " + getSmoothedCrossEntropy(probs, newLambdas, testData));
        }

        System.out.println("Discounting");
        double[] discountPercentages = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};

        for (double perc : discountPercentages) {
            double[] newLambdas = discountTrigramLambda(lambdas, perc);
            //System.out.println(Arrays.toString(newLambdas));
            System.out.println((perc * 100) + "%: " + getSmoothedCrossEntropy(probs, newLambdas, testData));
        }
        
        System.out.println("Coverage (all): " + getCoverage(trainData, testData));
        System.out.println("Coverage (unique): " + getCoverage(new HashSet<>(trainData), new HashSet<>(testData)));
    }

    private static double getSmoothedProbability(ProbabilityContainer probs, double[] lambdas, String word, String history2, String history1) {
        int vocabularySize = probs.getDistinctWords().size();

        return lambdas[3] * probs.getTrigramConditionalProbability(word, history2, history1)
                + lambdas[2] * probs.getBigramConditionalProbability(word, history1)
                + lambdas[1] * probs.getUnigramProbability(word)
                + lambdas[0] * (1.0 / vocabularySize);
    }

    private static double getSmoothedCrossEntropy(ProbabilityContainer probs, double[] lambdas, List<String> testData) {
        double entropy = 0;

        String history2 = "<s>";
        String history1 = "<s>";
        String word;

        for (int i = 0; i < testData.size(); i++) {
            word = testData.get(i);

            entropy += EntropyCounter.log2(getSmoothedProbability(probs, lambdas, word, history2, history1));

            history2 = history1;
            history1 = word;
        }

        return (-1) * entropy / (testData.size());
    }

    private static double[] boostTrigramLambda(double[] lambdas, double percentage) {
        double[] newLambdas = Arrays.copyOf(lambdas, 4);

        double diff = 1 - lambdas[3];
        double addition = percentage * diff;
        newLambdas[3] += addition;

        //proportional discount
        double sumLambdas = 0;
        for (int i = 0; i < 3; i++) {
            sumLambdas += lambdas[i];
        }

        for (int i = 0; i < 3; i++) {
            newLambdas[i] -= (addition * lambdas[i]) / sumLambdas;
        }

        return newLambdas;
    }

    private static double[] discountTrigramLambda(double[] lambdas, double percentage) {
        double[] newLambdas = Arrays.copyOf(lambdas, 4);

        newLambdas[3] = percentage * lambdas[3];
        double diff = lambdas[3] - newLambdas[3];

        //proportional discount
        double sumLambdas = 0;
        for (int i = 0; i < 3; i++) {
            sumLambdas += lambdas[i];
        }

        for (int i = 0; i < 3; i++) {
            newLambdas[i] += (diff * lambdas[i]) / sumLambdas;
        }

        return newLambdas;
    }
    
    /**
     * Returns 'coverage' - percentage of words from 'test' that are in 'train'. 
     */
    private static double getCoverage(Collection<String> train, Collection<String> test) {
        int count = 0;
        for (String t : test) {
            if (train.contains(t)) count++;
        }
        
        return ((double) count) / test.size();
    }
}

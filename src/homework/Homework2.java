package homework;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Source code for computing cross-entropy and language modeling (Second task of
 * the homework).
 *
 * The results are printed into results2.html file, which contains all most
 * important information computed.
 *
 * @author Jakub Naplava
 */
public class Homework2 {

    private static final String englishText = "TEXTEN1.txt";
    private static final String czechText = "TEXTCZ1.txt";

    public static void main(String[] args) throws IOException {

        Text text = new Text(new FileInputStream(englishText));

        List<String> testData = text.getWords().subList(text.getLength() - 20000, text.getLength());
        List<String> heldoutData = text.getWords().subList(text.getLength() - 60000, text.getLength() - 20000);
        List<String> trainData = text.getWords().subList(0, text.getLength() - 60000);

//        System.out.println(testData.size());
//        System.out.println(heldoutData.size());
//        System.out.println(trainData.size());
        Text trainDataText = new Text(trainData);
        ProbabilityContainer probs = new ProbabilityContainer(trainDataText);
        
        //EM smoothing algorithm
        double[] lambdas = {0.7, 0.1, 0.1, 0.1};
        //System.out.println(Arrays.toString(lambdas));
        double error = 0.0001;

        String history2 = "<s>";
        String history1 = "<s>";
        String word;

        while (true) {
            double[] expectedCounts = new double[4];
            
            for (int i = 0; i < heldoutData.size(); i++) {
                word = heldoutData.get(i);
                double smoothedProb = getSmoothedProbability(probs, lambdas, word, history2, history1);
                
                double a = probs.getTrigramConditionalProbability(word, history2, history1);
                expectedCounts[3] += lambdas[3]*
                        a / smoothedProb;
                expectedCounts[2] += lambdas[2]*
                        probs.getBigramConditionalProbability(word, history1) / smoothedProb;
                expectedCounts[1] += lambdas[1]*
                        probs.getUnigramProbability(word) / smoothedProb;
                expectedCounts[0] += lambdas[0]*
                        (1.0 / probs.getNumberOfWords()) / smoothedProb;
                
                history2 = history1;
                history1 = word;
            }
            
            //System.out.println("Expected counts:" + Arrays.toString(expectedCounts));
            
            double sumExpectedCounts = 0;
            for (double count : expectedCounts) sumExpectedCounts += count;
            
            double[] newLambdas = new double[4];
            for (int i = 0; i < 4; i++) {
                newLambdas[i] = expectedCounts[i] / sumExpectedCounts;
            }
                        
            boolean terminationConditionMet = true;
            for (int i = 0; i < 4; i++) {
                if (Math.abs(lambdas[i] - newLambdas[i]) > error) terminationConditionMet = false;
            }
            
            if (terminationConditionMet) break;
            
            lambdas = newLambdas;
            System.out.println(Arrays.toString(lambdas));
        }
        
        System.out.println("final lambdas: " + Arrays.toString(lambdas));
    }
    
    private static double getSmoothedProbability(ProbabilityContainer probs, double[] lambdas, String word, String history2, String history1) {
        return lambdas[3] * probs.getTrigramConditionalProbability(word, history2, history1)
                + lambdas[2] * probs.getBigramConditionalProbability(word, history1) 
                + lambdas[1] * probs.getUnigramProbability(word)
                + lambdas[0] * (1.0/probs.getNumberOfWords());
    }
}


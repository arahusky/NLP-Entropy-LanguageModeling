package homework;

import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

/**
 * This class contains all computed probabilities (e.g. unigram, bigram
 * probabilities).
 *
 * @author Jakub Naplava
 */
public class ProbabilityContainer {

    //HashMap keeping for every triplet (three words) its number of occurence
    private final HashMap<Triplet, Integer> tripletCounts;
    
    //HashMap keeping for every pair its number of occurence
    private final HashMap<Pair, Integer> pairCounts;
    
    //HashMap keeping for every words its number of occurence
    private final HashMap<String, Integer> wordCounts;

    //number of all words (=pairs, =triplets) in the text.
    //double type is just a small trick to make all divisions non-integer
    private final double numberOfWords;

    public ProbabilityContainer(Text text) {
        tripletCounts = new HashMap<>();
        pairCounts = new HashMap<>();
        wordCounts = new HashMap<>();

        numberOfWords = text.getLength();

        Triplet window = new Triplet();
        for (int i = 0; i < text.getLength(); i++) {
            window.push(text.get(i));

            int count = 0;
            if (tripletCounts.containsKey(window)) {
                count = tripletCounts.get(window);
            }
            tripletCounts.put(window, count + 1);

            Pair pair = new Pair(window.second, window.third);
            count = 0;
            if (pairCounts.containsKey(pair)) {
                count = pairCounts.get(pair);
            }
            pairCounts.put(pair, count + 1);

            String word = window.third;

            count = 0;
            if (wordCounts.containsKey(word)) {
                count = wordCounts.get(word);
            }
            wordCounts.put(word, count + 1);
        }
    }

    /**
     * Returns probability of given word, i.e. P(word).
     */
    public double getUnigramProbability(String word) {
        int wordCount = wordCounts.getOrDefault(word, 0);
        return wordCount / numberOfWords;
    }

    /**
     * Returns disjoint probability P(x,y).
     */
    public double getBigramProbability(String x, String y) {
        Pair pair = new Pair(x, y);
        int pairCount = pairCounts.getOrDefault(pair, 0);

        return pairCount / numberOfWords;
    }

    /**
     * Returns conditional probability P(y|x).
     */
    public double getBigramConditionalProbability(String y, String x) {
        //P(y | x) = P(x,y) / P(x)
        if (getUnigramProbability(x) == 0) {
            return 0;
        }
        
        return getBigramProbability(x, y) / getUnigramProbability(x);
    }

    /**
     * Returns disjoint probability P(x,y,z).
     */
    public double getTrigramProbability(String x, String y, String z) {
        Triplet triplet = new Triplet(x, y, z);
        int tripletCount = tripletCounts.getOrDefault(triplet, 0);

        return tripletCount / numberOfWords;
    }

    /**
     * Return conditional probability P(z|x,y).
     */
    public double getTrigramConditionalProbability(String z, String x, String y) {
        //P(z|x,y) = P(x,y,z) / P(x,y)
        if (getBigramProbability(x,y) == 0) {
            return 0;
        }
        
        return getTrigramProbability(x, y, z) / getBigramProbability(x,y);
    }

    /**
     * Returns number of all words (=pairs, =triplets) in the text.
     */
    public double getNumberOfWords() {
        return numberOfWords;
    }

    
    /**
     * Returns all words used in the text.
     */
    public Set<String> getDistinctWords() {
        return wordCounts.keySet();
    }

    /**
     * Returns all pairs used in the text.
     */
    public Set<Pair> getDistinctPairs() {
        return pairCounts.keySet();
    }

    public class Triplet {

        private String first;
        private String second;
        private String third;

        Triplet() {
            second = "<s>";
            third = "<s>";
        }

        public Triplet(String first, String second, String third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }               

        void push(String word) {
            first = second;
            second = third;
            third = word;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 53 * hash + Objects.hashCode(this.first);
            hash = 53 * hash + Objects.hashCode(this.second);
            hash = 53 * hash + Objects.hashCode(this.third);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Triplet other = (Triplet) obj;
            if (!Objects.equals(this.first, other.first)) {
                return false;
            }
            if (!Objects.equals(this.second, other.second)) {
                return false;
            }
            if (!Objects.equals(this.third, other.third)) {
                return false;
            }
            return true;
        }

    }
}

package homework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class encapsulates loaded text and provides methods for messing it up.
 *
 * @author Jakub Naplava
 */
public class Text {

    //the loaded text is parsed into this structure, where each item is one word (row from original file).
    private final List<String> words;
    private final SortedSet<Character> distinctCharacters;
    private final SortedSet<String> distinctWords;

    private static Random random = new Random(666);

    public Text(List<String> words) {
        this.words = words;
        distinctCharacters = new TreeSet<>();
        distinctWords = new TreeSet<>();

        for (String word : words) {
            distinctWords.add(word);

            for (int i = 0; i < word.length(); i++) {
                distinctCharacters.add(word.charAt(i));
            }
        }
    }

    public Text(List<String> words, SortedSet<Character> distinctCharacters, SortedSet<String> distinctWords) {
        this.words = words;
        this.distinctCharacters = distinctCharacters;
        this.distinctWords = distinctWords;
    }

    public Text(InputStream input) throws IOException {
        words = new ArrayList<>();
        distinctCharacters = new TreeSet<>();
        distinctWords = new TreeSet<>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(input, "ISO8859_2"));
        String line;
        while (((line = reader.readLine()) != null)) {
            words.add(line);
            distinctWords.add(line);

            for (int i = 0; i < line.length(); i++) {
                distinctCharacters.add(line.charAt(i));
            }
        }
    }

    public String get(int i) {
        return words.get(i);
    }

    public int getLength() {
        return words.size();
    }

    public List<String> getWords() {
        return words;
    }

    /**
     * Mess up given text.
     *
     * @param wordProb Probability to mess up each word.
     * @param charProb Probability to mess up each character.
     */
    public void messUp(double wordProb, double charProb) {
        for (int i = 0; i < getLength(); i++) {
            String word = get(i);

            //if we should change whole word
            if (random.nextDouble() < wordProb) {
                //choose random word from the set of all words and replace the old one with it
                word = (String) distinctWords.toArray()[random.nextInt(distinctWords.size())];
                words.set(i, word);
            }

            StringBuilder sb = new StringBuilder(word);
            for (int characterPosition = 0; characterPosition < word.length(); characterPosition++) {
                //if the current character should be changed
                if (random.nextDouble() < charProb) {
                    //choose random character from the set of all characters and use it to replace the current one
                    char newChar = (char) distinctCharacters.toArray()[random.nextInt(distinctCharacters.size())];
                    sb.setCharAt(characterPosition, newChar);
                }
            }

            words.set(i, sb.toString());
        }
    }

    public TextStatistics getTextStatistics() {
        return new TextStatistics();
    }

    @Override
    public Text clone() {

        //the words may change and we want to keep the original one unchanged
        List<String> newWords = (ArrayList<String>) ((ArrayList<String>) words).clone();

        return new Text(newWords, distinctCharacters, distinctWords);
    }

    /**
     * This class provides basic statistics (characteristics) of the text.
     *
     * The purpose of this class for the homework was to have better insight
     * into difference of the Czech and the English text (mostly its entropies).
     */
    public class TextStatistics {

        private int wordCount;
        private int totalCharacters;

        //map storing for each word its number of occurrences
        private HashMap<String, Integer> wordFrequency;

        public TextStatistics() {
            computeCharacteristics();
        }

        private void computeCharacteristics() {
            wordCount = words.size();
            wordFrequency = new HashMap<>();

            for (String word : words) {
                totalCharacters += word.length();

                int wcount = 0;
                if (wordFrequency.containsKey(word)) {
                    wcount = wordFrequency.get(word);
                }

                wordFrequency.put(word, wcount + 1);
            }
        }

        public int getWordCount() {
            return wordCount;
        }

        public int getTotalCharacters() {
            return totalCharacters;
        }

        public double getAvgCharacterPerWord() {
            return ((double) totalCharacters) / wordCount;
        }

        public int getNumUniqueWords() {
            return wordFrequency.size();
        }

        /**
         * Returns number of words with given occurrence.
         */
        public int getNumWordsWithGivenFrequency(int numberOfOccurrences) {            
            int num = 0;            
            for (int i : wordFrequency.values()) {
                if (i == numberOfOccurrences) num++;
            }
            
            return num;
        }
    }
}

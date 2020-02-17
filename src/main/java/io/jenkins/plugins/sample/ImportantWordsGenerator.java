package io.jenkins.plugins.sample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.SimpleTokenizer;
import org.apache.commons.lang.StringUtils;

/**
* ImportantWordsGenerator creates a map of words,
* not in the stop-words list, that are stemmed
* and broken into base words, along with the 
* number of times they occurr in a given string.
*/
public class ImportantWordsGenerator {

  // Static instance variable
  public static ImportantWordsGenerator INSTANCE = null;

  // Path to stop words file
  public static final String STOP_WORD_FILE_PATH = 
      System.getProperty("user.dir") + File.separator + "stop_words.txt";
  // The number of lines to read at once
  private static final int READ_LINES = 50;

  private static HashSet<String> stopWords;

  private final SimpleTokenizer tokenizer;
  private final PorterStemmer stemmer;

  private ImportantWordsGenerator() {
    tokenizer = SimpleTokenizer.INSTANCE;
    stemmer = new PorterStemmer();

    // Initialize stopWords and fill it with words from STOP_WORD_FILE_PATH
    stopWords = new HashSet<String>();
    try (BufferedReader stpWrdReader = new BufferedReader(new InputStreamReader(
	    new FileInputStream(STOP_WORD_FILE_PATH), "UTF-8"))) {
      System.err.println("Stop Words File: " + STOP_WORD_FILE_PATH);
      String currStpWrd;
      // One stop word per line
      while ((currStpWrd = stpWrdReader.readLine()) != null) {
        stopWords.add(currStpWrd);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
  * Gets the instance of ImportantWordsGenerator, creates it if null.
  *
  * @return  Instance of ImportantWordsGenerator	
  */
  public static ImportantWordsGenerator getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ImportantWordsGenerator();
    } 
    return INSTANCE;
  }
  

  /**
  * Creates a map of unique words and the number of
  * occurrences from the input string.
  *
  * @param input  the string to be processed
  * @return  a map of words and occurrences
  */
  public Map<String, Integer> generate(String input) {
    // Map of unique words with occurrence
    Map<String, Integer> occurrences = new HashMap<String, Integer>();
    return extendGeneration(input, occurrences);
  }

  /**
  * Creates a map of unique words and the number of
  * occurrecnes contained within the input file. 
  * Reades READ_LINES number of lines from the file
  * at a time and processes them.
  *
  * @param file	 the file to be scanned
  * @return  a map of words and occurrences	
  */
  public Map<String, Integer> generate(File file) {
    Map<String, Integer> occurrences = new HashMap<String, Integer>();
    String currLine;
    StringBuilder fileTextChunk = new StringBuilder();
    int lineNum = 0;
    try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(
	    new FileInputStream(file), "UTF-8"))) {
      while ((currLine = fileReader.readLine()) != null) {
        fileTextChunk.append(currLine);
        lineNum += 1;
        // If lineNum is multiple of READ_LINES then process chunk
        if (lineNum % READ_LINES == 0) {
          extendGeneration(fileTextChunk.toString(), occurrences);
          // Reset the StringBuilder buffer to 0
          fileTextChunk.setLength(0);
        }
      }
      if (fileTextChunk.length() > 0) {
        // Extend the current occurrences with important words from the final chunk
        extendGeneration(fileTextChunk.toString(), occurrences);
      }
    } catch (IOException e) {
      System.err.println("Unable to track " + file.getAbsolutePath());
    } 
    return occurrences; 
  }
  

  /**
  * Appends the unique words and number of occurrences
  * to the given map.
  *
  * @param input  the string to be processed
  * @param occurrences  map of words and number of occurrences
  * @return  a map of words and occurrences
  */
  public Map<String, Integer> extendGeneration(String input, Map<String, Integer> occurrences) {
    // Convert the input into tokens
    String[] tokens = tokenizer.tokenize(input);

    for (String token : tokens) {
      // Split a token by Capital letters
      String[] subTokens = StringUtils.splitByCharacterTypeCamelCase(token);
      for (String subToken : subTokens) {
        subToken = subToken.toLowerCase();
        // If the subToken isn't a stopword, then add it to occurrences
        if (!stopWords.contains(subToken)) {
          String stemmedToken = stemmer.stem(subToken);
          // add one to the number of occurences or set to 1 if doesn't exist
          occurrences.put(stemmedToken, occurrences.getOrDefault(stemmedToken, 0) + 1);
        }
      }
      // Add the original token if it was broken up
      if (subTokens.length > 1) {
        token = token.toLowerCase();
        if (!stopWords.contains(token)) {
          String stemmedToken = stemmer.stem(token);
          // add one to the number of occurences or set to 1 if doesn't exist
          occurrences.put(stemmedToken, occurrences.getOrDefault(stemmedToken, 0) + 1);
        }
      }
    }
    return occurrences;
  }
}

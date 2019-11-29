package io.jenkins.plugins.sample;

import java.io.*;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.stemmer.PorterStemmer;
import org.apache.commons.lang.StringUtils;

public class ImportantWordsGenerator {

    //Static instance variable
    public static ImportantWordsGenerator INSTANCE = new ImportantWordsGenerator();

    //Path to stop words file
    public static String STOPWORDSFILE;

    private static HashSet<String> stopWords;

    private final SimpleTokenizer tokenizer;
    private final PorterStemmer stemmer;

    private ImportantWordsGenerator() {
	String fileSep = File.separator;
	STOPWORDSFILE = System.getProperty("user.home") + fileSep + ".BugRanking" + fileSep + "stop_words.txt";
	
	tokenizer = SimpleTokenizer.INSTANCE;
	stemmer = new PorterStemmer();

	//Initialize stopWords and fill it with words from STOPWORDSFILE
	stopWords = new HashSet<String>();
	BufferedReader stpWrdReader = null;
	try {
	    System.err.println(System.getProperty("user.home"));
	    System.err.println("Stop Words File: " + STOPWORDSFILE);
	    String currStpWrd;
	    stpWrdReader = new BufferedReader(new FileReader(STOPWORDSFILE));
	    //One stop word per line
	    while ((currStpWrd = stpWrdReader.readLine()) != null) {
		stopWords.add(currStpWrd);
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	} finally {
	    //Close the BufferedReader
	    try {
		if (stpWrdReader != null) {
		    stpWrdReader.close();
		}
	    } catch (IOException ex) {
		ex.printStackTrace();
	    }
	}

    }
    
    public Map<String, Integer> generate(String input) {
	//Map of unique words with occurrence
	Map<String, Integer> occurrences = new HashMap<String, Integer>();
	return extendGeneration(input, occurrences);	
    }

    public Map<String, Integer> extendGeneration(String input, Map<String, Integer> occurrences) {
	//Takes a string input and a map of occurrences
	//Occurrences is a map of unique words with their number of occurrences
	//Convert the input into tokens
	String tokens[] = tokenizer.tokenize(input);
    
	for (String token: tokens){
	    //Split a token by Capital letters
	    String[] subTokens = StringUtils.splitByCharacterTypeCamelCase(token);
	    for (String subToken: subTokens) {
		subToken = subToken.toLowerCase();
		//If the subToken isn't a stopword, then add it to occurrences
		if (!stopWords.contains(subToken)) {
		    //stem the word
		    String stemmedToken = stemmer.stem(subToken);
		    //add one to the number of occurences or set to 1 if doesn't exist
		    occurrences.put(stemmedToken, occurrences.getOrDefault(stemmedToken, 0) + 1);
		}
	    }
	    //Add the original token if it was broken up
	    if (subTokens.length > 1) {
		token = token.toLowerCase();
		//If the token isn't a stopword, then add it to result
		if (!stopWords.contains(token)) {
		    //stem the word
		    String stemmedToken = stemmer.stem(token);
		    //add one to the number of occurences or set to 1 if doesn't exist
		    occurrences.put(stemmedToken, occurrences.getOrDefault(stemmedToken, 0) + 1);
		}
	    }
	    
	}
	return occurrences;	
    }

}

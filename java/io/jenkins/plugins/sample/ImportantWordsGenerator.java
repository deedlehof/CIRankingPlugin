package io.jenkins.plugins.sample;

import java.io.*;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Properties;
import java.lang.StringBuilder;
import java.lang.System;

import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.stemmer.PorterStemmer;
import org.apache.commons.lang.StringUtils;

public class ImportantWordsGenerator {

    //Static instance variable
    public static ImportantWordsGenerator INSTANCE = new ImportantWordsGenerator();

    //Path to stop words file
    public static String STOPWORDSFILE;
    //Important word seperator
    public static final String WORDSEPARATOR = " ";

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

    public String generate(String input) {
	//Convert the input into tokens
	String tokens[] = tokenizer.tokenize(input);
	//Result string builder
	StringBuilder result = new StringBuilder();
    

	for (String token: tokens){
	    //Split a token by Capital letters
	    String[] subTokens = StringUtils.splitByCharacterTypeCamelCase(token);
	    for (String subToken: subTokens) {
		//If the subToken isn't a stopword, then add it to result
		if (!stopWords.contains(subToken)) {
		    //
		    result.append(stemmer.stem(subToken));
		    result.append(WORDSEPARATOR);
		}
	    }
	    //Add the original token if it was broken up
	    if (subTokens.length > 1) {
		//If the token isn't a stopword, then add it to result
		if (!stopWords.contains(token)) {
		    result.append(stemmer.stem(token));
		    result.append(WORDSEPARATOR);
		}
	    }
	    
	}
	return result.toString();	
    }

}

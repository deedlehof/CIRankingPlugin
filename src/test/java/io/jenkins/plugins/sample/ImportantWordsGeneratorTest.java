package io.jenkins.plugins.sample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.StringBuilder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import opennlp.tools.stemmer.PorterStemmer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

public class ImportantWordsGeneratorTest { 
 
  public static ImportantWordsGenerator wordGen;
  public static Set<String> stopWords;
  public static Set<String> stemmedStopWords;
  public static PorterStemmer stemmer;
   
  @BeforeClass
  public static void setUpClass() {
    wordGen = ImportantWordsGenerator.getInstance();
    stemmer = new PorterStemmer();
    stopWords = new HashSet<String>();
    stemmedStopWords = new HashSet<String>();

    String stpWrdFilePath = wordGen.STOP_WORD_FILE_PATH;
    try (BufferedReader stpWrdReader = new BufferedReader(new FileReader(stpWrdFilePath))) {
      String stpWrd;
      while ((stpWrd = stpWrdReader.readLine()) != null) {
        stopWords.add(stpWrd);
	stemmedStopWords.add(stemmer.stem(stpWrd));
      } 
    } catch (IOException e) {
      e.printStackTrace(); 
    } 
  } 

  @Test
  public void testGenerateStpWrdRemoval() {
    StringBuilder input = new StringBuilder("Tanner "); 
    for (String stpWrd : stopWords) {
      input.append(stpWrd);
      input.append(' ');
    }
    input.append("Coffman");
    Map<String, Integer> occs = wordGen.generate(input.toString());
    if (occs.size() != 2) {
      Assert.fail("Word Generator didn't remove all stop words");
    } 
    if (!occs.containsKey(stemmer.stem("tanner")) || !occs.containsKey(stemmer.stem("coffman"))) {
      Assert.fail("Word Generator removed incorrect words");
    } 
  } 

  @Test
  public void testStringGenerate() {
    String input = "This is a test string full of possible words as well as camelCase words";
    Map<String, Integer> expected = new HashMap<String, Integer>();
    expected.put("thi", 1);
    expected.put("test", 1);
    expected.put("camel", 1);
    expected.put("well", 1);
    expected.put("possibl", 1);
    expected.put("word", 2);
    expected.put("camelcas", 1);
    expected.put("case", 1);
    expected.put("full", 1);
    Map<String, Integer> occs = wordGen.generate(input);
    if (!occs.equals(expected)) {
      Assert.fail("String Generate created incorrect mappings");   
    } 
  } 

  @Test
  public void testFileGenerate() {
    File input = new File(getClass().getResource("ImportantWordGen/file.txt").getFile());
    Map<String, Integer> expected = new HashMap<String, Integer>();
    expected.put("thi", 1);
    expected.put("test", 1);
    expected.put("camel", 1);
    expected.put("well", 1);
    expected.put("possibl", 1);
    expected.put("word", 2);
    expected.put("camelcas", 1);
    expected.put("case", 1);
    expected.put("full", 1);
    Map<String, Integer> occs = wordGen.generate(input);
    if (!occs.equals(expected)) {
      Assert.fail("File Generate created incorrect mappings");   
    } 
  }

  @Test
  public void testExtendGenerate() {
    Map<String, Integer> expected = new HashMap<String, Integer>();
    expected.put("thi", 2);
    expected.put("test", 2);
    expected.put("camel", 2);
    expected.put("well", 1);
    expected.put("possibl", 1);
    expected.put("word", 2);
    expected.put("camelcas", 1);
    expected.put("case", 1);
    expected.put("full", 1);
    Map<String, Integer> occs = new HashMap<String, Integer>();
    occs.put("thi", 1);
    occs.put("test", 1);
    occs.put("camel", 1);
    String input = "This is a test string full of possible words as well as camelCase words";
    wordGen.extendGeneration(input, occs);
    if (!occs.equals(expected)) {
      Assert.fail("Extending Generation created incorrect mappings");   
    } 
  } 
  
}

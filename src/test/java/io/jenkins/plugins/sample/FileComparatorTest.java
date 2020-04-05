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

public class FileComparatorTest { 
 
  @Test
  public void testTrackFile() {
    FileComparator fc = new FileComparator("test");
    File f = new File(getClass().getResource("FileComparator/trackTest.txt").getFile());
    fc.trackFile(f);
    // Check that the tracker contains the added file
    // Remove leading file seperator from path to match fc storing conventions 
    if (!fc.getTrackedFiles().contains(f.toString().substring(1))) {
      Assert.fail("File Comparitor file tracking failed");
    } 
  }
  
  @Test
  public void testTrackDirectory() {
    FileComparator fc = new FileComparator("test");
    File root = new File(getClass().getResource("FileComparator").getFile());
    File file1 = new File(getClass().getResource("FileComparator/trackTest.txt").getFile());
    File file2 = new File(getClass().getResource("FileComparator/testDir/dirFile.txt").getFile());
    fc.trackDirectory(root);
    // Check that the tracker contains the added file
    // Remove leading file seperator from path to match fc storing conventions 
    if (!fc.getTrackedFiles().contains(file1.toString().substring(1))) {
      Assert.fail("Track directory failed to track root file");
    } 

    if (!fc.getTrackedFiles().contains(file2.toString().substring(1))) {
      Assert.fail("Track directory failed to track nested file");
    } 
  }

  @Test
  public void testCompare() {
    FileComparator fc = new FileComparator("test");
    File root = new File(getClass().getResource("FileComparator").getFile());
    File file1 = new File(getClass().getResource("FileComparator/trackTest.txt").getFile());
    File file2 = new File(getClass().getResource("FileComparator/testDir/dirFile.txt").getFile());
    fc.trackDirectory(root);
    String testStr = "This is a file to test FileComparator tracking.";
    Map<String, Double> scores = fc.compare(testStr, 5);
    // Round the score for the complete match file to 2 decimal places
    double fileVal = Math.round(scores.get(file1.toString().substring(1)) * 100.0) / 100.0;
    if (fileVal != 1.00) {
      Assert.fail("Value was " + fileVal);
    } 
  }
}

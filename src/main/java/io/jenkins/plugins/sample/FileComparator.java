package io.jenkins.plugins.sample;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream; 
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader; 
import java.io.OutputStreamWriter; 
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * FileComparitor is the class for tracking source files and generating the top matching files for a
 * given input string. All tracked files are given a cached file of the count of their important
 * words.
 */
public class FileComparator {

  public static final String FILE_EXT = ".occ";
  private static final String SEP_REPLACER = "%";

  public final String cacheDirectory;
  private ImportantWordsGenerator importantWrdGen = ImportantWordsGenerator.getInstance();
  // Contains all tracked files and their lastModified date
  private Map<String, Long> trackedFiles = new HashMap<String, Long>();

  /**
   * Creates a new FileComparator that caches the important words for all of the files found in
   * directory. Creates the caching directory if it doesn't exist.
   *
   * @param directory the location of the files to be tracked
   */
  public FileComparator(String directory) {
    cacheDirectory =
        System.getProperty("user.dir") + File.separator + "Cache" + File.separator + directory;
    // Check if the caching directory exists
    // If it does then intialize trackedFiles
    // If not then create the directory
    File cacheDirFile = new File(cacheDirectory);
    if (cacheDirFile.exists() && cacheDirFile.isDirectory()) {
      initializeTracker(cacheDirectory);
    } else {
      System.err.println("Creating caching directory...");
      System.err.println("MADE DIR: " + cacheDirFile.mkdirs());
    }
  }

  /**
   * Tracks all of the files within directory, recursively including all files in subdirectories.
   * All files in the directory and it's subdirectories are tracked. A .occ cache file is created
   * for each of the files and placed in the caching directory.
   *
   * @param directory the location of the files to be tracked
   */
  public void trackDirectory(String directory) {
    // Tracks all of the files within the directory (absolute path)
    // Recursive, includes all files in subdirectories
    // Creates .occ files for each
    File folder = new File(directory);
    trackDirectory(folder);
  }

  /**
   * Given a File object, tracks all of the files within directory, recursively including all files
   * in subdirectories. All files in the directory and it's subdirectories are tracked. A .occ cache
   * file is created for each of the files and placed in the caching directory.
   *
   * @param folder a File directory containing files to be tracked
   */
  public void trackDirectory(File folder) {
    // Tracks all of the files within the directory
    // Recursive, includes all files in subdirectories
    // Creates .occ files for each
    if (!folder.isDirectory()) {
      return;
    }
    File[] files = folder.listFiles();

    for (File file : files) {
      if (file.isFile()) {
        // Check if file is already tracked and updated
        // If not then update it
        if (trackedFiles.getOrDefault(fileToOccName(file.getAbsolutePath()), 0L)
            < file.lastModified()) {
          trackFile(file);
        }
      } else if (file.isDirectory()) {
        trackDirectory(file);
      }
    }
  }

  /**
   * Creates a .occ file of important words in the cache directory for the given file. Reads the
   * file at directory piece by piece and takes a count of the important words. Once the entirety of
   * the file is read it generates the .occ file containing the words and number of times they
   * occur.
   *
   * @param directory path to tracked file
   */
  public void trackFile(String directory) {
    File file = new File(directory);
    trackFile(file);
  }

  /**
   * Creates a .occ file of important words in the cache directory for the given file. Reads the
   * file passed piece by piece and takes a count of the important words. Once the entirety of the
   * file is read it generates the .occ file containing the words and number of times they occur.
   *
   * @param file file object to be tracked
   */
  public void trackFile(File file) {
    // If file doesn't exist, stop
    if (!file.exists()) {
      return;
    }
    // Get the important words from file
    Map<String, Integer> occurrences = importantWrdGen.generate(file);
    // Write the occurrences to a .occ file
    // Replace all file separators with something that won't be
    // interpreted as such
    String occName = fileToOccName(file.getAbsolutePath());
    String completePath = cacheDirectory + File.separator + occName;
    try (BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(
	    new FileOutputStream(completePath), "UTF-8"))) {
      // Write all unique occurrences and number to file
      for (Map.Entry<String, Integer> occurrence : occurrences.entrySet()) {
        fileWriter.write(occurrence.getKey() + " " + occurrence.getValue());
        fileWriter.newLine();
      }
    } catch (IOException e) {
      System.err.println("Unable to cache occurrences for " + file.getAbsolutePath());
    } 
    // Add file to tracking list
    trackedFiles.put(occName, file.lastModified());
  }

  /**
   * Compares the input string to all tracked files,
   * returning the top results number of matches.
   * The input text is converted into a map
   * of important words and the cosine similarity
   * between that map and all of the tracked files
   * is calculated.  The names of the top matching 
   * files are returned. 
   *
   * @param text the text to compare to the tracked files
   * @param results the number of matching files to return
   * @return a ScoreBoard object containing the top matches
   */
  public Map<String, Double> compare(String text, int results) {
    Map<String, Double> fileScores = new LinkedHashMap<>();
    // Calculate score
    surfaceSimilarity(fileScores, text);
    // Sort the map by converting it to a list 
    // List sorting has O(n * log n) time complexity
    List<Entry<String, Double>> scoresList = new ArrayList<>(fileScores.entrySet());
    //emptry map
    fileScores.clear();
    scoresList.sort(Entry.comparingByValue());
    for (int i = scoresList.size() - 1; i >= scoresList.size() - results; i -= 1) {
      Entry<String, Double> entry = scoresList.get(i);
      fileScores.put(entry.getKey(), entry.getValue());
    }
    return fileScores;
  }

  private void updateOccIfOutdated(String name) {
    // Update .occ file is its been modified since last tracking
    File originalFile = new File(occToFileName(name));
    if (trackedFiles.getOrDefault(name, 0L) < originalFile.lastModified()) {
      trackFile(originalFile);
    }
  }

  private void surfaceSimilarity(Map<String, Double> fileScores, String text) {
    // Compare each .occ file to text and return top results number results
    // Comparison using cosine similarity
    // (A * B) / (|A| * |B|)

    Map<String, Integer> textImpWrds = importantWrdGen.generate(text);
    // Generate the magnitude of the given text
    int textOccNorm = 0;
    for (int val : textImpWrds.values()) {
      textOccNorm += val * val;
    }
    // Magnitude of text occurrence map
    double textOccMag = Math.sqrt(textOccNorm); // A

    // Read from all .occ files
    // Generate comparison score against text
    File folder = new File(cacheDirectory);
    File[] files = folder.listFiles();
    for (File file : files) {
      // Update .occ file is its been modified since last tracking
      updateOccIfOutdated(file.getName());
      try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(
	      new FileInputStream(file), "UTF-8"))) {
        // The magnitude of the current occ file
        int occFileNorm = 0; // B
        int dotProduct = 0;
        String currLine;
        int wrdOccurrence;
        // Each line contains one word and its occurrence number
        while ((currLine = fileReader.readLine()) != null) {
          String[] parts = currLine.split(" ");
          try {
            wrdOccurrence = Integer.parseInt(parts[1]);
          } catch (NumberFormatException ne) {
            System.err.println(
                "Unable to parse line \"" + currLine + "\" of file " + file.getName());
            wrdOccurrence = 0;
          }
          occFileNorm += wrdOccurrence * wrdOccurrence;
          dotProduct += wrdOccurrence * textImpWrds.getOrDefault(parts[0], 0);
        }

        // Calculate and save fileScore
        double fileScore = dotProduct / (textOccMag * Math.sqrt(occFileNorm));
        // Score is saved if it is in top 'results' number of elements
        fileScores.put(occToFileName(file.getName()), fileScore);
      } catch (IOException e) {
        System.err.println("Unable to consider " + file.getName() + " in comparison");
      } 
    }
  }

  private void initializeTracker(String directory) {
    // Looks in directory for .occ files and adds them to
    // the tracked files.  Used for reloading after a session
    File cacheDirFiles = new File(directory);
    // Gets all of the files with FILE_EXT extension
    File[] occFiles =
        cacheDirFiles.listFiles(
            new FilenameFilter() {
              @Override
              public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(FILE_EXT);
              }
            });

    for (File occFile : occFiles) {
      // occFile name is path with .occ extension added and file separators replaced
      // Reverse process to get original file
      File originalFile = new File(occToFileName(occFile.getName()));
      // Check if file is still part of the project
      // TODO check occ file age and update if original file is newer
      if (originalFile.exists()) {
        trackedFiles.put(occFile.getName(), originalFile.lastModified());
      } else {
        // If file is no longer part of project then delete .occ file for it
        try {
          occFile.delete();
        } catch (SecurityException e) {
          System.err.println("Deletion of redundant file " + occFile.getAbsolutePath() + " failed");
        }
      }
    }
  }

  private String fileToOccName(String name) {
    return name.substring(1).replaceAll(File.separator, SEP_REPLACER) + FILE_EXT;
  }

  private String occToFileName(String name) {
    String cleanedFilePath = name.substring(0, name.length() - FILE_EXT.length());
    return cleanedFilePath.replaceAll(SEP_REPLACER, File.separator);
  }
}

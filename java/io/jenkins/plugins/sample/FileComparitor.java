package io.jenkins.plugins.sample;

import java.io.*;
import java.util.Map;
import java.util.HashMap;
import java.lang.StringBuilder;

public class FileComparitor {
    
    //TODO IMPORTANT NOTE File.lastModified()
    //compare that to cahced info
    //when doing a lookup to tell if cached files are updated
    public static final String FILE_EXT = ".occ"; 
    private static final int READ_LINES = 50;
    private static final String SEP_REPLACER = "//";

    public final String cacheDirectory;
    private ImportantWordsGenerator importantWrdGen = ImportantWordsGenerator.INSTANCE;
    //Contains all tracked files and their lastModified date
    private Map<String, Long> trackedFiles= new HashMap<String, Long>();

    //Creates a new comparitor that caches files in directory
    //TODO change directory to absolute instead of relative?
    public FileComparitor(String directory) {
	cacheDirectory = System.getProperty("user.home") + File.separator + ".BugRanking" + 
	    File.separator + directory;
	//Check if the caching directory exists
	//If it does then intialize trackedFiles
	//If not then create the directory
	File cacheDirFile = new File(cacheDirectory);
	if (cacheDirFile.exists() && cacheDirFile.isDirectory()) {
	    initializeTracker(cacheDirectory);
	} else {
	    cacheDirFile.mkdir();
	}
    }

    public void trackDirectory(String directory) {
	//Tracks all of the files within the directory (absolute path)
	//Recursive, includes all files in subdirectories
	//Creates .occ files for each
	File folder = new File(directory);
	trackDirectory(folder);
    }
    
    public void trackDirectory(File folder) {
	//Tracks all of the files within the directory 
	//Recursive, includes all files in subdirectories
	//Creates .occ files for each
	if (!folder.isDirectory()) { return; }
	File[] files = folder.listFiles();

	for (File file: files) {
	    if (file.isFile()) {
		trackFile(file);
	    } else if (file.isDirectory()) {
		trackDirectory(file);
	    }
	}
    }

    public void trackFile(String directory) {
	File file = new File(directory);
	trackFile(file);
    }

    public void trackFile(File file) {
	//If file doesn't exist, stop
	if (!file.exists()) { return; }

	Map<String, Integer> occurrences = new HashMap<String, Integer>();
	BufferedReader fileReader = null;
	String currLine;
	StringBuilder fileTextChunk = new StringBuilder();
	int lineNum = 0;
	//Reads READ_LINES number of lines from the file at a time
	//Generates the important words from that chunk
	//Saves the occurrences in occurrences map
	try {
	    fileReader = new BufferedReader(new FileReader(file));
	    //Read file line by line
	    while ((currLine = fileReader.readLine()) != null) {
		fileTextChunk.append(currLine);
		lineNum += 1;
		//If lineNum is multiple of READ_LINES then process chunk
		if (lineNum % READ_LINES) {
		    //Extend the current occurrences with important words from this chunk
		    importantWrdGen.extendGeneration(fileTextChunk.toString(), occurrences);
		    //Reset the StringBuilder buffer to 0
		    fileTextChunk.setLength(0);
		}
	    }
	} catch (IOException e) {
	    System.err.println("Unable to track " + file.getAbsolutePath());
	} finally {
	    //Close fileReader 
	    try {
		if (fileReader != null) {
		    fileReader.close();
		}
	    } catch (IOException ex) {
		ex.printStackTrace();
	    }
	}
	BufferedWriter fileWriter = null;
	//Replace all file separators with someting that won't be
	//interpreted as such '//'
	String niceFilePath = file.getAbsolutePath().replaceAll(File.separator, SEP_REPLACER);
	try {
	    fileWrite = new BufferedWriter(new FileWriter(cacheDirectory + File.separator + niceFilePath));
	    //Write all unique occurrences and number to file
	    for (Map.Entry<String, Integer> occurrence: occurrences.entrySet()) {
		fileWrite.write(occurrence.getKey() + " " + occurrence.getValue());
		fileWrite.newLine();
	    }
	} catch (IOException e) {
	    System.err.println("Unable to cache occurrences for " + file.getAbsolutePath());
	} finally {
	    //Close fileWriter
	    try {
		if (fileWriter != null) {
		    fileWriter.close();
		}
	    } catch (IOException ex) {
		ex.printStackTrace();
	    }
	}
	//Add file to tracking list
	trackedFiles.put(file.getAbsolutePath(), file.lastModified());
    }

    public void compare(String text, int results) {
	//compare each .occ file to text and return top results number results
    }

    private void initializeTracker(String directory) {
	//Looks in directory for .occ files and adds them to
	//the tracked files.  Used for reloading after a session
	File cacheDirFiles = new File(directory);
	//Gets all of the files with FILE_EXT extension
	File[] occFiles = cacheDirFiles.listFiles(new FilenameFilter() {
	    @Override
	    public boolean accept(File dir, String name) {
		return name.toLowerCase().endsWith(FILE_EXT);
	    }
	});
	//File extension length (plus period).  Used to remove file extension
	int pathExtLen = FILE_EXT.length() + 1;
	
	for (File occFile: occFiles) {
	    //occFile name is path with .occ extension added and file separators replaced with '//'
	    //Reverse process to get original file path
	    String cleanedFilePath = occFile.getName().substring(0, occFileAbsPath.length() - pathExtLen);
	    String originalPath = cleanedFilePath.replaceAll(SEP_REPLACER, File.separator);
	    File originalFile = new File(originalPath);
	    //Check if file is still part of the project
	    if (originalFile.exists()) {
		trackedFiles.put(
			originalPath,
			originalFile.lastModified() 
		);
	    } else {
		//If file is no longer part of project then delete .occ file for it
		try {
		    occFile.delete();
		} catch (SecurityException e) {
		    System.err.println("Deletion of redundant file " + occFile.getAbsolutePath() + " failed");
		}
	    }
	}
    }

}


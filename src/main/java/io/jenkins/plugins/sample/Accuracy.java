package io.jenkins.plugins.sample;

import java.util.Map;

public class Accuracy {

  public static Accuracy INSTANCE = null;

  private Accuracy() { 
    
  }

  public static Accuracy getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new Accuracy();
    }
    return INSTANCE;
  }

  /**
  *
  *
  * @param projectName	
  * @return	
  */
  public String projectAccuracy(String projectName) {
    TestProgram defects = new TestProgram(projectName);
    System.out.println("Project " + defects.getID() + " has " + defects.getNumOfBugs() + " bugs");
    //for (int bugID = 1; bugID <= defects.getNumOfBugs(); bugID += 1) {
    int bugID = 1; //TODO DELETE
    String codeDirectory = defects.checkoutCodeVersion(bugID);
    Map<String, String> bugInfo = defects.getBugInformation(bugID);
    FileComparator fc = new FileComparator("Accuracy");
    fc.trackDirectory("/tmp/Lang1/src/main");
    Map<String, Double> matchedFiles = fc.compare(bugInfo.get("report"), 5);
    fc.deleteCache();
    //}
    
    return matchedFiles.toString();
  }
  
}

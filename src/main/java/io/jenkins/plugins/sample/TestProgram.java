package io.jenkins.plugins.sample;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TestProgram {

  private final String identity;
  private ArrayList<String> projectInfo;
  private ArrayList<String> bugInfo;
  private int currentBugID = 1;
  private int numOfBugs = -1;
  private static final String defects4jDir = "/home/tanner/School/616/defects4j";
  private static final String defects4jExecutable = defects4jDir + "/framework/bin/defects4j";
  private static final String srcCodeBaseDir = "/tmp/";

  private final String lineDivider = "---------";

  public TestProgram(String identity) {
    this.identity = identity;
    try {
      projectInfo = runCommand(new String[] {defects4jExecutable, "info", "-p", identity});
    } catch (Exception e) {
      /*
      StringWriter errors = new StringWriter();
      e.printStackTrace(new PrintWriter(errors));
      numOfBugs = errors.toString();
      */
    }
    String numBugStr = findValInCmdByLabel("number of bugs", projectInfo, false).trim();
    numOfBugs = Integer.parseInt(numBugStr);
  }

  public String getID() {
    return identity;
  }

  public int getNumOfBugs() {
    return numOfBugs;
  }

  // checkout buggy code for bug identity
  // returns code directory name
  public String checkoutCodeVersion(int bugID) {
    String codeDirectory = srcCodeBaseDir + identity + bugID;
    try {
      String[] cmd =
          new String[] {
            defects4jExecutable, "checkout", "-p", identity, "-v", bugID + "b", "-w", codeDirectory
          };
      runCommand(cmd);
    } catch (Exception e) {
      return "";
    }
    return codeDirectory;
  }

  public Map<String, String> getNextBug() {
    currentBugID += 1;
    return getBugInformation(currentBugID);
  }

  public Map<String, String> getBugInformation(int index) {
    Map<String, String> bugInfoMap = new HashMap<String, String>();

    try {
      String[] cmd =
          new String[] {defects4jExecutable, "info", "-p", identity, "-b", Integer.toString(index)};
      if ((bugInfo = runCommand(cmd)) == null) {
        return null;
      }
    } catch (Exception e) {
      return null;
    }

    bugInfoMap.put("files", findValInCmdByLabel("list of modified", bugInfo, true));
    String url = findValInCmdByLabel("bug report url", bugInfo, false).trim();
    bugInfoMap.put("url", url);
    bugInfoMap.put("report", getBugReportFromUrl(url));
    return bugInfoMap;
  }

  public String getBugReportFromUrl(String url) {
    Document page;
    try {
      page = Jsoup.connect(url).get();
    } catch (IOException e) {
      return "Failed to grab page.";
    }

    // TODO make compatible with all projects
    Elements pageElements = page.select("div.user-content-block > p");

    StringBuilder report = new StringBuilder();
    for (Element pageElement : pageElements) {
      report.append(pageElement.text());
    }
    return report.toString();
  }

  // Finds the value for a given label in defects4j
  // Returns the value or "" if not found
  private String findValInCmdByLabel(String label, ArrayList<String> cmdResult, boolean multiLine) {
    label = label.toLowerCase();
    for (int i = 0; i < cmdResult.size(); i += 1) {
      if (cmdResult.get(i).toLowerCase().startsWith(label)) {
        if (multiLine) {
          StringBuilder result = new StringBuilder();
          int lineNum = i + 1;
          while (lineNum <= cmdResult.size() && !cmdResult.get(lineNum).startsWith(lineDivider)) {
            result.append(cmdResult.get(lineNum)).append("\n");
            lineNum += 1;
          }
          return result.toString();
        } else {
          // split the line into label and value
          String[] parts = cmdResult.get(i).split(":", 2);
          // if value is empty then check if value is on next line
          if (parts[1].equals("") && (i + 1 <= cmdResult.size())) {
            return cmdResult.get(i + 1);
            // Otherwise, return the value
          } else {
            return parts[1];
          }
        }
      }
    }
    return "";
  }

  private ArrayList<String> runCommand(String[] commands) throws IOException, InterruptedException {
    // run command in 4j directory
    ProcessBuilder pb = new ProcessBuilder(commands);
    pb.directory(new File(defects4jDir));
    pb.redirectErrorStream(true);
    Process process = pb.start();

    // read output
    ArrayList<String> output = new ArrayList<String>();
    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
    String line = null;
    String previous = null;
    while ((line = br.readLine()) != null) {
      if (!line.equals(previous)) {
        previous = line;
        output.add(line);
      }
    }

    if (process.waitFor() != 0) {
      System.err.println("Failed to run command: " + Arrays.toString(commands));
      return null;
    }
    return output;
  }
}

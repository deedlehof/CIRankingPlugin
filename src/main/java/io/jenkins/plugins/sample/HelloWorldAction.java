package io.jenkins.plugins.sample;

import hudson.model.Run;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import jenkins.model.RunAction2;


/**
* HelloWorldAction is responsible for serving
* data to the plugin front-end.
*/
public class HelloWorldAction implements RunAction2 {

  private transient FileComparator fc;
  private transient Map<String, Double> matchedFiles;
  private transient Run run;

  private String codeLocation;
  private String bugReport;

  /**
  * Creates a new HelloWorldAction that runs the
  * file comparison for bug reports.
  *
  * @param codeLocation  the directory to be tracked
  * @param bugReport  the bug report to be compared
  */
  public HelloWorldAction(String codeLocation, String bugReport) {
    this.codeLocation = codeLocation;
    this.bugReport = bugReport;

    fc = new FileComparator("Lang");
    fc.trackDirectory(codeLocation);

    matchedFiles = fc.compare(bugReport, 5);
    System.err.println("=======TOP MATCHING FILES=========");
    for (Map.Entry<String, Double> entry: matchedFiles.entrySet()) {
      System.out.println(entry.getValue() + " --- " + entry.getKey());
    }
    
  }

  public String getCodeLocation() {
    return codeLocation;
  }

  public Map<String, Double> getMatchedFiles() {
    return matchedFiles;
  }

  public String[] getFileLabels() {
    Set<String> keys = matchedFiles.keySet();
    return keys.toArray(new String[keys.size()]);
  }

  @Override
  public void onAttached(Run<?, ?> run) {
    this.run = run;
  }

  @Override
  public void onLoad(Run<?, ?> run) {
    this.run = run;
  }

  public Run getRun() {
    return run;
  }

  @Override
  public String getIconFileName() {
    return "document.png";
  }

  @Override
  public String getDisplayName() {
    return "Bug Ranking";
  }

  @Override
  public String getUrlName() {
    return "bugranking";
  }
}

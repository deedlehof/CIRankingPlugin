package io.jenkins.plugins.sample;

// import hudson.model.Action;
import hudson.model.Run;
import jenkins.model.RunAction2;

public class HelloWorldAction implements RunAction2 {

  private transient FileComparator fc;
  private transient ScoreBoard matchedFiles;
  private transient Run run;

  private String codeLocation;
  private String bugReport;

  public HelloWorldAction(String codeLocation, String bugReport) {
    this.codeLocation = codeLocation;
    this.bugReport = bugReport;

    fc = new FileComparator("Lang");
    fc.trackDirectory(codeLocation);

    matchedFiles = fc.compare(bugReport, 5);
    System.err.println("=======TOP MATCHING FILES=========");
    System.err.println(matchedFiles);
  }

  public String getCodeLocation() {
    return codeLocation;
  }

  public ScoreBoard getMatchedFiles() {
    return matchedFiles;
  }

  public String[] getFileLabels() {
    return matchedFiles.getLabels();
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
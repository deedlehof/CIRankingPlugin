package io.jenkins.plugins.sample;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Project;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import jenkins.model.TransientActionFactory;


/**
* HelloWorldAction is responsible for serving
* data to the plugin front-end.
*/
public class RankingAction implements Action {

  private transient FileComparator fc;
  private transient Map<String, Double> matchedFiles;

  private String codeLocation;
  private String bugReport;

  private Project project;

  /**
  * Creates a new HelloWorldAction that runs the
  * file comparison for bug reports.
  *
  */
  public RankingAction(Project project) {
    this.codeLocation = "This is a test location"; //TODO DELETE
    /*
    this.codeLocation = codeLocation;
    this.bugReport = bugReport;

    fc = new FileComparator("Lang");
    fc.trackDirectory(codeLocation);

    matchedFiles = fc.compare(bugReport, 5);
    System.err.println("=======TOP MATCHING FILES=========");
    for (Map.Entry<String, Double> entry: matchedFiles.entrySet()) {
      System.out.println(entry.getValue() + " --- " + entry.getKey());
    }
    */
    this.project = project;
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

  @Extension
  public static final class RankingFactory extends TransientActionFactory<Project> {
    
    @Override
    public Class<Project> type() {
      return Project.class;
    } 

    // Create action depending on the project type
    @Override
    public Collection<? extends Action> createFor(Project project) {
      return Collections.singleton(new RankingAction(project));
    } 
  } 
}

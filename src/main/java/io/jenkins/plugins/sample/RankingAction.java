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
    this.project = project;

    this.codeLocation = "/home/tanner/School/Project/Lang/src/main"; //TODO DELETE
    this.bugReport = "  NumberUtils.createLong() does not handle hex numbers, but createInteger() handles hex and octal. This seems odd.  NumberUtils.createNumber() assumes that hex numbers can only be Integer. Again, why not handle bigger Hex numbers?  ==  It is trivial to fix createLong() - just use Long.decode() instead of valueOf(). It's not clear why this was not done originally - the decode() method was added to both Integer and Long in Java 1.2.  Fixing createNumber() is also fairly easy - if the hex string has more than 8 digits, use Long.  Should we allow for leading zeros in an Integer? If not, the length check is trivial.";
    /*
    this.codeLocation = codeLocation;
    this.bugReport = bugReport;
    */
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

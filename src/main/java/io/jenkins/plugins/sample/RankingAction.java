package io.jenkins.plugins.sample;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.Project;
import hudson.util.FormValidation;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.servlet.ServletException;
import jenkins.model.TransientActionFactory;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
* HelloWorldAction is resposible for serving
* data to the plugin front-end.
*/
public class RankingAction implements Action {

  private transient FileComparator fc;
  private transient Map<String, Double> matchedFiles;

  private String codeLocation = "";
  private String report = "";
  private int numResults = 5;

  private Project project;

  /**
  * Creates a new HelloWorldAction that runs the
  * file comparison for bug reports.
  *
  */
  public RankingAction(Project project) {
    this.project = project;

    // Load properties
    Properties loadProps = new Properties();
    try {
      loadProps.loadFromXML(new FileInputStream("settings.xml"));
    } catch (IOException e) {
      System.err.println("FAILURE: Failed to load properties");
    }
    codeLocation = loadProps.getProperty("codeLocation");
    String numResultsStr = loadProps.getProperty("numResults");
    try {
      numResults = Integer.parseInt(numResultsStr);
      if (numResults < 1) {
        numResults = 1;
      } 
    } catch (NumberFormatException e) {
      System.err.println("FAILURE: Failed to load number of results from properties");
    }
     

    fc = new FileComparator("Lang");
    // Get all the file locations
    String[] locations = codeLocation.split("\\r?\\n");
    for (String location : locations) {
      fc.trackDirectory(location);
    }

    System.err.println("===========REINIT ACTION============");
  }

  public String getCodeLocation() {
    return codeLocation;
  }

  public String getReport() {
    return report;
  } 

  public int getNumResults() {
    return numResults;
  } 

  /**
  * Converts top matching files from (String, Double) to (String, Integer), with the value being the
  * percent match and returns it.
  *
  * @return a Map containing file names and their percent match to query
  */
  public Map<String, Integer> getMatchedFiles() {
    Map<String, Integer> formatted = new LinkedHashMap<>();
    for (Map.Entry<String, Double> match: matchedFiles.entrySet()) {
      formatted.put(match.getKey(), (int)Math.round(match.getValue() * 100));
    }
    return formatted; 
  }

  public String[] getFileLabels() {
    Set<String> keys = matchedFiles.keySet();
    return keys.toArray(new String[keys.size()]);
  }

  /**
  * Takes data from search page and updates top matching files for query.
  *
  * @return am HttpRedirect to return to the index page
  */
  public HttpResponse doCalculateResults(StaplerRequest req) throws IOException, ServletException {
    JSONObject jsonData = req.getSubmittedForm();
    report = jsonData.optString("bugReport");
    matchedFiles = fc.compare(report, numResults);
    System.err.println("=======TOP MATCHING FILES=========");
    for (Map.Entry<String, Double> entry: matchedFiles.entrySet()) {
      System.out.println(entry.getValue() + " --- " + entry.getKey());
    }
    return new HttpRedirect("index");
  } 

  /**
  * Takes data from the settings page, updates them, and stores them as properties.
  *
  * @return an HttpRedirect to return to the settings page
  */
  public HttpResponse doSetProperties(StaplerRequest req) throws IOException, ServletException {
    JSONObject jsonData = req.getSubmittedForm();
    codeLocation = jsonData.optString("codeLocation");
    String numResultsStr = jsonData.optString("numResults");
    // Convert field to integer
    try {
      numResults = Integer.parseInt(numResultsStr);
      if (numResults < 1) {
        numResults = 1;
      } 
    } catch (NumberFormatException e) {
      numResults = 5;
    }

    // Get all the file locations
    String[] locations = codeLocation.split("\\r?\\n");
    for (String location : locations) {
      fc.trackDirectory(location);
    }
   
    // Get new matched files
    if (!report.isEmpty()) {
      matchedFiles = fc.compare(report, numResults);
    } 

    // Save properties
    Properties saveProps = new Properties();
    saveProps.setProperty("codeLocation", codeLocation);
    saveProps.setProperty("numResults", numResultsStr);

    try {
      saveProps.storeToXML(new FileOutputStream("settings.xml"), "");
    } catch (IOException e) {
      System.err.println("FAILURE: Failed to write properties to file");
    }
      
    return new HttpRedirect("settings");
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
    return "RankingAction";
  }

  @Extension
  public static final class RankingFactory extends TransientActionFactory<Project> {

    public static RankingAction ra = null;
    public static Project project = null;
    
    @Override
    public Class<Project> type() {
      return Project.class;
    } 

    // Create action depending on the project type
    @Override
    public Collection<? extends Action> createFor(Project project) {
      if (this.project != project) {
        ra = new RankingAction(project);
	this.project = project;
      } 
      // Return Collections.singleton(new RankingAction(project));
      return Collections.singleton(ra);
    } 
  } 

}

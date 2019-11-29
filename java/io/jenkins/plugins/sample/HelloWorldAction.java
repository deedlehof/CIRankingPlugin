package io.jenkins.plugins.sample;

//import hudson.model.Action;
import hudson.model.Run;
import jenkins.model.RunAction2;
import java.util.Map;

public class HelloWorldAction implements RunAction2 {
  
    private transient FileComparitor fc;

    private int bugIndex;
    private String files;
    private String url;
    private String report;
    private String codeLocation;
    private transient Run run;
    private transient TestProgram tp;

    public HelloWorldAction(int index) {
	bugIndex = index;
	tp = new TestProgram("Lang");
	Map<String, String> bugReportInfo = tp.getBugInformation(bugIndex);
	files = bugReportInfo.get("files");
	url = bugReportInfo.get("url");
	report = bugReportInfo.get("report");
	codeLocation = tp.checkoutCodeVersion(index);

	fc = new FileComparitor("test");
	fc.trackFile("/home/tanner/School/616/TestFiles/file1");
	
	Map<String, Double> matchedFiles = fc.compare("compare word docs", 1);
	
	for (Map.Entry<String, Double> file: matchedFiles.entrySet()) {
	    System.err.println(file.getKey() + " \t" + file.getValue());
	}
    }

    public String getName() {
	return tp.getID();
	//return tp.testMeth();
	//return Integer.toString(tp.getNumOfBugs());
	//return tp.getBugInformation(1).get("report");
	//return tp.checkoutCodeVersion(1);
    }

    public String getFiles() {
	return files;
    }

    public String getURL() {
	return url;
    }

    public String getReport() {
	return report;
    }

    public String getCodeLocation() {
	return codeLocation;
    }

    public int getBugIndex() {
	return bugIndex;
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

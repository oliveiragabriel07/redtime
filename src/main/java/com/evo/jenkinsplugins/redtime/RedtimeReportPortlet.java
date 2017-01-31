package com.evo.jenkinsplugins.redtime;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.plugins.view.dashboard.DashboardPortlet;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.ArrayList;
import java.util.List;

public class RedtimeReportPortlet extends DashboardPortlet {

    private int maxReds = 0;

    @DataBoundConstructor
    public RedtimeReportPortlet(String name) {
        super(name);
        // TODO: get maxReds as parameter
        // this.maxReds = maxReds;
    }

    public List<RedInterval> getReds() {
        List<Job> jobs = getDashboardJobs();
        List<RedInterval> reds = new ArrayList<>();

        for (Job job : jobs) {
            Run lastUnsuccessfulBuild = job.getLastUnsuccessfulBuild();
            int i = 0;

            while (lastUnsuccessfulBuild != null && (maxReds == 0) || i < maxReds) {
                Run repair = lastUnsuccessfulBuild.getNextBuild();
                Run initialFailure;
                Run previousSuccessfulBuild = lastUnsuccessfulBuild.getPreviousSuccessfulBuild();
                if (previousSuccessfulBuild == null) {
                    // never succeeded, get the first build
                    initialFailure = lastUnsuccessfulBuild;
                    while (initialFailure.getPreviousBuild() != null) {
                        initialFailure = initialFailure.getPreviousBuild();
                    }
                } else {
                    initialFailure = previousSuccessfulBuild.getNextBuild();
                }

                reds.add(new RedInterval(job, repair, initialFailure));

                lastUnsuccessfulBuild = getPreviousUnsuccessfulBuild(initialFailure);
                i++;
            }
        }

        return reds;
    }

    /**
     * for unit test
     */
    protected List<Job> getDashboardJobs() {
        return getDashboard().getJobs();
    }

    protected Run getPreviousUnsuccessfulBuild(Run build) {
        Run r = build.getPreviousBuild();
        while (r != null && r.getResult() == Result.SUCCESS)
            r = r.getPreviousBuild();
        return r;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<DashboardPortlet> {
        @Override
        public String getDisplayName() {
            return Messages.Redtime_ReportName();
        }
    }
}

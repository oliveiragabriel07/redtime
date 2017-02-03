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
import java.util.PriorityQueue;

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
        PriorityQueue<RedInterval> queue = new PriorityQueue<>(jobs.size(),
                RedInterval.ORDER_BY_REPAIR_DATE);

        for (Job job : jobs) {
            Run lastUnsuccessfulBuild = job.getLastUnsuccessfulBuild();
            if (lastUnsuccessfulBuild != null) {
                queue.add(getLastRed(job, lastUnsuccessfulBuild));
            }
        }

        List<RedInterval> reds = new ArrayList<>();
        while (queue.peek() != null) {
            RedInterval red = queue.poll();
            reds.add(red);
            if (maxReds > 0 && reds.size() == maxReds) {
                break;
            }

            Run lastUnsuccessfulBuild = getPreviousUnsuccessfulBuild(red.getFailure());
            if (lastUnsuccessfulBuild != null) {
                queue.add(getLastRed(red.getJob(), lastUnsuccessfulBuild));
            }
        }

        return reds;
    }

    private RedInterval getLastRed(Job job, Run lastUnsuccessfulBuild) {
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

        return new RedInterval(job, repair, initialFailure);
    }

    /**
     * for unit test
     */
    protected List<Job> getDashboardJobs() {
        return getDashboard().getJobs();
    }

    private Run getPreviousUnsuccessfulBuild(Run build) {
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

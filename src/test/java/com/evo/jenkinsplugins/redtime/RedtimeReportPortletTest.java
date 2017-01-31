package com.evo.jenkinsplugins.redtime;

import hudson.Launcher;
import hudson.model.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

/**
 * Created by gabriel on 17/01/17.
 */
public class RedtimeReportPortletTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    private FreeStyleProject project;

    private Set<Integer> fails;

    private RedtimeReportPortlet redsReport;

    @Before
    public void setup() throws IOException {
        fails = new HashSet<>();
        project = j.createFreeStyleProject();
        project.getBuildersList().add(new TestBuilder() {

            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
                return !fails.contains(build.getNumber());
            }
        });

        redsReport = new RedtimeReportPortlet("RedsReport") {

            @Override
            protected List<Job> getDashboardJobs() {
                return Collections.singletonList((Job) project);
            }

        };
    }

    @Test
    public void repairedBuildRetunsCorrectInterval() throws Exception {
        fails.add(2);
        for (int i = 0; i < 3; i++) {
            j.waitForCompletion(project.scheduleBuild2(0).get());
        }

        List<RedInterval> reds = redsReport.getReds();
        assertThat(reds.size(), is(1));
        RedInterval red = reds.get(0);
        assertThat(red.getFailure(), is((Run) project.getBuildByNumber(2)));
        assertThat(red.getRepair(), is((Run) project.getBuildByNumber(3)));
    }

    @Test
    public void setFailureToFirstBuildIfNeverSucceedBefore() throws ExecutionException, InterruptedException {
        fails.addAll(Arrays.asList(1, 2));
        for (int i = 0; i < 3; i++)
            j.waitForCompletion(project.scheduleBuild2(0).get());
        List<RedInterval> reds = redsReport.getReds();
        assertThat(reds.size(), is(1));
        RedInterval red = reds.get(0);
        assertThat(red.getFailure(), is((Run) project.getBuildByNumber(1)));
        assertThat(red.getRepair(), is((Run) project.getBuildByNumber(3)));
    }

    @Test
    public void setRepairToNullIfNotFixedYet() throws ExecutionException, InterruptedException {
        fails.addAll(Arrays.asList(2, 3));
        for (int i = 0; i < 3; i++) {
            j.waitForCompletion(project.scheduleBuild2(0).get());
        }

        List<RedInterval> reds = redsReport.getReds();
        assertThat(reds.size(), is(1));
        RedInterval red = reds.get(0);
        assertThat(red.getFailure(), is((Run) project.getBuildByNumber(2)));
        assertThat(red.getRepair(), is(nullValue()));
    }

    @Test
    public void getAllReds() throws ExecutionException, InterruptedException {
        fails.addAll(Arrays.asList(1,3,5));
        for (int i = 0; i<6; i++) {
            j.waitForCompletion(project.scheduleBuild2(0).get());
        }

        assertThat(redsReport.getReds().size(), is(3));
    }
}


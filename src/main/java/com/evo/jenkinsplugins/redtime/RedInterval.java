package com.evo.jenkinsplugins.redtime;

import hudson.Util;
import hudson.model.Job;
import hudson.model.Run;

import java.util.GregorianCalendar;

/**
 * Created by gabriel on 16/01/17.
 */
public class RedInterval {
    private Job job;
    private Run failure;
    private Run repair;
    private GregorianCalendar calendar = new GregorianCalendar();

    // used for tests
    public RedInterval() {
    }

    public RedInterval(Job job, Run repair, Run failure) {
        this.job = job;
        this.repair = repair;
        this.failure = failure;
    }

    public Job getJob() {
        return job;
    }

    public Run getFailure() {
        return failure;
    }

    public Run getRepair() {
        return repair;
    }

    public boolean isFixed() {
        return this.repair != null;
    }

    public GregorianCalendar getCalendar() {
        return calendar;
    }

    public void setCalendar(GregorianCalendar calendar) {
        this.calendar = calendar;
    }

    public long getDuration() {
        long failedAt = failure.getStartTimeInMillis() + failure.getDuration();
        long repairedAt;

        if (isFixed()) {
            repairedAt = repair.getStartTimeInMillis() + repair.getDuration();
        } else {
            // Use current timestamp
            repairedAt = getCalendar().getTimeInMillis();
        }

        return repairedAt - failedAt;
    }

    public String getDurationString() {
        String durationString = Util.getTimeSpanString(getDuration());
        return isFixed() ? durationString : Messages.Redtime_RedtimeDurationNotFixedYet(durationString);
    }
}

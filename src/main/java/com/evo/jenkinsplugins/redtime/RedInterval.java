package com.evo.jenkinsplugins.redtime;

import hudson.Util;
import hudson.model.Job;
import hudson.model.Run;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.GregorianCalendar;

/**
 * Created by gabriel on 16/01/17.
 */
public class RedInterval {
    public static final Comparator<RedInterval> ORDER_BY_REPAIR_DATE = new Comparator<RedInterval>() {
        public int compare(@Nonnull RedInterval lRed, @Nonnull RedInterval rRed) {
            long lt = lRed.getRepairTime();
            long rt = rRed.getRepairTime();
            if (lt > rt) return -1;
            if (lt < rt) return 1;
            return 0;
        }
    };
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
        return getRepairTime() - getFailureTime();
    }

    public long getFailureTime() {
        return failure.getStartTimeInMillis() + failure.getDuration();
    }

    public long getRepairTime() {
        if (isFixed()) {
            return repair.getStartTimeInMillis() + repair.getDuration();
        }

        return getCalendar().getTimeInMillis();
    }

    public String getDurationString() {
        String durationString = Util.getTimeSpanString(getDuration());
        return isFixed() ? durationString : Messages.Redtime_RedtimeDurationNotFixedYet(durationString);
    }
}

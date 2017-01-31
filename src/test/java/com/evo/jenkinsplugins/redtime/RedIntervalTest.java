package com.evo.jenkinsplugins.redtime;

import hudson.Util;
import hudson.model.Result;
import hudson.model.Run;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.annotation.Nonnull;
import java.util.GregorianCalendar;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Created by gabriel on 16/01/17.
 */
public class RedIntervalTest {
    private Run failure;
    private Run repair;

    @Before
    public void setup() {
        failure = new RunStub(0, Result.FAILURE, 500L);
        repair = new RunStub(5000L, Result.SUCCESS, 1000L);
    }

    @Test
    public void calculateDurationOfFixedRed() {
        RedInterval interval = new RedInterval(null, repair, failure);
        assertEquals((5000 + 1000) - (0 + 500), interval.getDuration());
    }

    @Test
    public void calculateDurationOfNotFixedRed() {
        GregorianCalendar calendar = Mockito.mock(GregorianCalendar.class);
        when(calendar.getTimeInMillis()).thenReturn(3000L);
        RedInterval interval = new RedInterval(null, null, failure);
        interval.setCalendar(calendar);
        assertEquals(3000 - (0 + 500), interval.getDuration());
    }

    @Test
    public void isFixedIfHasRepareBuild() {
        RedInterval interval = new RedInterval(null, repair, failure);
        assertTrue(interval.isFixed());
    }

    @Test
    public void isRedIfRepareBuildIsNull() {
        RedInterval interval = new RedInterval(null, null, failure);
        assertFalse(interval.isFixed());
    }

    @Test
    public void durationStringForFixedJobContainsTimespan() {
        RedInterval interval = new RedInterval(null, repair, failure);
        long duration = (5000 + 1000) - (0 + 500);
        assertEquals(Util.getTimeSpanString(duration), interval.getDurationString());
    }

    @Test
    public void durationStringForUnfixedJobContainsDefaultString() {
        final long duration = 1000;
        RedInterval interval = new RedInterval() {
            @Override
            public long getDuration() {
                return duration;
            }
        };

        assertEquals(Messages.Redtime_RedtimeDurationNotFixedYet(Util.getTimeSpanString(duration)), interval.getDurationString());
    }

    public static class RunStub extends Run {

        private long duration;

        protected RunStub(long timestamp, Result result, long duration) {
            super(null, timestamp);
            this.duration = duration;
            setResult(result);
        }

        @Override
        public void setResult(@Nonnull Result r) {
            result = r;
        }

        @Override
        public long getDuration() {
            return duration;
        }
    }
}

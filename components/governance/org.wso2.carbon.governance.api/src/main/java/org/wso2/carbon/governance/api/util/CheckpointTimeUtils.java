/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
 

package org.wso2.carbon.governance.api.util;

import org.apache.commons.lang.StringUtils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CheckpointTimeUtils {

    /**
     * Duration seconds format.
     * This will produce a two digit number for seconds followed with character 's'.
     * Example 08s.
     */
    private static final String durationSecondsFormat = "%02ds";

    /**
     * Duration minutes and seconds format.
     * This will provide a two digit number for minutes followed by character 'm' and a two digit number for seconds
     * followed with character 's'.
     * Example: 01m:23s.
     */
    private static final String durationMinutesSecondsFormat = "%02dm:%02ds";

    /**
     * Duration hours, minutes and seconds format. This will provide a two digit number for hours followed by
     * character 'h', a two digit number for minutes followed with character 'm' and a two digit number for seconds
     * followed with character 's'.
     * Example 07h:12m:09s.
     */
    private static final String durationHoursMinutesSecondsFormat = "%02dh:%02dm:%02ds";

    /**
     * Duration days, hours, minutes and seconds format.
     * This will produce a number for days followed with character 'd', a two digit number for hours followed by
     * character 'h', a two digit number for minutes followed with character 'm' and a two digit number for seconds
     * followed with character 's'.
     */
    private static final String durationDaysHoursMinutesSecondsFormat = "%dd:%02dh:%02dm:%02ds";

    /**
     * This method used to check whether a duration is between a specific boundary.
     *
     * @param duration  lifecycle current state duration timestamp.
     * @param minTime   boundary lower value.
     * @param maxTime   boundary upper value.
     * @return          true when duration is between the boundary.
     */
    public static boolean isDurationBetweenTimestamps(long duration, String minTime, String maxTime) {
        boolean result = false;
        // Current duration in milly seconds
        long durationInMillySeconds = TimeUnit.MILLISECONDS.convert(duration, TimeUnit.MILLISECONDS);
        // Get checkpoint boundary values in milly seconds
        long minBoundaryInMillySeconds = getMillySecondsByDuration(minTime);
        // Get checkpoint boundary values in milly seconds
        long maxBoundaryInMillySeconds = getMillySecondsByDuration(maxTime);
        // Check the duration is between the boundaries
        if (minBoundaryInMillySeconds < durationInMillySeconds && durationInMillySeconds < maxBoundaryInMillySeconds) {
            result = true;
        }
        return result;
    }

    /**
     * This method is used to get duration in milly seconds by passing the duration as a String.
     * @param duration  duration as a String.
     * @return          duration in milly seconds.
     */
    public static long getMillySecondsByDuration(String duration) {
        if (StringUtils.isEmpty(duration)) {
            throw new IllegalArgumentException("Invalid arguments supplied as duration: " + duration);
        }

        String formattedDuration = duration.replaceAll("d", "").replaceAll("h", "").replaceAll("m", "")
                .replaceAll("s", "");

        String[] tokens = formattedDuration.split(":");
        long secondsToMillySeconds = Long.parseLong(tokens[3]) * 1000;
        long minutesToMillySeconds = Long.parseLong(tokens[2]) * 60 * 1000;
        long hoursToMillySeconds = Long.parseLong(tokens[1]) * 60 * 60 * 1000;
        long daysToMillySeconds = Long.parseLong(tokens[0]) * 24 * 60 * 60 * 1000;
        return daysToMillySeconds + secondsToMillySeconds + minutesToMillySeconds + hoursToMillySeconds;
    }

    /**
     * This method used to calculate time difference of two timestamps.
     *
     * @param timeStampOne              latest timestamp.
     * @param timeStampTwo              earlier timestamp.
     * @return timeDurationTimestamp    timestamp difference from current time to current lifecycle last state changed
     *                                  time.
     */
    public static long calculateTimeDifference(String timeStampOne, String timeStampTwo) {
        if (StringUtils.isEmpty(timeStampOne) && StringUtils.isEmpty(timeStampTwo)) {
            throw new IllegalArgumentException("Invalid arguments supplied as timestamp one: '" + timeStampOne + "' or"
                    + " " + "timestamp two: '" + timeStampTwo + "' is not set");
        }
        return Timestamp.valueOf(timeStampOne).getTime() - Timestamp.valueOf(timeStampTwo).getTime();
    }

    /**
     * This method used to calculate time difference between timestamp to present.
     *
     * @param timeStamp earlier timestamp.
     * @return timeDurationTimestamp    timestamp difference from current time to current lifecycle last state changed
     *                                  time.
     */
    public static long calculateTimeDifferenceToPresent(String timeStamp) {
        if (StringUtils.isEmpty(getCurrentTime()) && StringUtils.isEmpty(timeStamp)) {
            throw new IllegalArgumentException(
                    "Invalid arguments supplied as timestamp two: '" + timeStamp + "' is not set");
        }
        return Timestamp.valueOf(getCurrentTime()).getTime() - Timestamp.valueOf(timeStamp).getTime();
    }

    /**
     * This method is used to current time
     *
     * @return String  current time in  yyyy-MM-dd HH:mm:ss.SSS format.
     */
    public static String getCurrentTime() {
        Date currentTimeStamp = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return dateFormat.format(currentTimeStamp);
    }

    /**
     * This method is used to format a timestamp to 'dd:hh:mm:ss'.
     *
     * @param duration  timestamp duration.
     * @return          formatted time duration to 'dd:hh:mm:ss'.
     */
    public static String formatTimeDuration(long duration) {
        String timeDuration;
        long days = TimeUnit.MILLISECONDS.toDays(duration);
        long hours = TimeUnit.MILLISECONDS.toHours(duration) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS
                .toDays(duration));
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
                .toHours(duration));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                .toMinutes(duration));
        // Setting the duration to a readable format.
        if (days == 0 && hours == 0 && minutes == 0) {
            timeDuration = String.format(durationSecondsFormat, seconds);
        } else if (days == 0 && hours == 0) {
            timeDuration = String.format(durationMinutesSecondsFormat, minutes, seconds);
        } else if (days == 0) {
            timeDuration = String.format(durationHoursMinutesSecondsFormat, hours, minutes, seconds);
        } else {
            timeDuration = String.format(durationDaysHoursMinutesSecondsFormat, days, hours, minutes, seconds);
        }
        return timeDuration;
    }
}

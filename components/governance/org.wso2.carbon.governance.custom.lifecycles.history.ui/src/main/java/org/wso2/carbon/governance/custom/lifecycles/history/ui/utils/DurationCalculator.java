/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.governance.custom.lifecycles.history.ui.utils;

import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

public class DurationCalculator {
    public static String calculateDifference(String timestamp1, String timestamp2) {

        long duration =
                Timestamp.valueOf(timestamp1).getTime() - Timestamp.valueOf(timestamp2).getTime();
        String res = "";
        long days = TimeUnit.MILLISECONDS.toDays(duration);
        long hours = TimeUnit.MILLISECONDS.toHours(duration)
                - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(duration));
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration));


        if (days == 0 && hours == 0 && minutes == 0) {
            if (seconds >= 30) {
                res = "1m";
            } else {
                res = "0m";
            }
        } else if (days == 0 && hours == 0) {
            if (seconds >= 30) {
                res = String.format("%02dm", minutes + 1);
            } else {
                res = String.format("%02dm", minutes);
            }

        } else if (days == 0) {
            if (seconds >= 30) {
                res = String.format("%02dh:%02dm", hours, minutes + 1);
            } else {
                res = String.format("%02dh:%02dm", hours, minutes);
            }

        } else {
            if (seconds >= 30) {
                res = String.format("%dd:%02dh:%02dm", days, hours, minutes + 1);
            } else {
                res = String.format("%dd:%02dh:%02dm", days, hours, minutes);
            }

        }
        return res;
    }


}


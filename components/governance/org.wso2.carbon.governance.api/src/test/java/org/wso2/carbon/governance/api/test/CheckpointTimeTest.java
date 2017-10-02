/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.governance.api.test;

import org.wso2.carbon.governance.api.test.utils.BaseTestCase;
import org.wso2.carbon.governance.api.util.CheckpointTimeUtils;

public class CheckpointTimeTest extends BaseTestCase {

    public void testIsDurationBetweenTimestamps() {
        assertTrue(CheckpointTimeUtils.isDurationBetweenTimestamps(300000000L, "1d:0h:00m:20s", "23d:2h:5m:52s"));
        assertFalse(CheckpointTimeUtils.isDurationBetweenTimestamps(9000000L, "1d:0h:00m:20s", "23d:2h:5m:52s"));
        assertFalse(CheckpointTimeUtils.isDurationBetweenTimestamps(300000000000L, "1d:0h:00m:20s", "23d:2h:5m:52s"));
    }

    public void testGetMillySecondsByDuration() {
        assertEquals(86400000L, CheckpointTimeUtils.getMillySecondsByDuration("1d:0h:00m:0s"));
    }

    public void testErrorGetMillySecondsByDuration() {
        //Empty Input
        try {
            CheckpointTimeUtils.getMillySecondsByDuration("");
            fail("Expected exception");
        } catch (IllegalArgumentException e) {

        }
        //Null input
        try {
            CheckpointTimeUtils.getMillySecondsByDuration(null);
            fail("Expected exception");
        } catch (IllegalArgumentException e) {

        }
    }

    public void testCalculateTimeDifference() {
        assertEquals(1L,
                     CheckpointTimeUtils.calculateTimeDifference("2017-09-27 17:33:20.115", "2017-09-27 17:33:20.114"));
    }

    public void testErrorCalculateTimeDifference() {
        // Empty Input
        try {
            CheckpointTimeUtils.calculateTimeDifference("", "");
            fail("Expected exception");
        } catch (IllegalArgumentException e) {

        }

        // Null Input
        try {
            CheckpointTimeUtils.calculateTimeDifference(null, null);
            fail("Expected exception");
        } catch (IllegalArgumentException e) {

        }
    }


    public void testCalculateTimeDifferenceToPresent() {
        assertNotNull(CheckpointTimeUtils.calculateTimeDifferenceToPresent(CheckpointTimeUtils.getCurrentTime()));
    }

    public void testErrorCalculateTimeDifferenceToPresent() {
        try {
            CheckpointTimeUtils.calculateTimeDifferenceToPresent("");
            fail("Expected exception");
        } catch (IllegalArgumentException e) {
            //expected exception
        }

        try {
            CheckpointTimeUtils.calculateTimeDifferenceToPresent(null);
            fail("Expected exception");
        } catch (IllegalArgumentException e) {
            //expected exception
        }
    }


    public void testGetCurrentTime() {
        assertNotNull(CheckpointTimeUtils.getCurrentTime());
    }

    public void testFormatTimeDuration() {
        assertEquals("1d:00h:00m:00s", CheckpointTimeUtils.formatTimeDuration(86400000L));
    }
}

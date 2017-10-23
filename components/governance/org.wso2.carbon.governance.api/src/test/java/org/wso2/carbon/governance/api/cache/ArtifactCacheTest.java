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
package org.wso2.carbon.governance.api.cache;

import junit.framework.TestCase;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;

import static org.mockito.Mockito.mock;

public class ArtifactCacheTest extends TestCase {

    public void testInvalidateCache() throws Exception {
        ArtifactCache artifactCache = new ArtifactCache();
        GovernanceArtifact governanceArtifact = mock(GovernanceArtifact.class);

        assertNull(artifactCache.getArtifact("Sample"));

        artifactCache.addArtifact("Sample", governanceArtifact);

        assertNotNull(artifactCache.getArtifact("Sample"));

        artifactCache.invalidateCache();

        assertNull(artifactCache.getArtifact("Sample"));

    }

    public void testInvalidateArtifact() throws Exception {

        ArtifactCache artifactCache = new ArtifactCache();
        GovernanceArtifact governanceArtifact = mock(GovernanceArtifact.class);
        assertNull(artifactCache.getArtifact("Sample"));

        artifactCache.addArtifact("Sample", governanceArtifact);

        assertNotNull(artifactCache.getArtifact("Sample"));

        artifactCache.invalidateArtifact("Sample");

        assertNull(artifactCache.getArtifact("Sample"));
    }

}
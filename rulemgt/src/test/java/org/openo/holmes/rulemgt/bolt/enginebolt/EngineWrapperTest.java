/**
 * Copyright 2017 ZTE Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openo.holmes.rulemgt.bolt.enginebolt;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.openo.holmes.rulemgt.bean.request.CorrelationCheckRule4Engine;
import org.openo.holmes.rulemgt.bean.request.CorrelationDeployRule4Engine;

public class EngineWrapperTest {

    private EngineWrapper engineWrapper = new EngineWrapper();

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void deployEngine() throws Exception {
        assertThat(engineWrapper.deployEngine(new CorrelationDeployRule4Engine()), equalTo(""));
    }

    @Test
    public void deleteRuleFromEngine() throws Exception {
        assertThat(engineWrapper.deleteRuleFromEngine(""), is(true));
    }

    @Test
    public void checkRuleFromEngine() throws Exception {
        assertThat(engineWrapper.checkRuleFromEngine(new CorrelationCheckRule4Engine()), is(true));
    }

}
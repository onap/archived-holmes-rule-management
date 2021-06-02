/**
 * Copyright 2017 - 2021 ZTE Corporation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.holmes.rulemgt.bolt.enginebolt;


import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.rulemgt.bean.request.CorrelationCheckRule4Engine;
import org.onap.holmes.rulemgt.bean.request.CorrelationDeployRule4Engine;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.easymock.EasyMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.powermock.api.easymock.PowerMock.*;

@RunWith(PowerMockRunner.class)
public class EngineWrapperTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private EngineWrapper engineWrapper = new EngineWrapper();

    private EngineService mockedEngineService;

    @Before
    public void before() {
        mockedEngineService = createMock(EngineService.class);
        Whitebox.setInternalState(engineWrapper, "engineService", mockedEngineService);
    }

    @After
    public void after() {
        resetAll();
    }

    @Test
    public void deployEngine_fail() throws Exception {
        thrown.expect(CorrelationException.class);
        thrown.expectMessage("Failed to deploy the rule!");

        expect(mockedEngineService.deploy(anyObject(CorrelationDeployRule4Engine.class),
                anyObject(String.class))).andReturn(null);

        replayAll();

        engineWrapper.deployEngine(new CorrelationDeployRule4Engine(), "127.0.0.1");

        verifyAll();
    }

    @Test
    public void deployEngine_parse_content_exception() throws Exception {
        thrown.expect(CorrelationException.class);
        thrown.expectMessage(
                "Failed to parse the value returned by the engine management service.");
        expect(mockedEngineService.deploy(anyObject(CorrelationDeployRule4Engine.class),
                anyObject(String.class))).andReturn("");

        replayAll();

        engineWrapper.deployEngine(new CorrelationDeployRule4Engine(), "127.0.0.1");

        verifyAll();
    }

    @Test
    public void deployEngine_success() throws Exception {
        String content = "{\"packageName\":\"test\"}";
        expect(mockedEngineService.deploy(anyObject(CorrelationDeployRule4Engine.class),
                anyObject(String.class))).andReturn(content);

        replayAll();

        String result = engineWrapper.deployEngine(new CorrelationDeployRule4Engine(), "127.0.0.1");

        assertThat(result, equalTo("test"));

    }

    @Test
    public void deleteRuleFromEngine_fail() throws Exception {
        thrown.expect(CorrelationException.class);
        thrown.expectMessage("Failed to delete the rule!");

        expect(mockedEngineService.delete(anyObject(String.class),
                anyObject(String.class)))
                .andReturn(false);

        replayAll();

        engineWrapper.deleteRuleFromEngine("", "127.0.0.1");

        verifyAll();
    }

    @Test
    public void deleteRuleFromEngine_success() throws Exception {
        expect(mockedEngineService.delete(anyObject(String.class),
                anyObject(String.class)))
                .andReturn(true);

        replayAll();

        boolean result = engineWrapper.deleteRuleFromEngine("", "127.0.0.1");

        assertThat(result, equalTo(true));
    }

    @Test
    public void checkRuleFromEngine_fail() throws Exception {
        thrown.expect(CorrelationException.class);
        thrown.expectMessage("Failed to verify the rule. The contents of the rule are invalid.");

        expect(
                mockedEngineService.check(anyObject(CorrelationCheckRule4Engine.class),
                        anyObject(String.class))).andReturn(false);
        replayAll();

        engineWrapper.checkRuleFromEngine(new CorrelationCheckRule4Engine(), "127.0.0.1");

        verifyAll();
    }

    @Test
    public void checkRuleFromEngine_success() throws Exception {
        expect(mockedEngineService.check(anyObject(CorrelationCheckRule4Engine.class),anyString())).andReturn(true);

        replayAll();

        boolean result = engineWrapper.checkRuleFromEngine(new CorrelationCheckRule4Engine(), "127.0.0.1");

        assertThat(result, is(true));
    }
}
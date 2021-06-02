/**
 * Copyright 2017-2021 ZTE Corporation.
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
package org.onap.holmes.rulemgt.dcae;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.holmes.common.dcae.DcaeConfigurationQuery;
import org.onap.holmes.common.dcae.entity.DcaeConfigurations;
import org.onap.holmes.common.dcae.entity.Rule;
import org.onap.holmes.common.utils.JerseyClient;
import org.onap.holmes.rulemgt.bean.response.RuleQueryListResponse;
import org.onap.holmes.rulemgt.bean.response.RuleResult4API;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.powermock.api.easymock.PowerMock.*;

@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("org.onap.holmes.common.utils.JerseyClient")
@PrepareForTest({DcaeConfigurationPolling.class, DcaeConfigurationQuery.class})
public class DcaeConfigurationPollingTest {

    @Test
    public void run() throws Exception {
        DcaeConfigurations dcaeConfigurations = new DcaeConfigurations();
        dcaeConfigurations.addDefaultRule(new Rule("test", "clName", "contents", 1));
        mockStatic(DcaeConfigurationQuery.class);
        expect(DcaeConfigurationQuery.getDcaeConfigurations(anyObject(String.class))).andReturn(dcaeConfigurations);
        DcaeConfigurationPolling dcaeConfigurationPolling = new DcaeConfigurationPolling("localhost");

        Whitebox.setInternalState(dcaeConfigurationPolling, "url", "http://127.0.0.1");

        RuleQueryListResponse ruleQueryListResponse = new RuleQueryListResponse();
        List<RuleResult4API> ruleResult4APIList = new ArrayList<RuleResult4API>() {
            {
                add(new RuleResult4API());
            }
        };
        ruleQueryListResponse.setCorrelationRules(ruleResult4APIList);
        ruleQueryListResponse.setTotalCount(ruleResult4APIList.size());

        JerseyClient mockedJerseyClient = PowerMock.createMock(JerseyClient.class);
        PowerMock.expectNew(JerseyClient.class).andReturn(mockedJerseyClient);
        expect(mockedJerseyClient.get(anyString(), anyObject())).andReturn(ruleQueryListResponse);

        PowerMock.expectNew(JerseyClient.class).andReturn(mockedJerseyClient);
        expect(mockedJerseyClient.delete(anyString())).andReturn("true");

        PowerMock.expectNew(JerseyClient.class).andReturn(mockedJerseyClient);
        expect(mockedJerseyClient.header(anyString(), anyString())).andReturn(mockedJerseyClient);
        expect(mockedJerseyClient.put(anyString(), anyObject())).andReturn("true");

        replayAll();

        dcaeConfigurationPolling.run();

        verifyAll();
    }

    @Test
    public void run_identical_contents() throws Exception {
        DcaeConfigurations dcaeConfigurations = new DcaeConfigurations();
        dcaeConfigurations.addDefaultRule(new Rule("test", "clName", "contents", 1));
        mockStatic(DcaeConfigurationQuery.class);
        expect(DcaeConfigurationQuery.getDcaeConfigurations(anyObject(String.class))).andReturn(dcaeConfigurations).times(2);
        DcaeConfigurationPolling dcaeConfigurationPolling = new DcaeConfigurationPolling("localhost");

        Whitebox.setInternalState(dcaeConfigurationPolling, "url", "http://127.0.0.1");

        RuleQueryListResponse ruleQueryListResponse = new RuleQueryListResponse();
        List<RuleResult4API> ruleResult4APIList = new ArrayList<RuleResult4API>() {
            {
                add(new RuleResult4API());
            }
        };
        ruleQueryListResponse.setCorrelationRules(ruleResult4APIList);
        ruleQueryListResponse.setTotalCount(ruleResult4APIList.size());
        JerseyClient mockedJerseyClient = PowerMock.createMock(JerseyClient.class);
        PowerMock.expectNew(JerseyClient.class).andReturn(mockedJerseyClient);
        expect(mockedJerseyClient.get(anyString(), anyObject())).andReturn(ruleQueryListResponse);

        PowerMock.expectNew(JerseyClient.class).andReturn(mockedJerseyClient);
        expect(mockedJerseyClient.delete(anyString())).andReturn("true");

        PowerMock.expectNew(JerseyClient.class).andReturn(mockedJerseyClient);
        expect(mockedJerseyClient.header(anyString(), anyString())).andReturn(mockedJerseyClient);
        expect(mockedJerseyClient.put(anyString(), anyObject())).andReturn("true");

        replayAll();

        dcaeConfigurationPolling.run();
        dcaeConfigurationPolling.run();

        verifyAll();
    }
}
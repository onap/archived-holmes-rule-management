/**
 * Copyright 2017 ZTE Corporation.
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
package org.onap.holmes.rulemgt.dcae;

import static org.easymock.EasyMock.anyObject;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.ProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.onap.holmes.common.dcae.entity.DcaeConfigurations;
import org.onap.holmes.common.dcae.utils.DcaeConfigurationParser;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.rulemgt.bean.request.RuleCreateRequest;
import org.onap.holmes.rulemgt.bean.response.RuleQueryListResponse;
import org.onap.holmes.rulemgt.bean.response.RuleResult4API;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.onap.holmes.common.dcae.entity.Rule;

@PrepareForTest(DaceConfigurationPolling.class)
@RunWith(PowerMockRunner.class)
public class DaceConfigurationPollingTest {

    @org.junit.Rule
    public ExpectedException thrown = ExpectedException.none();

    private DaceConfigurationPolling daceConfigurationPolling;

    @Before
    public void setUp() {
        daceConfigurationPolling = new DaceConfigurationPolling("holmes-rule-mgmt");
    }

    @Test
    public void testDaceConfigurationPolling_getDcaeConfigurations_exception() throws Exception {
        thrown.expect(CorrelationException.class);
        PowerMock.createMock(DcaeConfigurationParser.class);
        PowerMock.expectPrivate(DcaeConfigurationParser.class, "parse", anyObject(String.class))
                .andThrow(new CorrelationException("")).anyTimes();
        PowerMock.replayAll();
        Whitebox.invokeMethod(daceConfigurationPolling, "getDcaeConfigurations");
    }

    @Test
    public void testDaceConfigurationPolling_getDcaeConfigurations_null() throws Exception {
        thrown.expect(CorrelationException.class);
        PowerMock.createMock(DcaeConfigurationParser.class);
        PowerMock.expectPrivate(DcaeConfigurationParser.class, "parse", anyObject(String.class))
                .andReturn(null).anyTimes();
        PowerMock.replayAll();
        DcaeConfigurations dcaeConfigurations = Whitebox
                .invokeMethod(daceConfigurationPolling, "getDcaeConfigurations");
        assertTrue(dcaeConfigurations == null);
    }

    @Test
    public void testDaceConfigurationPolling_addAllCorrelationRules_ok() throws Exception {
        thrown.expect(ProcessingException.class);
        DcaeConfigurations dcaeConfigurations = new DcaeConfigurations();
        Rule rule = new Rule("test", "test", 1);
        dcaeConfigurations.getDefaultRules().add(rule);
        Whitebox.invokeMethod(daceConfigurationPolling, "addAllCorrelationRules", dcaeConfigurations);
    }

    @Test
    public void testDaceConfigurationPolling_getRuleCreateRequest() throws Exception {
        Rule rule = new Rule("test", "test1", 1);
        RuleCreateRequest actual = Whitebox
                .invokeMethod(daceConfigurationPolling, "getRuleCreateRequest", rule);
        assertTrue(actual.getRuleName().equals("test"));
        assertTrue(actual.getContent().equals("test1"));
        assertTrue(actual.getDescription().equals(""));
        assertTrue(actual.getEnabled() == 1);
    }
}
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
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.when;

import com.alibaba.fastjson.JSONException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.ws.rs.ProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.onap.holmes.common.config.MicroServiceConfig;
import org.onap.holmes.common.dcae.entity.DcaeConfigurations;
import org.onap.holmes.common.dcae.entity.Rule;
import org.onap.holmes.common.dcae.utils.DcaeConfigurationParser;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.rulemgt.bean.request.RuleCreateRequest;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@PrepareForTest({DcaeConfigurationPolling.class, MicroServiceConfig.class, ObjectMapper.class})
@RunWith(PowerMockRunner.class)
public class DcaeConfigurationPollingTest {

    @org.junit.Rule
    public ExpectedException thrown = ExpectedException.none();

    private DcaeConfigurationPolling daceConfigurationPolling;

    @Before
    public void setUp() {
        daceConfigurationPolling = new DcaeConfigurationPolling("holmes-rule-mgmt");
    }

    @Test
    public void testDaceConfigurationPolling_getDcaeConfigurations_exception() throws Exception {
        PowerMock.resetAll();
        thrown.expect(CorrelationException.class);
        thrown.expectMessage("syntax error, pos 1");
        PowerMockito.mockStatic(MicroServiceConfig.class);
        when(MicroServiceConfig.getServiceConfigInfoFromCBS("holmes-rule-mgmt"))
                .thenReturn("host");
        PowerMock.createMock(DcaeConfigurationParser.class);
        PowerMock.expectPrivate(DcaeConfigurationParser.class, "parse", "host")
                .andThrow(new CorrelationException("")).anyTimes();

        PowerMock.replayAll();
        Whitebox.invokeMethod(daceConfigurationPolling, "getDcaeConfigurations");
        PowerMock.verifyAll();
    }

    @Test
    public void testDaceConfigurationPolling_getDcaeConfigurations_null() throws Exception {
        PowerMock.resetAll();
        thrown.expect(CorrelationException.class);
        PowerMockito.mockStatic(MicroServiceConfig.class);
        when(MicroServiceConfig.getServiceConfigInfoFromCBS("holmes-rule-mgmt"))
                .thenReturn("host");
        PowerMock.createMock(DcaeConfigurationParser.class);
        PowerMock.expectPrivate(DcaeConfigurationParser.class, "parse", "host")
                .andReturn(null).anyTimes();

        PowerMock.replayAll();
        DcaeConfigurations dcaeConfigurations = Whitebox
                .invokeMethod(daceConfigurationPolling, "getDcaeConfigurations");
        PowerMock.verifyAll();

        assertThat(dcaeConfigurations == null, equalTo(true));
    }

    @Test
    public void testDaceConfigurationPolling_addAllCorrelationRules_connection_exception()
            throws Exception {
        PowerMock.resetAll();
        thrown.expect(ProcessingException.class);
        DcaeConfigurations dcaeConfigurations = new DcaeConfigurations();
        Rule rule = new Rule("test", "test", "tset",1);
        dcaeConfigurations.getDefaultRules().add(rule);

        PowerMock.replayAll();
        Whitebox.invokeMethod(daceConfigurationPolling, "addAllCorrelationRules",
                dcaeConfigurations);
        PowerMock.verifyAll();
    }

    @Test
    public void testDaceConfigurationPolling_getRuleCreateRequest() throws Exception {
        PowerMock.resetAll();
        Rule rule = new Rule("test", "test1", "stest",1);
        PowerMock.replayAll();
        RuleCreateRequest actual = Whitebox
                .invokeMethod(daceConfigurationPolling, "getRuleCreateRequest", rule);
        PowerMock.verifyAll();

        assertThat(actual.getRuleName(), equalTo("test"));
        assertThat(actual.getLoopControlName(), equalTo("test1"));
        assertThat(actual.getContent(), equalTo("stest"));
        assertThat(actual.getDescription(), equalTo(""));
        assertThat(actual.getEnabled(), equalTo(1));
    }
}
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.holmes.common.utils.JerseyClient;
import org.onap.holmes.rulemgt.bean.request.CorrelationCheckRule4Engine;
import org.onap.holmes.rulemgt.bean.request.CorrelationDeployRule4Engine;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.easymock.EasyMock.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(EngineService.class)
@SuppressStaticInitializationFor({"org.onap.holmes.common.utils.JerseyClient"})
public class EngineServiceTest {

    private EngineService engineService = new EngineService();
    ;

    @Before
    public void setUp() throws Exception {
        System.setProperty("ENABLE_ENCRYPT", "false");
    }

    @Test
    public void delete() throws Exception {
        JerseyClient client = createMock(JerseyClient.class);
        expectNew(JerseyClient.class).andReturn(client);
        expect(client.path(anyString())).andReturn(client);
        expect(client.delete(anyString())).andReturn("true");
        replayAll();
        assertThat(engineService.delete("test", "127.0.0.1"), is(true));
        verifyAll();
    }

    @Test
    public void check() throws Exception {
        JerseyClient client = createMock(JerseyClient.class);
        expectNew(JerseyClient.class).andReturn(client);
        expect(client.header(anyString(), anyString())).andReturn(client);
        expect(client.post(anyString(), anyObject())).andReturn("true");

        CorrelationCheckRule4Engine correlationCheckRule4Engine = new CorrelationCheckRule4Engine();
        correlationCheckRule4Engine.setContent("{\"package\":\"test\"}");

        replayAll();
        assertThat(engineService.check(correlationCheckRule4Engine, "127.0.0.1"), is(true));
        verifyAll();
    }

    @Test
    public void deploy() throws Exception {
        JerseyClient client = createMock(JerseyClient.class);
        expectNew(JerseyClient.class).andReturn(client);
        expect(client.header(anyString(), anyString())).andReturn(client);
        expect(client.put(anyString(), anyObject())).andReturn("true");

        CorrelationDeployRule4Engine correlationDeployRule4Engine = new CorrelationDeployRule4Engine();
        correlationDeployRule4Engine.setContent("{\"package\":\"test\"}");

        replayAll();
        assertThat(engineService.deploy(correlationDeployRule4Engine, "127.0.0.1"), equalTo("true"));
        verifyAll();
    }
}
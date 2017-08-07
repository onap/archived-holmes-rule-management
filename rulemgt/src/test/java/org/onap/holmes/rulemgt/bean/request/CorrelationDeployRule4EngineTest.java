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

package org.onap.holmes.rulemgt.bean.request;

import static org.junit.Assert.*;
import static org.hamcrest.core.IsEqual.equalTo;

import org.junit.Test;

public class CorrelationDeployRule4EngineTest {
    @Test
    public void getterAndSetter4Content(){
        final String value = "content";
        CorrelationDeployRule4Engine correlationDeployRule4Engine = new CorrelationDeployRule4Engine();
        correlationDeployRule4Engine.setContent(value);
        assertThat(correlationDeployRule4Engine.getContent(), equalTo(value));
    }

    @Test
    public void getterAndSetter4EngineId(){
        final String value = "engineId";
        CorrelationDeployRule4Engine correlationDeployRule4Engine = new CorrelationDeployRule4Engine();
        correlationDeployRule4Engine.setEngineId(value);
        assertThat(correlationDeployRule4Engine.getEngineId(), equalTo(value));
    }
}
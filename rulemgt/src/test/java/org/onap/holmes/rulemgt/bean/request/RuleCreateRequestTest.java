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

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

import org.junit.Test;

public class RuleCreateRequestTest {

    @Test
    public void getterAndSetter4RuleName(){
        final String rulename = "rulename";
        RuleCreateRequest ruleCreateRequest = new RuleCreateRequest();
        ruleCreateRequest.setRuleName(rulename);
        assertThat(ruleCreateRequest.getRuleName(), equalTo(rulename));
    }

    @Test
    public void getterAndSetter4Description(){
        final String description = "desc";
        RuleCreateRequest ruleCreateRequest = new RuleCreateRequest();
        ruleCreateRequest.setDescription(description);
        assertThat(ruleCreateRequest.getDescription(), equalTo(description));
    }

    @Test
    public void getterAndSetter4Content(){
        final String contents = "contents";
        RuleCreateRequest ruleCreateRequest = new RuleCreateRequest();
        ruleCreateRequest.setContent(contents);
        assertThat(ruleCreateRequest.getContent(), equalTo(contents));
    }

    @Test
    public void getterAndSetter4Enabled(){
        final int enabled = 0;
        RuleCreateRequest ruleCreateRequest = new RuleCreateRequest();
        ruleCreateRequest.setEnabled(enabled);
        assertThat(ruleCreateRequest.getEnabled(), is(enabled));
    }
}
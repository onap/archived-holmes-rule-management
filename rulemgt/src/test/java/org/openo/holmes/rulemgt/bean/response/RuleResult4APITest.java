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

package org.openo.holmes.rulemgt.bean.response;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

import java.util.Date;
import org.junit.Test;

public class RuleResult4APITest {

    @Test
    public void getterAndSetter4RuleId(){
        final String value = "ruleId";
        RuleResult4API ruleResult4API = new RuleResult4API();
        ruleResult4API.setRuleId(value);
        assertThat(ruleResult4API.getRuleId(), equalTo(value));
    }

    @Test
    public void getterAndSetter4RuleName(){
        final String value = "ruleName";
        RuleResult4API ruleResult4API = new RuleResult4API();
        ruleResult4API.setRuleName(value);
        assertThat(ruleResult4API.getRuleName(), equalTo(value));
    }

    @Test
    public void getterAndSetter4Description(){
        final String value = "desc";
        RuleResult4API ruleResult4API = new RuleResult4API();
        ruleResult4API.setDescription(value);
        assertThat(ruleResult4API.getDescription(), equalTo(value));
    }

    @Test
    public void getterAndSetter4Content(){
        final String value = "content";
        RuleResult4API ruleResult4API = new RuleResult4API();
        ruleResult4API.setContent(value);
        assertThat(ruleResult4API.getContent(), equalTo(value));
    }

    @Test
    public void getterAndSetter4CreateTime(){
        final Date value = new Date();
        RuleResult4API ruleResult4API = new RuleResult4API();
        ruleResult4API.setCreateTime(value);
        assertThat(ruleResult4API.getCreateTime(), equalTo(value));
    }

    @Test
    public void getterAndSetter4Creator(){
        final String value = "admin";
        RuleResult4API ruleResult4API = new RuleResult4API();
        ruleResult4API.setCreator(value);
        assertThat(ruleResult4API.getCreator(), equalTo(value));
    }

    @Test
    public void getterAndSetter4UpdateTime(){
        final Date value = new Date();
        RuleResult4API ruleResult4API = new RuleResult4API();
        ruleResult4API.setUpdateTime(value);
        assertThat(ruleResult4API.getUpdateTime(), equalTo(value));
    }

    @Test
    public void getterAndSetter4Modifier(){
        final String value = "admin";
        RuleResult4API ruleResult4API = new RuleResult4API();
        ruleResult4API.setModifier(value);
        assertThat(ruleResult4API.getModifier(), equalTo(value));
    }

    @Test
    public void getterAndSetter4Enabled(){
        final int value = 0;
        RuleResult4API ruleResult4API = new RuleResult4API();
        ruleResult4API.setEnabled(value);
        assertThat(ruleResult4API.getEnabled(), is(value));
    }
}
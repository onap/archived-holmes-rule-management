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

package org.onap.holmes.rulemgt.bean.response;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class RuleQueryListResponseTest {

    @Test
    public void getterAndSetter4CorrelationRules(){
        final List<RuleResult4API> value = new ArrayList<>();
        RuleQueryListResponse ruleQueryListResponse = new RuleQueryListResponse();
        ruleQueryListResponse.setCorrelationRules(value);
        assertThat(ruleQueryListResponse.getCorrelationRules(), equalTo(value));
    }

    @Test
    public void getterAndSetter4TotalCount(){
        final int value = 0;
        RuleQueryListResponse ruleQueryListResponse = new RuleQueryListResponse();
        ruleQueryListResponse.setTotalCount(value);
        assertThat(ruleQueryListResponse.getTotalCount(), is(value));
    }

}
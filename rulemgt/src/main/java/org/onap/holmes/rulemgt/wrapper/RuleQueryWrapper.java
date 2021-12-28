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

package org.onap.holmes.rulemgt.wrapper;

import org.onap.holmes.common.api.entity.CorrelationRule;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.rulemgt.db.CorrelationRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RuleQueryWrapper {

    @Autowired
    private CorrelationRuleService correlationRuleService;


    public List<CorrelationRule> queryRuleByEnable(int enable) {
        List<CorrelationRule> ruleTemp = correlationRuleService.queryRuleByRuleEnable(enable);
        return ruleTemp;
    }

    public List<CorrelationRule> queryRuleByEngineInstance(String instance) throws CorrelationException {
        List<CorrelationRule> ruleTemp = correlationRuleService.queryRuleByRuleEngineInstance(instance);
        return ruleTemp;
    }
}

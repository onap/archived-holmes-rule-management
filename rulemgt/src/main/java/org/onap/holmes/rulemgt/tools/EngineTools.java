/**
 * Copyright 2017-2020 ZTE Corporation.
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

package org.onap.holmes.rulemgt.tools;

import lombok.extern.slf4j.Slf4j;
import org.jvnet.hk2.annotations.Service;
import org.onap.holmes.common.api.entity.CorrelationRule;
import org.onap.holmes.common.engine.entity.EngineEntity;
import org.onap.holmes.common.engine.service.EngineEntityService;
import org.onap.holmes.rulemgt.wrapper.RuleQueryWrapper;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EngineTools {

    @Inject
    private RuleQueryWrapper ruleQueryWrapper;
    @Inject
    private EngineEntityService engineEntityService;

    public List<String> getInstanceList() {
        List<EngineEntity> entities = engineEntityService.getAllEntities();
        return entities.stream().map(EngineEntity::getIp).collect(Collectors.toList());
    }

    public List<String> getLegacyEngineInstances() {
        return engineEntityService.getLegacyEngines();
    }

    public String getEngineWithLeastRules() {
        LinkedHashMap<String, Integer> ruleNumInEachEngine = new LinkedHashMap<>();

        try {
            for (String ip : getInstanceList()) {
                List<CorrelationRule> rules = ruleQueryWrapper.queryRuleByEngineInstance(ip);
                if (rules != null) {
                    ruleNumInEachEngine.put(ip, rules.size());
                }
            }
        } catch (Exception e) {
            log.error("getEngineWithLeastRules failed!" + e.getMessage());
        }

        Integer[] numOfRules = ruleNumInEachEngine.values().toArray(new Integer[0]);
        Arrays.sort(numOfRules);

        for (String ip : ruleNumInEachEngine.keySet()) {
            if (ruleNumInEachEngine.get(ip) == numOfRules[0]) {
                return ip;
            }
        }
        return null;
    }
}

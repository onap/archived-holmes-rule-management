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

package org.onap.holmes.rulemgt;

import lombok.extern.slf4j.Slf4j;
import org.jvnet.hk2.annotations.Service;
import org.onap.holmes.common.api.entity.CorrelationRule;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.common.utils.DbDaoUtil;
import org.onap.holmes.rulemgt.bolt.enginebolt.EngineWrapper;
import org.onap.holmes.rulemgt.db.CorrelationRuleDao;
import org.onap.holmes.rulemgt.tools.EngineTools;
import org.onap.holmes.rulemgt.wrapper.RuleMgtWrapper;
import org.onap.holmes.rulemgt.wrapper.RuleQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
@Service
public class RuleAllocator {
    private static final Logger LOGGER = LoggerFactory.getLogger(RuleAllocator.class);

    public final static int ENABLE = 1;
    private RuleMgtWrapper ruleMgtWrapper;
    private RuleQueryWrapper ruleQueryWrapper;
    private EngineWrapper engineWrapper;
    private EngineTools engineTools;
    private CorrelationRuleDao correlationRuleDao;

    @Inject
    public RuleAllocator(RuleMgtWrapper ruleMgtWrapper, RuleQueryWrapper ruleQueryWrapper,
                         EngineWrapper engineWrapper, EngineTools engineTools, DbDaoUtil daoUtil) {
        this.ruleMgtWrapper = ruleMgtWrapper;
        this.ruleQueryWrapper = ruleQueryWrapper;
        this.engineWrapper = engineWrapper;
        this.engineTools = engineTools;
        correlationRuleDao = daoUtil.getJdbiDaoByOnDemand(CorrelationRuleDao.class);
    }

    @PostConstruct
    private void initialize() {
        new Timer("RuleAllocatorTimer").schedule(new TimerTask() {

            public void run() {
                try {
                    allocateRules();
                } catch (Exception e) {
                    LOGGER.error("Failed to reallocate rules.", e);
                }
            }

        }, SECONDS.toMillis(10), SECONDS.toMillis(30));
    }

    public synchronized void allocateRules() throws Exception {
        List<String> engines = engineTools.getInstanceList();

        if (engines == null) {
            return;
        }

        int numOfEngines = engines.size();
        LOGGER.info(String.format("There are %d engine instance(s) running currently.", numOfEngines));

        List<String> legacyEngineInstances = engineTools.getLegacyEngineInstances();
        if (legacyEngineInstances == null) {
            return;
        }

        if (legacyEngineInstances.size() < numOfEngines) {
            //extend
            List<CorrelationRule> rules2Allocate = calculateRule(legacyEngineInstances, numOfEngines);
            List<CorrelationRule> rules2Delete = copyList(rules2Allocate);
            List<String> newInstanceIds = sortOutNewEngineInstances(engines, legacyEngineInstances);
            distributeRules(newInstanceIds, rules2Allocate);
            cleanUpRulesFromEngines(rules2Delete, legacyEngineInstances);
        } else {
            //destroy
            List<String> destroyed = getDestroyedEngines(engines, legacyEngineInstances);
            distributeRules(getRemainingEngines(engines, destroyed), getRules(destroyed));
        }
    }

    private List<CorrelationRule> copyList(List<CorrelationRule> rules) {
        List<CorrelationRule> ret = new ArrayList<>(rules.size());
        for (CorrelationRule r : rules) {
            ret.add((CorrelationRule) r.clone());
        }
        return ret;
    }

    // When the engine is expanding, the rules that need to be allocated are calculated.
    private List<CorrelationRule> calculateRule(List<String> existingEngineIps,
                                                int latestEngineInsNum) throws CorrelationException {
        List<CorrelationRule> enabledRules = ruleQueryWrapper.queryRuleByEnable(ENABLE);
        int ruleCount = 0;
        if (enabledRules != null) {
            ruleCount = enabledRules.size();
        }
        // Average number of rule that's to be allocate into each instance
        int count = ruleCount / latestEngineInsNum;
        // The number of remaining rules (to be allocated) after each instance has been allocated with the average number of rules.
        int remainder = ruleCount % latestEngineInsNum;

        List<CorrelationRule> ret = new ArrayList<>();
        for (String ip : existingEngineIps) {
            List<CorrelationRule> rules = ruleQueryWrapper.queryRuleByEngineInstance(ip);
            List<CorrelationRule> tmp = rules.subList(count + (remainder-- / existingEngineIps.size()), rules.size());
            ret.addAll(tmp);
        }
        return ret;
    }

    // Rules that need to be allocated after the engine is destroyed
    private List<CorrelationRule> getRules(List<String> destroyIpList) throws CorrelationException {
        List<CorrelationRule> rules = new ArrayList<>();
        try {
            if (destroyIpList != null) {
                for (String ip : destroyIpList) {
                    rules.addAll(ruleQueryWrapper.queryRuleByEngineInstance(ip));
                }
            }
        } catch (CorrelationException e) {
            LOGGER.error("method getRules get data from DB failed !", e);
        }
        return rules;
    }

    // Extended IP
    private List<String> sortOutNewEngineInstances(List<String> newIps, List<String> oldIps) {
        List<String> ret = new ArrayList<>();

        for (String ip : newIps) {
            if (!oldIps.contains(ip)) {
                ret.add(ip);
            }
        }
        return ret;
    }

    // Destroyed IP
    private List<String> getDestroyedEngines(List<String> latest, List<String> existing) {
        List<String> ret = new ArrayList<>();
        for (String ip : existing) {
            if (!latest.contains(ip)) {
                ret.add(ip);
            }
        }
        return ret;
    }

    // Residual IP after destruction
    private List<String> getRemainingEngines(List<String> all, List<String> destroyed) {
        List<String> ret = new ArrayList<>();
        for (String ip : all) {
            if (!destroyed.contains(ip)) {
                ret.add(ip);
            }
        }
        return ret;
    }

    private void distributeRules(List<String> instanceIps, List<CorrelationRule> rules) throws CorrelationException {
        List<String> sortedIps = sortIpByRuleNumDesc(instanceIps);

        for (int i = 0, j = 0; j < rules.size(); i++, j++) {
            int index = i % sortedIps.size();
            String ip = sortedIps.get(index);
            CorrelationRule rule = rules.get(j);
            rule.setEngineInstance(ip);
            allocateRule(rule, ip);
        }
    }

    // Sorted by the number of rules each engine contains, in a descending order.
    private List<String> sortIpByRuleNumDesc(List<String> ips) {
        List<CorrelationRule> rules = null;
        Map<String, Integer> ruleNumOfEngines = new HashMap();

        try {
            for (String ip : ips) {
                rules = ruleQueryWrapper.queryRuleByEngineInstance(ip);
                if (rules != null) {
                    ruleNumOfEngines.put(ip, rules.size());
                }
            }
        } catch (Exception e) {
            LOGGER.error("getEngineWithLeastRules failed !", e);
        }

        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(ruleNumOfEngines.entrySet());
        Collections.sort(sortedEntries, (o1, o2) -> o2.getValue() - o1.getValue());

        List<String> ret = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : sortedEntries) {
            ret.add(entry.getKey());
        }
        return ret;
    }

    private void allocateRule(CorrelationRule rule, String ip) throws CorrelationException {
        try {
            ruleMgtWrapper.deployRule2Engine(rule, ip);
            correlationRuleDao.updateRule(rule);
        } catch (CorrelationException e) {
            throw new CorrelationException(String.format("Failed to allocate rule <%s> to <%s>",
                    rule.getName(), ip), e);
        }
    }

    private void cleanUpRulesFromEngines(List<CorrelationRule> rules, List<String> ipList) {
        try {
            for (String ip : ipList) {
                for (CorrelationRule rule : rules) {
                    if (ip.equals(rule.getEngineInstance())) {
                        engineWrapper.deleteRuleFromEngine(rule.getPackageName(), ip);
                    }
                }
            }
        } catch (CorrelationException e) {
            LOGGER.error("When the engine is extended, deleting rule failed", e);
        }
    }
}

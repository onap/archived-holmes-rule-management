/**
 * Copyright 2017 ZTE Corporation.
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

package org.onap.holmes.rulemgt.send;

import lombok.extern.slf4j.Slf4j;
import org.glassfish.hk2.api.ServiceLocator;
import org.onap.holmes.common.api.entity.CorrelationRule;
import org.onap.holmes.common.dropwizard.ioc.utils.ServiceLocatorHolder;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.common.utils.DbDaoUtil;
import org.onap.holmes.rulemgt.bolt.enginebolt.EngineWrapper;
import org.onap.holmes.rulemgt.db.CorrelationRuleDao;
import org.onap.holmes.rulemgt.msb.EngineInsQueryTool;
import org.onap.holmes.rulemgt.wrapper.RuleMgtWrapper;
import org.onap.holmes.rulemgt.wrapper.RuleQueryWrapper;

import java.util.*;


@Slf4j
public class RuleAllocator {
    public final static int ENABLE = 1;
    private RuleMgtWrapper ruleMgtWrapper;
    private RuleQueryWrapper ruleQueryWrapper;
    private EngineWrapper engineWrapper;
    private EngineInsQueryTool engineInsQueryTool;
    private DbDaoUtil daoUtil;
    private CorrelationRuleDao correlationRuleDao;
    private int latestEngineInsNum = 0;
    private List<String> existingEngineServiceIps = new ArrayList<>();
    private List<String> latestEngineServiceIps = new ArrayList<>();

    public RuleAllocator() {
        ServiceLocator locator = ServiceLocatorHolder.getLocator();
        ruleMgtWrapper = locator.getService(RuleMgtWrapper.class);
        ruleQueryWrapper = locator.getService(RuleQueryWrapper.class);
        engineWrapper = locator.getService(EngineWrapper.class);
        engineInsQueryTool = locator.getService(EngineInsQueryTool.class);
        daoUtil = locator.getService(DbDaoUtil.class);

        initDaoUtilAndEngineIp();
    }

    private void initDaoUtilAndEngineIp() {
        correlationRuleDao = daoUtil.getJdbiDaoByOnDemand(CorrelationRuleDao.class);
        try {
            existingEngineServiceIps = engineInsQueryTool.getInstanceList();

        } catch (Exception e) {
            log.warn("Failed to get the number of engine instances.", e);
        }
    }

    public synchronized void allocateRules(List<String> latestEngineIps) throws Exception {
        if (latestEngineIps == null) {
            throw new NullPointerException("The parameter of " + this.getClass().getSimpleName()
                    + ".allocateRules(List<String>) can not be null!");
        }

        latestEngineServiceIps = latestEngineIps;
        latestEngineInsNum = latestEngineIps.size();
        if (existingEngineServiceIps.size() < latestEngineInsNum) {
            //extend
            List<CorrelationRule> rules2Allocate = calculateRule(existingEngineServiceIps);
            List<CorrelationRule> rules2Delete = copyList(rules2Allocate);
            List<String> newInstanceIds = sortOutNewEngineInstances(latestEngineServiceIps, existingEngineServiceIps);
            distributeRules(newInstanceIds, rules2Allocate);
            cleanUpRulesFromEngines(rules2Delete, existingEngineServiceIps);
        } else if (existingEngineServiceIps.size() > latestEngineInsNum) {
            //destroy
            List<String> destroyed = getDestroyedEngines(latestEngineServiceIps, existingEngineServiceIps);
            distributeRules(getRemainingEngines(destroyed), reallocateRules(destroyed));
        }

        existingEngineServiceIps = latestEngineServiceIps;
    }

    private List<CorrelationRule> copyList(List<CorrelationRule> rules) {
        List<CorrelationRule> ret = new ArrayList<>(rules.size());
        for (CorrelationRule r : rules) {
            ret.add((CorrelationRule) r.clone());
        }
        return ret;
    }

    // When the engine is expanding, the rules that need to be allocated are calculated.
    private List<CorrelationRule> calculateRule(List<String> existingEngineIps) throws CorrelationException {
        List<CorrelationRule> enabledRules = ruleQueryWrapper.queryRuleByEnable(ENABLE);
        int ruleCount = 0;
        if (enabledRules != null) {
            ruleCount = enabledRules.size();
        }
        int count = ruleCount / latestEngineInsNum;
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
    private List<CorrelationRule> reallocateRules(List<String> destroyIpList) throws CorrelationException {
        List<CorrelationRule> rules = new ArrayList<>();
        try {
            if (destroyIpList != null) {
                for (String ip : destroyIpList) {
                    rules.addAll(ruleQueryWrapper.queryRuleByEngineInstance(ip));
                }
            }
        } catch (CorrelationException e) {
            log.error("method reallocateRules get data from DB failed !", e);
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
    private List<String> getRemainingEngines(List<String> destroyed) {
        List<String> ret = new ArrayList<>();
        for (String ip : latestEngineServiceIps) {
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
            log.error("getEngineIp4AddRule failed !", e);
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
            throw new CorrelationException("allocate Deploy Rule failed", e);
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
            log.error("When the engine is extended, deleting rule failed", e);
        }
    }
}

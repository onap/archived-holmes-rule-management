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

package org.onap.holmes.rulemgt.send;

import lombok.extern.slf4j.Slf4j;
import org.jvnet.hk2.annotations.Service;
import org.onap.holmes.common.api.entity.CorrelationRule;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.common.utils.DbDaoUtil;
import org.onap.holmes.rulemgt.bolt.enginebolt.EngineWrapper;
import org.onap.holmes.rulemgt.db.CorrelationRuleDao;
import org.onap.holmes.rulemgt.msb.EngineIpList;
import org.onap.holmes.rulemgt.wrapper.RuleQueryWrapper;
import org.onap.holmes.rulemgt.wrapper.RuleMgtWrapper;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;


@Service
@Slf4j
public class RuleAllocation {

    private final static int ENABLE = 1;

    @Inject
    private RuleMgtWrapper ruleMgtWrapper;
    @Inject
    private RuleQueryWrapper ruleQueryWrapper;
    @Inject
    private EngineWrapper engineWrapper;
    @Inject
    private EngineIpList engineIpList;
    @Inject
    private DbDaoUtil daoUtil;

    private CorrelationRuleDao correlationRuleDao;

    private int ruleCount;
    private int serviceCount;
    private List<String> temIpList = new ArrayList<>();
    private List<String> engineService = new ArrayList<>();
    private List<CorrelationRule> allRules = new ArrayList<>();

    @PostConstruct
    public void initDaoUtilAndEngineIp() throws Exception{
        correlationRuleDao = daoUtil.getJdbiDaoByOnDemand(CorrelationRuleDao.class);
        temIpList =  engineIpList.getServiceCount();
    }

    public void judgeAndAllocateRule(List<String> ipList)throws Exception{
        if(ipList != null) {
            engineService = ipList;
            serviceCount = ipList.size();
        }
        if(temIpList.size() < serviceCount){
            //extend
            List<CorrelationRule> deleteRule = calculateRule(temIpList);
            List<CorrelationRule> allocateRule =  calculateRule(temIpList);
            List<String> extendIp = extendCompareIp(engineService,temIpList);
            AllocateService(extendIp,allocateRule);
            deleteRuleFromFormerEngine(deleteRule,temIpList);

        } else if (temIpList.size() > serviceCount) {
            //destroy
            List<String> destroyIp = destroyCompareIp(engineService, temIpList);
            AllocateService(restIp(destroyIp), relocateRuleAfterDestroy(destroyIp));

        } else if(temIpList.size() == serviceCount) {
            temIpList = engineService;
            return;
        }
        temIpList = engineService;

    }


    // When the engine is expanding, the rules that need to be allocated are calculated.
    private List<CorrelationRule> calculateRule(List<String> oldIpList) throws Exception{
        allRules = ruleQueryWrapper.queryRuleByEnable(ENABLE);
        if(allRules != null) {
            ruleCount = allRules.size();
        }
        int count = ruleCount / serviceCount;
        int remainder = ruleCount % serviceCount;

        List<CorrelationRule> subRule = new ArrayList<>();
        for(String ip : oldIpList) {
            List<CorrelationRule> rules = ruleQueryWrapper.queryRuleByEngineInstance(ip);
            List<CorrelationRule> tem = rules.subList(count + (remainder-- / oldIpList.size()),rules.size());
            subRule.addAll(tem);
        }
        return subRule;
    }

    //Rules that need to be allocated after the engine is destroyed
    private List<CorrelationRule> relocateRuleAfterDestroy(List<String> destroyIpList) throws CorrelationException {
        List<CorrelationRule> rules = new ArrayList<>();
        try{
            if(destroyIpList != null){
                for(String ip : destroyIpList) {
                    rules.addAll(ruleQueryWrapper.queryRuleByEngineInstance(ip));
                }
            }
        }catch(CorrelationException e) {
            log.error("method relocateRuleAfterDestroy get data from DB failed !" +e.getMessage());
        }
        return rules;
    }

    //Extended IP
    private List<String> extendCompareIp(List<String> newList, List<String> oldList){
        List<String> extendIpList = new ArrayList<>();

        for( String ip :newList) {
            if(! oldList.contains(ip)) {
                extendIpList.add(ip);
            }
        }
        return extendIpList;
    }

    //Destroyed IP
    private List<String> destroyCompareIp(List<String> newList, List<String> oldList) {
        List<String> destroyIpList = new ArrayList<>();
        for(String ip : oldList) {
            if(!newList.contains(ip)) {
                destroyIpList.add(ip);
            }
        }
        return destroyIpList;
    }

    //Residual IP after destruction
    private List<String> restIp(List<String> destroyIp) {
        List<String> restIpList = new ArrayList<>();
        for(String ip : engineService) {
            if(!destroyIp.contains(ip)) {
                restIpList.add(ip);
            }
        }
        return restIpList;
    }

    public void AllocateService(List<String> extendIpList, List<CorrelationRule> subList) throws Exception{
        List<String> needIpList = getSortIp(extendIpList);

        for(int i=0,j=0;j < subList.size();i++,j++ ){
            int index = i % needIpList.size();
            String deployIp = needIpList.get(index);
            CorrelationRule rule = subList.get(j);
            rule.setEngineInstance(deployIp);
            allocateDeployRule(rule,deployIp);
        }
    }

    //The IP to be allocated is in ascending order, and the least is circulate.
    private List<String > getSortIp(List<String> ipList){
        List<CorrelationRule> ipRuleList  = new ArrayList<>();
        HashMap<String,String> hashMap = new HashMap();

        try{
            for(String ip : ipList){
                ipRuleList = ruleQueryWrapper.queryRuleByEngineInstance(ip);
                if(ipRuleList != null) {
                    hashMap.put(ip, String.valueOf(ipRuleList.size()));
                }
            }
        }catch (Exception e){
            log.error("getEngineIp4AddRule failed !" + e.getMessage());
        }

        List<Map.Entry<String, String>> list_Data = new ArrayList<Map.Entry<String, String>>(hashMap.entrySet());
        Collections.sort(list_Data, new Comparator<Map.Entry<String, String>>() {
            public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2)
            {
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        List<String> needList = new ArrayList<>();
        for(Map.Entry<String, String> map: list_Data) {
            String key = map.getKey();
            needList.add(key);
        }
        return needList;
    }

    private void allocateDeployRule(CorrelationRule rule, String ip) throws CorrelationException {
        try{
            ruleMgtWrapper.deployRule2Engine(rule,ip);
            correlationRuleDao.updateRule(rule);
        }catch (CorrelationException e){
            throw new CorrelationException("allocate Deploy Rule failed", e);
        }
    }

    private void deleteRuleFromFormerEngine(List<CorrelationRule> subRule, List<String> oldList) {
        try{
            for(String ip : oldList){
                for(CorrelationRule rule: subRule) {
                    if(ip.equals(rule.getEngineInstance())) {
                        engineWrapper.deleteRuleFromEngine(rule.getPackageName(),ip);
                    }
                }
            }
        }catch (CorrelationException e) {
            log.error("When the engine is extended, deleting rule failed" +e.getMessage());
        }

    }

}

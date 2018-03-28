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
import org.onap.holmes.rulemgt.wrapper.RuleQueryWrapper;

import javax.inject.Inject;
import java.util.*;

@Service
@Slf4j
public class Ip4AddingRule {

    @Inject
    private RuleQueryWrapper ruleQueryWrapper;
    private List<String> engineService;

    public void getIpList(List<String> ipList){
        engineService = ipList;
    }

    public String getEngineIp4AddRule() {
        List<CorrelationRule> ipRuleList  = new ArrayList<>();
        LinkedHashMap<String,Integer> linkedHashMap = new LinkedHashMap();

        try{
            for(String ip : engineService){
                ipRuleList = ruleQueryWrapper.queryRuleByEngineInstance(ip);
                if(ipRuleList != null) {
                    linkedHashMap.put(ip, ipRuleList.size());
                }
            }
        }catch (Exception e){
            log.error("getEngineIp4AddRule failed !" + e.getMessage());
        }

        //min
        Collection<Integer> c = linkedHashMap.values();
        Object[] obj = c.toArray();
        Arrays.sort(obj);
        String ip = null;
        for(String getKey: linkedHashMap.keySet()){
            if(linkedHashMap.get(getKey).equals(obj[0])){
                ip = getKey;
            }
        }
        return ip;
    }
}

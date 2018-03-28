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
package org.onap.holmes.rulemgt.msb;

import lombok.extern.slf4j.Slf4j;
import org.jvnet.hk2.annotations.Service;
import org.onap.holmes.rulemgt.send.RuleAllocation;
import org.onap.holmes.rulemgt.send.Ip4AddingRule;
import org.onap.holmes.rulemgt.wrapper.RuleMgtWrapper;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


@Service
@Slf4j
public class MsbQuery {

    @Inject
    private RuleAllocation ruleAllocation;

    @Inject
    private Ip4AddingRule ip4AddingRule;

    @Inject
    private EngineIpList engineIpList;

    @Inject
    private RuleMgtWrapper ruleMgtWrapper;

    private  List<String> timerIpList;

    @PostConstruct
    public void init() {

        try{
            timer();
        }catch(Exception e){
            log.error("MSBQuery init timer task failed !" + e.getMessage());
        }

    }

    public void timer() throws Exception{
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            public void run() {
                try {
                    timerIpList = engineIpList.getServiceCount();
                    ip4AddingRule.getIpList(timerIpList);
                    ruleAllocation.judgeAndAllocateRule(timerIpList);

                } catch (Exception e) {
                    log.error("The timing query engine instance failed " ,e);
                }
            }

        }, 5000, 30000);

    }

}

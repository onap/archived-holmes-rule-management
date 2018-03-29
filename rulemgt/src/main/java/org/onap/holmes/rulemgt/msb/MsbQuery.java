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
package org.onap.holmes.rulemgt.msb;

import lombok.extern.slf4j.Slf4j;
import org.glassfish.hk2.api.ServiceLocator;
import org.onap.holmes.common.dropwizard.ioc.utils.ServiceLocatorHolder;
import org.onap.holmes.rulemgt.send.RuleAllocation;
import org.onap.holmes.rulemgt.send.Ip4AddingRule;
import org.onap.holmes.rulemgt.wrapper.RuleMgtWrapper;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


@Slf4j
public class MsbQuery {

    private RuleAllocation ruleAllocation;

    private Ip4AddingRule ip4AddingRule;

    private EngineIpList engineIpList;

    private RuleMgtWrapper ruleMgtWrapper;

    private List<String> timerIpList;

    public MsbQuery() {
        ruleAllocation = new RuleAllocation();

        ServiceLocator locator = ServiceLocatorHolder.getLocator();
        ip4AddingRule = locator.getService(Ip4AddingRule.class);
        engineIpList = locator.getService(EngineIpList.class);
        ruleMgtWrapper = locator.getService(RuleMgtWrapper.class);
    }

    public void startTimer() {
        try {
            timer();
        } catch (Exception e) {
            log.error("MSBQuery startTimer timer task failed !" + e.getMessage(), e);
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e1) {
                Thread.currentThread().interrupt();
            }
        }


    }

    public void timer() throws Exception {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            public void run() {
                try {
                    timerIpList = engineIpList.getServiceCount();
                    log.info(String.format("There are %d engine instance(s) running currently.", timerIpList.size()));

                    ip4AddingRule.setIpList(timerIpList);
                    ruleAllocation.judgeAndAllocateRule(timerIpList);
                } catch (Exception e) {
                    log.error("The timing query engine instance failed ", e);
                }
            }

        }, 10000, 30000);

    }

}

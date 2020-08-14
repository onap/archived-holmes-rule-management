/**
 * Copyright 2017-2018 ZTE Corporation.
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

import io.dropwizard.setup.Environment;
import org.onap.holmes.common.config.MicroServiceConfig;
import org.onap.holmes.common.dropwizard.ioc.bundle.IOCApplication;
import org.onap.holmes.common.exception.CorrelationException;
import org.onap.holmes.common.utils.HttpsUtils;
import org.onap.holmes.common.utils.MSBRegisterUtil;
import org.onap.holmes.common.utils.transactionid.TransactionIdFilter;
import org.onap.holmes.rulemgt.dcae.DcaeConfigurationPolling;
import org.onap.holmes.rulemgt.msb.MsbQuery;
import org.onap.holmes.rulemgt.resources.RuleMgtResources;
import org.onap.msb.sdk.discovery.entity.MicroServiceInfo;
import org.onap.msb.sdk.discovery.entity.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RuleActiveApp extends IOCApplication<RuleAppConfig> {

    private Logger log = LoggerFactory.getLogger(RuleActiveApp.class);

    public static void main(String[] args) throws Exception {
        new RuleActiveApp().run(args);
    }

    @Override
    public void run(RuleAppConfig configuration, Environment environment) throws Exception {
        super.run(configuration, environment);

        try {
            new MSBRegisterUtil().register2Msb(createMicroServiceInfo());
        } catch (CorrelationException e) {
            log.warn(e.getMessage(), e);
        }

        if (!"1".equals(System.getenv("TESTING"))) {
            ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
            service.scheduleAtFixedRate(
                    new DcaeConfigurationPolling(MicroServiceConfig.getEnv(MicroServiceConfig.HOSTNAME)), 0,
                    DcaeConfigurationPolling.POLLING_PERIOD, TimeUnit.MILLISECONDS);
        }

        environment.servlets().addFilter("customFilter", new TransactionIdFilter()).addMappingForUrlPatterns(EnumSet
                .allOf(DispatcherType.class), true, "/*");

        new MsbQuery().startTimer();
    }

    private MicroServiceInfo createMicroServiceInfo() {
        String msbAddrTemplate = (HttpsUtils.isHttpsEnabled() ? "https" : "http")
                + "://%s:%s/api/holmes-rule-mgmt/v1/healthcheck";
        String[] serviceAddrInfo = MicroServiceConfig.getMicroServiceIpAndPort();
        MicroServiceInfo msinfo = new MicroServiceInfo();
        msinfo.setServiceName("holmes-rule-mgmt");
        msinfo.setVersion("v1");
        msinfo.setUrl("/api/holmes-rule-mgmt/v1");
        msinfo.setProtocol("REST");
        msinfo.setVisualRange("0|1");
        msinfo.setEnable_ssl(HttpsUtils.isHttpsEnabled());
        Set<Node> nodes = new HashSet<>();
        Node node = new Node();
        node.setIp(serviceAddrInfo[0]);
        node.setPort("9101");
        node.setCheckType("HTTP");
        node.setCheckUrl(String.format(msbAddrTemplate, serviceAddrInfo[0], "9101"));
        node.setCheckTimeOut("60s");
        node.setCheckInterval("60s");
        nodes.add(node);
        msinfo.setNodes(nodes);
        return msinfo;
    }
}


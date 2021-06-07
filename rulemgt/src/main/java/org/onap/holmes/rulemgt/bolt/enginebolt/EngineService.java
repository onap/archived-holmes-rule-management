/**
 * Copyright 2017-2021 ZTE Corporation.
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
package org.onap.holmes.rulemgt.bolt.enginebolt;

import org.jvnet.hk2.annotations.Service;
import org.onap.holmes.common.utils.CommonUtils;
import org.onap.holmes.common.utils.JerseyClient;
import org.onap.holmes.rulemgt.bean.request.CorrelationCheckRule4Engine;
import org.onap.holmes.rulemgt.bean.request.CorrelationDeployRule4Engine;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import static org.onap.holmes.rulemgt.constant.RuleMgtConstant.ENGINE_PATH;

@Service
public class EngineService {

    private static final String PORT = "9102";
    private static final String SEP = "//";
    private static final String COLON = ":";

    protected boolean delete(String packageName, String ip) {
        return new JerseyClient()
                .path(packageName)
                .delete(getUrl(ip)) != null;
    }

    protected boolean check(CorrelationCheckRule4Engine correlationCheckRule4Engine, String ip) {
        return new JerseyClient()
                .header("Accept", MediaType.APPLICATION_JSON)
                .post(getUrl(ip), Entity.json(correlationCheckRule4Engine)) != null;
    }

    protected String deploy(CorrelationDeployRule4Engine correlationDeployRule4Engine, String ip) {
        return new JerseyClient()
                .header("Accept", MediaType.APPLICATION_JSON)
                .put(getUrl(ip), Entity.json(correlationDeployRule4Engine));
    }

    private String getRequestPref() {
        return CommonUtils.isHttpsEnabled() ? JerseyClient.PROTOCOL_HTTPS : JerseyClient.PROTOCOL_HTTP;
    }

    private String getUrl(String ip) {
        return new StringBuilder(getRequestPref())
                .append(COLON)
                .append(SEP)
                .append(ip)
                .append(COLON)
                .append(PORT)
                .append(ENGINE_PATH)
                .toString();
    }
}

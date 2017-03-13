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
package org.openo.holmes.rulemgt.bolt.enginebolt;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.client.ClientConfig;
import org.jvnet.hk2.annotations.Service;
import org.openo.holmes.rulemgt.bean.request.CorrelationCheckRule4Engine;
import org.openo.holmes.rulemgt.bean.request.CorrelationDeployRule4Engine;
import org.openo.holmes.rulemgt.constant.RuleMgtConstant;

@Slf4j
@Service
public class EngineService {

    String url = "http://10.250.0.3:9102";

    protected Response delete(String packageName) throws IOException {
        Client client = createClient();
        WebTarget webTarget = client.target(url + RuleMgtConstant.ENGINE_PATH + "/" + packageName);
        return webTarget.request(MediaType.APPLICATION_JSON).delete();
    }

    private Client createClient() {
        ClientConfig clientConfig = new ClientConfig();
        return ClientBuilder.newClient(clientConfig);
    }

    protected Response check(CorrelationCheckRule4Engine correlationCheckRule4Engine)
            throws IOException {
        Client client = createClient();
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(correlationCheckRule4Engine);
        WebTarget webTarget = client.target(url + RuleMgtConstant.ENGINE_PATH);
        return webTarget.request(MediaType.APPLICATION_JSON).post(Entity.entity(content, MediaType.APPLICATION_JSON));
    }

    protected Response deploy(CorrelationDeployRule4Engine correlationDeployRule4Engine) throws IOException {
        Client client = createClient();
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(correlationDeployRule4Engine);
        WebTarget webTarget = client.target(url + RuleMgtConstant.ENGINE_PATH);
        return webTarget.request(MediaType.APPLICATION_JSON).put(Entity.entity(content, MediaType.APPLICATION_JSON));
    }
}

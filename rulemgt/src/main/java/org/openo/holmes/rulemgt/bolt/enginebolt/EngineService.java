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

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.jvnet.hk2.annotations.Service;
import org.openo.holmes.common.config.MicroServiceConfig;
import org.openo.holmes.common.exception.CorrelationException;
import org.openo.holmes.common.utils.I18nProxy;
import org.openo.holmes.rulemgt.bean.request.CorrelationCheckRule4Engine;
import org.openo.holmes.rulemgt.bean.request.CorrelationDeployRule4Engine;
import org.openo.holmes.rulemgt.constant.RuleMgtConstant;

@Slf4j
@Service
public class EngineService {

    String url = "http://10.250.0.3:9102";

    protected HttpResponse delete(String packageName) throws IOException {
        return deleteRequest(url + RuleMgtConstant.ENGINE_PATH + "/" + packageName);
    }

    protected HttpResponse check(CorrelationCheckRule4Engine correlationCheckRule4Engine)
            throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(correlationCheckRule4Engine);
        String queryUrl = MicroServiceConfig.getMsbServerAddr()
                + "/openoapi/microservices/v1/services/holmes-engine/version/v1";
        HttpGet httpGet = new HttpGet(queryUrl);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);
            log.info("response entity:" + EntityUtils.toString(httpResponse.getEntity()));
        } finally {
            httpClient.close();
        }
        return postRequest(url + RuleMgtConstant.ENGINE_PATH, content);
    }

    protected HttpResponse deploy(CorrelationDeployRule4Engine correlationDeployRule4Engine) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(correlationDeployRule4Engine);
        return putRequest(url + RuleMgtConstant.ENGINE_PATH, content);
    }

    private HttpResponse postRequest(String url, String content) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost(url);
            log.info("url:" + url + "," + "post:" + httpPost);
            setHeader(httpPost);
            if (StringUtils.isNotEmpty(content)) {
                httpPost.setEntity(new ByteArrayEntity(content.getBytes()));
            }
            return httpClient.execute(httpPost);
        } finally {
            httpClient.close();
        }
    }

    private HttpResponse putRequest(String url, String content) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpPut httpPut = new HttpPut(url);
            setHeader(httpPut);
            if (StringUtils.isNotEmpty(content)) {
                httpPut.setEntity(new ByteArrayEntity(content.getBytes()));
            }
            HttpResponse response = httpClient.execute(httpPut);
            log.info("Return value for put request is " + EntityUtils.toString(response.getEntity()) + ".");
            return response;
        } finally {
            httpClient.close();
        }
    }

    private HttpResponse deleteRequest(String url) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpDelete httpDelete = new HttpDelete(url);
            setHeader(httpDelete);
            return httpClient.execute(httpDelete);
        } finally {
            httpClient.close();
        }
    }

    private void setHeader(HttpRequestBase httpRequestBase) {
        httpRequestBase.setHeader("Accept", "application/json");
        httpRequestBase.setHeader("Content-Type", "application/json");
    }

    public String getResponseContent(HttpResponse response) {
        HttpEntity entity = response.getEntity();
        InputStream is = null;
        if (entity != null) {
            try {
                is = entity.getContent();
                final ContentType contentType = ContentType.getOrDefault(entity);
                Charset charset = contentType.getCharset();
                if (charset == null) {
                    charset = HTTP.DEF_CONTENT_CHARSET;
                }
                final StringBuilder b = new StringBuilder();
                final char[] tmp = new char[1024];
                final Reader reader = new InputStreamReader(is, charset);
                try {
                    int l;
                    while ((l = reader.read(tmp)) != -1) {
                        b.append(tmp, 0, l);
                    }
                } catch (ConnectionClosedException ignore) {

                } catch (IOException e) {
                    log.info("Failed to read the contents of the input stream of the http entity.", e);
                }
                return b.toString();
            } catch (IOException e) {
                log.info("Failed to read the contents of the http entity.", e);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    log.info("Failed to close the input stream of the http entity.", e);
                }
            }
        }
        return "{}";
    }

    public byte[] getData(HttpEntity httpEntity) throws IOException {
        log.info("Rule deployed. Package name: " + httpEntity.getContent().toString()
                + ". Content length: " + httpEntity.getContentLength());
        BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(httpEntity);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bufferedHttpEntity.writeTo(byteArrayOutputStream);
        byte[] responseBytes = byteArrayOutputStream.toByteArray();
        return responseBytes;
    }

//    public String getResponseContent(HttpResponse httpResponse) throws CorrelationException {
//        byte[] dataByte;
//        String result = null;
//        try {
//            HttpEntity httpEntity = httpResponse.getEntity();
//            if (httpEntity != null) {
//                byte[] responseBytes = getData(httpEntity);
//                dataByte = responseBytes;
//                result = bytesToString(dataByte);
//            }
//            return result;
//        } catch (Exception e) {
//            throw new CorrelationException(I18nProxy.RULE_MANAGEMENT_PARSE_DEPLOY_RESULT_ERROR, e);
//        }
//    }

    private String bytesToString(byte[] bytes) throws UnsupportedEncodingException {
        if (bytes != null) {
            String returnStr = new String(bytes, "utf-8");
            returnStr = StringUtils.trim(returnStr);
            return returnStr;
        }
        return null;
    }
}

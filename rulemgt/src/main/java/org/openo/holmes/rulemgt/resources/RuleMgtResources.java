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
package org.openo.holmes.rulemgt.resources;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.SwaggerDefinition;
import java.io.IOException;
import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.jvnet.hk2.annotations.Service;
import org.openo.holmes.common.api.entity.ServiceRegisterEntity;
import org.openo.holmes.common.config.MicroServiceConfig;
import org.openo.holmes.common.exception.CorrelationException;
import org.openo.holmes.common.utils.ExceptionUtil;
import org.openo.holmes.common.utils.I18nProxy;
import org.openo.holmes.common.utils.JacksonUtil;
import org.openo.holmes.common.utils.LanguageUtil;
import org.openo.holmes.common.utils.MSBRegisterUtil;
import org.openo.holmes.common.utils.UserUtil;
import org.openo.holmes.rulemgt.bean.request.RuleCreateRequest;
import org.openo.holmes.rulemgt.bean.request.RuleDeleteRequest;
import org.openo.holmes.rulemgt.bean.request.RuleQueryCondition;
import org.openo.holmes.rulemgt.bean.request.RuleUpdateRequest;
import org.openo.holmes.rulemgt.bean.response.RuleAddAndUpdateResponse;
import org.openo.holmes.rulemgt.bean.response.RuleQueryListResponse;
import org.openo.holmes.rulemgt.constant.RuleMgtConstant;
import org.openo.holmes.rulemgt.wrapper.RuleMgtWrapper;

@Service
@SwaggerDefinition
@Path("/rule")
@Api(tags = {"CorrelationRules"})
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class RuleMgtResources {

    @Inject
    private RuleMgtWrapper ruleMgtWrapper;

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Save the alarm+ rule to the database, and deployed to the engine when the enable to open.", response = RuleAddAndUpdateResponse.class)
    @Timed
    public RuleAddAndUpdateResponse addCorrelationRule(@Context HttpServletRequest request,
            @ApiParam(value = "alarm+ rule create request.<br>[rulename]:<font color=\"red\">required</font><br>[content]:<font color=\"red\">required</font><br>[enabled]:<font color=\"red\">required</font>", required = true) RuleCreateRequest ruleCreateRequest) {
        Locale locale = LanguageUtil.getLocale(request);
        RuleAddAndUpdateResponse ruleChangeResponse;
        try {
            ruleChangeResponse = ruleMgtWrapper
                    .addCorrelationRule(UserUtil.getUserName(request), ruleCreateRequest);
            log.info("create rule:" + ruleCreateRequest.getRuleName() + " success.");
            return ruleChangeResponse;
        } catch (CorrelationException e) {
            log.error("create rule:" + ruleCreateRequest.getRuleName() + " failed", e);
            throw ExceptionUtil.buildExceptionResponse(I18nProxy.getInstance().getValue(locale,
                    e.getMessage()));
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update the alarm+ rule and deployed to the engine when the enable to open.", response = RuleAddAndUpdateResponse.class)
    @Timed
    public RuleAddAndUpdateResponse updateCorrelationRule(@Context HttpServletRequest request,
            @ApiParam(value = "alarm+ rule update request.<br>[ruleid]:<font color=\"red\">required</font>", required = true) RuleUpdateRequest ruleUpdateRequest) {
        Locale locale = LanguageUtil.getLocale(request);
        RuleAddAndUpdateResponse ruleChangeResponse;
        try {
            ruleChangeResponse = ruleMgtWrapper.updateCorrelationRule(UserUtil.getUserName(request), ruleUpdateRequest);
            log.info("update rule:" + ruleUpdateRequest.getRuleId() + " successful");
            return ruleChangeResponse;
        } catch (CorrelationException e) {
            log.error("update rule:" + ruleUpdateRequest.getContent() + " failed", e);
            throw ExceptionUtil.buildExceptionResponse(I18nProxy.getInstance().getValue(locale,
                    e.getMessage()));
        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Delete the alarm+ rule,and when the enable is open also removed from the engine.")
    @Timed
    public boolean deleteCorrelationRule(@Context HttpServletRequest request,
            @ApiParam(value = "alarm+ rule delete request.<br>[ruleid]:<font color=\"red\">required</font>", required = true) RuleDeleteRequest ruleDeleteRequest) {
        Locale locale = LanguageUtil.getLocale(request);
        try {
            ruleMgtWrapper.deleteCorrelationRule(ruleDeleteRequest);
            log.info("delete rule:" + ruleDeleteRequest.getRuleId() + " successful");
            return true;
        } catch (CorrelationException e) {
            log.error("delete rule:" + ruleDeleteRequest.getRuleId() + " failed", e);
            throw ExceptionUtil.buildExceptionResponse(I18nProxy.getInstance().getValue(locale,
                    e.getMessage()));
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "According to the conditions query the alarm + rules", response = RuleQueryListResponse.class)
    @Timed
    public RuleQueryListResponse getCorrelationRules(@Context HttpServletRequest request,
            @ApiParam(value = "query condition:<br>" + " <b>[ruleid]</b>:Rule ID;<br>"
                    + "<b>[rulename]</b>:Rule name;<br>" + "<b>[creator]</b>:creator of the rule;<br>"
                    + "<b>[modifier]</b>:modifier of the rule;<br>"
                    + "<b>[enabled]</b>: 0 is Enabled,1 is disabled;<br><font color=\"red\">for example:</font><br>{\"ruleid\":\"rule_1484727187317\"}", required = false) @QueryParam("queryrequest") String ruleQueryRequest) {
        Locale locale = LanguageUtil.getLocale(request);
        RuleQueryListResponse ruleQueryListResponse;
        RuleQueryCondition ruleQueryCondition = getRuleQueryCondition(ruleQueryRequest, request);
        try {
            ruleQueryListResponse = ruleMgtWrapper
                    .getCorrelationRuleByCondition(ruleQueryCondition);
            log.info("query rule successful by condition:" + JSONObject.fromObject(ruleQueryCondition));
            return ruleQueryListResponse;
        } catch (CorrelationException e) {
            log.error("query rule failed,cause query condition conversion failure", e);
            throw ExceptionUtil.buildExceptionResponse(I18nProxy.getInstance().getValue(locale,
                    e.getMessage()));
        }
    }

    private RuleQueryCondition getRuleQueryCondition(String queryRequest,
            HttpServletRequest request) {
        Locale locale = LanguageUtil.getLocale(request);
        try {
            RuleQueryCondition ruleQueryCondition = JacksonUtil
                    .jsonToBean(queryRequest, RuleQueryCondition.class);
            if (queryRequest == null) {
                ruleQueryCondition.setEnabled(RuleMgtConstant.STATUS_RULE_ALL);
            } else if (queryRequest.indexOf("enabled") == -1) {
                ruleQueryCondition.setEnabled(RuleMgtConstant.STATUS_RULE_ALL);
            }
            return ruleQueryCondition;
        } catch (IOException e) {
            log.warn("queryRequest convert to json failed", e);
            throw ExceptionUtil.buildExceptionResponse(I18nProxy.getInstance().getValue(locale,
                    I18nProxy.RULE_MANAGEMENT_DATA_FORMAT_ERROR));
        }
    }
}

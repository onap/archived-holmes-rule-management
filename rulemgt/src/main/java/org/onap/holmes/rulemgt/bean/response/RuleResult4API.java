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
package org.onap.holmes.rulemgt.bean.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Setter
@Getter
public class RuleResult4API {
    @JsonProperty(value = "ruleid")
    private String ruleId;
    @JsonProperty(value = "rulename")
    private String ruleName;
    private String description;
    private String content;
    @JsonProperty(value = "createtime")
    private Date createTime;
    private String creator;
    @JsonProperty(value = "updatetime")
    private Date updateTime;
    private String modifier;
    private int enabled;
}

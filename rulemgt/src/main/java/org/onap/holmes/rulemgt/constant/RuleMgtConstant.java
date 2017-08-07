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
package org.onap.holmes.rulemgt.constant;

public class RuleMgtConstant {

    private RuleMgtConstant() {

    }
    public static final int STATUS_RULE_OPEN = 1;
    public static final int STATUS_RULE_CLOSE = 0;
    public static final int STATUS_RULE_ALL = 2;
    public static final String PACKAGE = "package";
    public static final String ENGINE_PATH = "/onapapi/holmes-engine-mgmt/v1/rule";
    public static final int RESPONSE_STATUS_OK = 200;
}

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

package org.openo.holmes.rulemgt;

import org.openo.dropwizard.ioc.bundle.IOCApplication;

public class RuleActiveApp extends IOCApplication < RuleAppConfig > {

    public static void main( String[] args ) throws Exception {
//        args = new String[]{ "server",
//                        "D:\\code\\open-o\\holmes-rule-management\\rulemgt-standalone\\src\\assembly\\resource\\conf\\holmes-rulemgt.yml" };
        new RuleActiveApp().run( args );
    }

    @Override
    public String getName() {
        return "Holmes Rule Management ActiveApp APP ";
    }

}

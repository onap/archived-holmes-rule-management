/*
 Copyright 2017 ZTE Corporation.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { routing } from './app.routing';
import { AppComponent } from './app.component';
import { AlarmRule } from './correlation-ruleList/alarmRule.component';
import { RuleInfo } from './correlation-ruleInfo/ruleInfo.component';
import { ModalService } from './correlation-modal/modal.service';
import { HttpModule, Jsonp } from '@angular/http';
import { HttpClientModule, HttpClient } from '@angular/common/http';
import { AlarmRuleService } from './correlation-ruleList/alarmRule.service';
import { TranslateModule, TranslateLoader } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { SifModalComponent } from './correlation-modal/modal.component';

export function HttpLoaderFactory(http: HttpClient): TranslateHttpLoader {
    return new TranslateHttpLoader(http, './assets/i18n/', '.json');
}

@NgModule({
    imports: [
        BrowserModule,
        FormsModule,
        HttpModule,
        HttpClientModule,
        routing,
        TranslateModule.forRoot({
            loader: {
                provide: TranslateLoader,
                useFactory: HttpLoaderFactory,
                deps: [HttpClient]
            }
        }),
    ],
    declarations: [
        AppComponent,
        AlarmRule,
        RuleInfo,
        SifModalComponent
    ],
    providers: [ModalService, AlarmRuleService, Jsonp],
    bootstrap: [AppComponent]
})
export class AppModule { }
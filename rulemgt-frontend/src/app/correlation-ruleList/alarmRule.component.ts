/*
 Copyright 2018 ZTE Corporation.

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
import { Component, OnInit, ElementRef } from '@angular/core';
import { ModalService } from '../correlation-modal/modal.service';
import { RuleModel } from './alarmRule';
import { RuleRequest } from './ruleRequest';
import { Router } from '@angular/router';
import { AlarmRuleService } from './alarmRule.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'alarmRule',
  templateUrl: './alarmRule.component.html',
  styleUrls: ['./alarmRule.component.css']
})
export class AlarmRule implements OnInit {
  ruleModel: RuleModel;
  rules: any[];
  queryRule: RuleModel;
  activeText: string;
  ruleName: string;
  enable_on = "enabled";
  enable_off = "disabled"
  totalcount: number = 0;
  model: any;
  ruleRequest: RuleRequest;
  solution = 'ANGULAR';
  selection = 'A';
  activeStatus = ["option_all", "common_enabled", "common_disabled"];

  constructor(public _alarmRuleService: AlarmRuleService, private modalService: ModalService,
    private router: Router, private ele: ElementRef) {
  }

  switch(select: string): void {
    console.log(select);
    if (select == "common_enabled") {
      this.ruleModel.enabled = 1;
    } else if (select == "common_disabled") {
      this.ruleModel.enabled = 0;
    } else {

      this.ruleModel.enabled = null;
    }
    this.setActiveText();
  }

  setActiveText(): void {
    if (this.ruleModel.enabled == 1) {
      this.activeText = "common_enabled";
      this.ruleRequest.enabled = 1;
    }
    else if (this.ruleModel.enabled == 0) {
      this.activeText = "common_disabled";
      this.ruleRequest.enabled = 0;
    } else {
      this.activeText = "option_all";
      this.ruleRequest.enabled = 2;
    }
  }

  getRules(): void {
    this._alarmRuleService.getRules().then(rules => {
      this.rules = rules.correlationRules;
      this.rules.map(x => x['showModal'] = false);
      console.log(this.rules);
      this.totalcount = rules.totalCount;
    });
  }

  public searchRules(): void {
    if (this.ruleModel.enabled == null) {
      this.ruleRequest.enabled = null;
    }
    this.ruleRequest.ruleName = this.ruleModel.ruleName;
    console.log(this.ruleRequest.enabled, this.ruleRequest.ruleName);

    this._alarmRuleService
      .searchrules(this.ruleRequest)
      .then(rules => {
        this.rules = rules;
        this.totalcount = rules.length;
      });
  }

  public updateRule(rule: RuleModel): void {
    this.router.navigate(['ruleInfo/', rule.ruleId]);
  }

  public delete(rule: any): void {
    rule.showModal = true;
  }

  public on_off(rule: RuleModel) {
    rule.enabled == 0 ? rule.enabled = 1 : rule.enabled = 0;
    this._alarmRuleService
      .updateRule(rule)
      .then(res => {
        rule = res;
      });
  }

  public reset(): void {
    this.ruleModel.ruleName = null;
    this.activeText = 'option_all';
    this.ruleModel.enabled = null;
    this.getRules();
  }

  deleteRule(ruleId: string): void {
    this._alarmRuleService.delete(ruleId).then(() => {
      this.cancelModal(ruleId);
      this.getRules();
    }).catch(() => {
      this.cancelModal(ruleId);
    })
  }

  cancelModal(ruleId: string): void {
    this.rules.find(x => x.ruleId === ruleId).showModal = false;
  }

  public ngOnInit(): void {
    this.activeText = 'option_all';
    this.ruleModel = {
      ruleId: null,
      ruleName: null,
      description: '',
      content: null,
      createTime: null,
      creator: null,
      updateTime: null,
      modifier: null,
      enabled: 0,
      loopControlName: ''
    };
    this.ruleRequest = {
      ruleId: null,
      ruleName: null,
      creator: null,
      modifier: null,
      enabled: null,
      loopControlName: ''
    };
    this.getRules();

    this.ele.nativeElement.querySelector('.container-fluid').style.height = window.innerHeight + 'px';
    Observable.fromEvent(window, 'resize')
      .debounceTime(100)
      　　.subscribe(() => {
        this.ele.nativeElement.querySelector('.container-fluid').style.height = window.innerHeight + 'px';
      　　});
  }
}

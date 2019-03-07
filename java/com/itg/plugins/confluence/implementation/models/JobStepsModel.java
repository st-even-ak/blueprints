/*
 * Author: steve.killelay
 * Last Updated: 21/02/19 22:13
 *
 * Copyright {c} 2019, ITG
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,  this list of conditions and the following disclaimer in the documentation  and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */
package com.itg.plugins.confluence.implementation.models;

import com.itg.plugins.confluence.implementation.abstrct.AbstractModels;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Steve.Killelay
 */
@XmlRootElement
public class JobStepsModel extends AbstractModels {

    @XmlElement(name = "id")
    public int id;
    @XmlElement(name = "step_name", required = true)
    public String stepName;
    @XmlElement(name = "step_command", required = true)
    public String stepCommand;
    @XmlElement(name = "database", required = true)
    public String database;
    @XmlElement(name = "on_success_action", required = true)
    public String onSuccessAction;
    @XmlElement(name = "on_failure_action", required = true)
    public String onFailureAction;
    @XmlElement(name = "type", required = true)
    public String type;
    @XmlElement(name = "retry_attempts", required = true, defaultValue = "0")
    public int retryAttempts;
    @XmlElement(name = "session_key", required = true)
    public long sessionKey;

    private final Map<String, String> aliasMap;

    public JobStepsModel() {

        aliasMap = new HashMap<>();
        aliasMap.put("step_name", "Step Name");
        aliasMap.put("step_command", "Step Commande");
        aliasMap.put("database", "Database");
        aliasMap.put("on_success_action", "On Success Action");
        aliasMap.put("on_failure_action", "On Failure Action");
        aliasMap.put("type", "Type");
        aliasMap.put("retry_attempts", "Retry Attempts");
    }

    @Override
    public String getFieldAlias(String fieldName) {

        return aliasMap.get(fieldName);
    }
}

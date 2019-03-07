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
package com.itg.plugins.confluence.implementation.utils;

import com.atlassian.json.jsonorg.JSONException;
import com.atlassian.json.jsonorg.JSONObject;
import com.itg.plugins.confluence.implementation.providers.ClientProvider;
import com.itg.plugins.confluence.implementation.context.DefaultContextProvider;
import org.slf4j.Logger;

import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;

import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 * @author ITG
 */
public class JSONPayloadFactory {

    private static final Logger LOGGER = getLogger(DefaultContextProvider.class);
    private static ClientProvider clientProvider;

    public static void setClientProvider(ClientProvider value) {

        clientProvider = value;
    }

    public static JSONObject createJiraIssuePayload(Map<String, Object> ctx) {

        JSONObject json = new JSONObject();
        try {
            JSONObject field = new JSONObject();
            JSONObject project = new JSONObject();
            JSONObject issue = new JSONObject();

            project.put("key", clientProvider.getClientData(Integer.parseInt(ctx.get("clientkey").toString())).get(0).get("jira_project_key"));
            issue.put("id", "10000"); //Id for an epic
            field.put("project", project);
            field.put("summary", ctx.get("varTitle").toString());
            field.put("description", ctx.get("wikiSummary").toString());
            field.put("customfield_10008", ctx.get("varTitle").toString());
            field.put("issuetype", issue);
            json.put("fields", field);

        } catch (JSONException e) {
            LOGGER.error("DOCUMENT BLUEPRINTS: Attempt to create JIRA Issue Payload, JSON object failed", e);
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(DefaultContextProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
        return json;
    }

    public static JSONObject createJiraIssueLink(Map<String, String> ctx) {

        JSONObject json = new JSONObject();
        try {
            JSONObject obj = new JSONObject();
            JSONObject icon = new JSONObject();

            //build from the deepest node
            icon.put("url16x16", ctx.get("favicon"));
            icon.put("title", ctx.get("alttext"));

            obj.put("icon", icon);
            obj.put("url", ctx.get("url")); //Id for an epic
            obj.put("title", ctx.get("title"));

            json.put("relationship", ctx.get("template"));
            json.put("object", obj);

        } catch (JSONException e) {
            LOGGER.error("DOCUMENT BLUEPRINTS: Attempt to create JIRA Issue link, JSON object failed", e);
        }
        return json;
    }
}

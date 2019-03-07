/*
 * Author: steve.killelay
 * Last Updated: 22/02/19 09:35
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

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.application.jira.JiraApplicationType;
import com.atlassian.json.jsonorg.JSONObject;
import com.atlassian.modzdetector.IOUtils;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.net.ResponseHandler;
import com.itg.plugins.confluence.implementation.context.DefaultContextProvider;
import org.apache.http.entity.StringEntity;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author ITG
 */
public class JIRAIntegrator {

    public static String getJiraLink() {

        return resolveJiraLink().getId().toString();
    }

    public static String createJiraPost(String uri, JSONObject payload) {

        ApplicationLink jiraLink = resolveJiraLink();
        String fullUrl = uri.concat("?applicationId=").concat(jiraLink.getId().toString());

        try {
            Request postRequest = createRequest(fullUrl, jiraLink.createAuthenticatedRequestFactory(), payload);
            HttpServletResponse postResponse = null;

            postRequest.execute(new ProxyResponseHandler(postResponse));

            JSONObject response = new JSONObject();
            return response.getString("key");

        } catch (CredentialsRequiredException | IOException | ResponseException ex) {
            java.util.logging.Logger.getLogger(DefaultContextProvider.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    private static ApplicationLink resolveJiraLink() {

        ApplicationLinkService appLinkService = ComponentLocator.getComponent(ApplicationLinkService.class
        );
        return appLinkService.getPrimaryApplicationLink(JiraApplicationType.class
        );
    }

    private static Request createRequest(String url, final ApplicationLinkRequestFactory requestFactory, JSONObject json) throws CredentialsRequiredException, IOException {

        Request postRequest = requestFactory.createRequest(Request.MethodType.POST, url);
        postRequest.setHeader("Content-Type", "application/json");
        postRequest.setEntity(new StringEntity(json.toString()));
        return postRequest;
    }

    private static class ProxyResponseHandler implements ResponseHandler<Response> {

        private final HttpServletResponse response;

        public ProxyResponseHandler(HttpServletResponse response) {
            this.response = response;
        }

        @Override
        public void handle(Response response) throws ResponseException {
            if (response.isSuccessful()) {
                InputStream responseStream = response.getResponseBodyAsStream();
                Map<String, String> headers = response.getHeaders();
                headers.keySet().stream().filter((key) -> !(key.equalsIgnoreCase("Set-Cookie"))).forEachOrdered((key) -> {
                    // don't pass on cookies set by linked application.
                    this.response.setHeader(key, headers.get(key));
                });
                try {
                    if (responseStream != null) {
                        try (ServletOutputStream outputStream = this.response.getOutputStream()) {
                            IOUtils.copy(responseStream, outputStream);
                            outputStream.flush();
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

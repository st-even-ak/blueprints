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
package com.itg.plugins.confluence.implementation.abstrct;

import com.atlassian.confluence.content.render.xhtml.DefaultConversionContext;
import com.atlassian.confluence.content.render.xhtml.XhtmlException;
import com.atlassian.confluence.languages.LocaleManager;
import com.atlassian.confluence.plugins.createcontent.api.contextproviders.AbstractBlueprintContextProvider;
import com.atlassian.confluence.plugins.createcontent.api.contextproviders.BlueprintContext;
import com.atlassian.confluence.renderer.PageContext;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.xhtml.api.EditorFormatService;
import com.atlassian.json.jsonorg.JSONObject;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.itg.plugins.confluence.implementation.properties.ConfluenceDatabaseProperties;
import com.itg.plugins.confluence.implementation.providers.BlueprintProvider;
import com.itg.plugins.confluence.implementation.providers.ClientProvider;
import com.itg.plugins.confluence.implementation.utils.JIRAIntegrator;
import com.itg.plugins.confluence.implementation.utils.JSONPayloadFactory;
import com.itg.plugins.confluence.interfaces.IBlueprintProvider;
import com.itg.plugins.confluence.interfaces.IClientProvider;
import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 * @author Steve.Killelay
 */
public abstract class AbstractContextProvider extends AbstractBlueprintContextProvider implements InitializingBean, DisposableBean {

    private static final Logger LOGGER = getLogger(AbstractContextProvider.class);

    @Nonnull
    @ComponentImport
    protected final EditorFormatService _editorFormatService;
    @Nonnull
    @ComponentImport
    protected final UserAccessor _userAccessor;
    @Nonnull
    @ComponentImport
    protected final LocaleManager _localeManager;

    protected final IBlueprintProvider _blueprintProvider;
    protected final IClientProvider _clientProvider;

    protected IBlueprintProvider getBlueprintProvider() {

        return _blueprintProvider;
    }

    public AbstractContextProvider(final EditorFormatService editorFormatService,
                                   final UserAccessor userAccessor,
                                   final LocaleManager localeManager,
                                   final ConfluenceDatabaseProperties properties) {

        this._editorFormatService = editorFormatService;
        this._userAccessor = userAccessor;
        this._localeManager = localeManager;
        this._blueprintProvider = new BlueprintProvider(this._userAccessor, this._localeManager);
		this._clientProvider = new ClientProvider(properties);
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException {
        // TODO Auto-generated method stub
    }

    @Override
    protected BlueprintContext updateBlueprintContext(BlueprintContext blueprintContext) {

        // set page title
        blueprintContext.setTitle(makePageTitle(blueprintContext.getMap()));

        try {
            // iterate the context map and substitute the wiki and default elements
            for (Entry<String, Object> kvp : blueprintContext.getMap().entrySet()) {
                // We're only looking for "wiki" items here, any wiki mark-up will be resolved to html
                if (kvp.getKey().substring(0, 4).equals("wiki")) {
                    kvp.setValue(resolveWikiEntry(kvp));
                }

                // if email address is empty use the page creators
                if (kvp.getKey().equals("varEmailAddress")) {
                    kvp.setValue(resolveEmailAddresses(kvp, blueprintContext.getMap().get("varTitle").toString()));
                }

                if (kvp.getKey().substring(0, 5).equals("macro")) {

                    // create status macro
                    if (kvp.getKey().substring(6).equals("jira")) {// && !kvp.getValue().equals("")) {
                        kvp.setValue(resolveJiraMacroEntry(kvp, blueprintContext));
                    }

                    // create status macro
                    if (kvp.getKey().substring(6).equals("status")) {

                        kvp.setValue(resolveStatusMacroEntry(kvp));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("DOCUMENT BLUEPRINTS: Unhandled Exception, while constructing the page body", e);
        } finally{
            // now add default values
            blueprintContext.getMap().put("varCreatedDate", convertToDate(_blueprintProvider.getFriendlyDate()));
            blueprintContext.getMap().put("varAuthor", "<ac:link><ri:userkey=\"" + _blueprintProvider.getCurrentUser().getKey().getStringValue() + "\" /></ac:link>");
            blueprintContext.getMap().put("varClient", _clientProvider.getClientName(Integer.parseInt(blueprintContext.getMap().get("clientkey").toString())));
        }
        return blueprintContext;
    }

    @Override
    public void destroy() throws Exception {
        // TODO Auto-generated method stub
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // TODO Auto-generated method stub
    }

    /**
     * @description wraps the supplied value in Atlassians own date/time element
     * @param value
     * @return
     */
    protected String convertToDate(String value) {

        return "<time datetime=\"".concat(value).concat("\" />");
    }

    protected String makePageTitle(Map<String, Object> ctx) {

        return _blueprintProvider.getPageTitle(ctx.get("contentTemplateKey").toString(), ctx.get("varTitle").toString());
    }

    private String resolveWikiEntry(@Nonnull Entry<String, Object> kvp) {

        DefaultConversionContext wikiConverter = new DefaultConversionContext(new PageContext());

        try {
            return _editorFormatService.convertWikiToEdit(kvp.getValue().toString(), wikiConverter)
                           .replaceAll("\n", "")
                           .replaceAll("\t", "");
        } catch(XhtmlException e) {
            LOGGER.error("DOCUMENT BLUEPRINTS: Xhtml Parsing failed, while constructing the page body", e);
            /*
             Just return the original value
            */
            return kvp.getValue().toString();
        }
    }

    private String resolveJiraMacroEntry(@Nonnull Entry<String, Object> kvp, BlueprintContext blueprintContext){

        String jiraIssueKey = kvp.getValue().toString();
        /*
          If JIRA field is empty create new issue
         */
        try {
            if ("".equals(jiraIssueKey)) {

                String uri = "/rest/jira-integration/1.0/issues?applicationId=".concat(JIRAIntegrator.getJiraLink());
                JSONObject payload = JSONPayloadFactory.createJiraIssuePayload(blueprintContext.getMap());
                jiraIssueKey = JIRAIntegrator.createJiraPost(uri, payload);
            }
            if (jiraIssueKey != null) {
                /*
                  now create JIRA macro
                 */
                HashMap<String, String> params = new HashMap<>();
                params.put("server", "JIRA");
                params.put("serverId", JIRAIntegrator.getJiraLink());
                params.put("key", jiraIssueKey);

                return _blueprintProvider.createMacro(kvp.getKey().substring(6), params);
            }

        } catch (Exception e){
            LOGGER.warn("DOCUMENT BLUEPRINTS: A Jira instance could not be found", e);

        } finally {
            return kvp.getValue().toString();
        }
    }

    private String resolveStatusMacroEntry(@Nonnull Entry<String, Object> kvp){

        HashMap<String, String> params = new HashMap<>();
        params.put("title", kvp.getValue().toString().toUpperCase());
        params.put("colour", _blueprintProvider.getDocumentStatusColour(kvp.getValue().toString()));

        return _blueprintProvider.createMacro(kvp.getKey().substring(6), params);
    }

    @Nonnull
    private String resolveEmailAddresses(@Nonnull Entry<String, Object> kvp, String DocumentTitle){

        StringBuilder ahref = new StringBuilder();

        // if email address is empty use the page creators
        if (kvp.getValue().toString().equals("")) {

            ahref.append( "<a href=\"mailto:");
            ahref.append(_blueprintProvider.getCurrentUser().getEmail());
            ahref.append("?subject=");
            ahref.append(DocumentTitle);
            ahref.append("\">");
            ahref.append(_blueprintProvider.getCurrentUser().getFullName());
            ahref.append("</a>");
        } else {

            String[] ahrefs = kvp.getValue().toString().split(";");
            for (String ref : ahrefs) {
                ahref.append("<a href=\"mailto:");
                ahref.append(ref);
                ahref.append("?subject=");
                ahref.append(DocumentTitle);
                ahref.append("\">");
                ahref.append(ref);
                ahref.append("</a> ");
            }
        }
        return ahref.toString();
    }
}

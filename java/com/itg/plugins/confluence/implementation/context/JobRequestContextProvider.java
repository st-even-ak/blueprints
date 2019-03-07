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
package com.itg.plugins.confluence.implementation.context;

import com.atlassian.confluence.content.render.xhtml.DefaultConversionContext;
import com.atlassian.confluence.content.render.xhtml.XhtmlException;
import com.atlassian.confluence.languages.LocaleManager;
import com.atlassian.confluence.plugins.createcontent.api.contextproviders.BlueprintContext;
import com.atlassian.confluence.renderer.PageContext;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.xhtml.api.EditorFormatService;
import com.itg.plugins.confluence.implementation.abstrct.AbstractContextProvider;
import com.itg.plugins.confluence.implementation.properties.ConfluenceDatabaseProperties;
import com.itg.plugins.confluence.interfaces.IJobStepsProvider;
import com.itg.plugins.confluence.interfaces.ISegmentInfoProvider;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 * @author Steve.Killelay
 */
public class JobRequestContextProvider extends AbstractContextProvider {

    private static final Logger LOGGER = getLogger(JobRequestContextProvider.class);
    private static final List<String> variablePrefixes = Arrays.asList("var", "wik", "mac");
    private final IJobStepsProvider _jobStepsProvider;
    private final ISegmentInfoProvider _segmentInfoProvider;

    @Autowired
    public JobRequestContextProvider(final EditorFormatService editorFormatService,
                                     final UserAccessor userAccessor,
                                     final LocaleManager localeManager,
                                     final ConfluenceDatabaseProperties properties,
                                     final IJobStepsProvider jobStepsProvider,
                                     final ISegmentInfoProvider segmentInfoProvider) {

        super(editorFormatService, userAccessor, localeManager, properties);
        this._jobStepsProvider = jobStepsProvider;
        this._segmentInfoProvider = segmentInfoProvider;
    }

    private enum variablePrefixes {
        var,
        wiki,
        macro
    }

    @Override
    protected String makePageTitle(Map<String, Object> ctx) {

        return super.getBlueprintProvider().getPageTitle(ctx.get("contentTemplateKey").toString(), ctx.get("varTitle").toString());
    }

    private String createTable(List<Map<String, Object>> data) {

        if (data.isEmpty()) {
            return null;
        }

        StringBuilder tblHtml = new StringBuilder();
        tblHtml.append("<table><thead><tr>");

        data.get(0).keySet().stream().peek((header) -> {
            tblHtml.append("<th>");
            tblHtml.append(header);
        }).forEachOrdered((_item) -> tblHtml.append("</th>"));

        tblHtml.append("</tr></thead><tbody>");

        data.stream().peek((map) -> {
            tblHtml.append("<tr>");
            map.entrySet().stream().peek((record) -> {
                tblHtml.append("<td>");
                tblHtml.append(record.getValue().toString());
            }).forEachOrdered((_item) -> tblHtml.append("</td>"));
        }).forEachOrdered((_item) -> tblHtml.append("</tr>"));
        tblHtml.append("</tbody></table>");

        return tblHtml.toString();
    }

    @Override
    protected BlueprintContext updateBlueprintContext(BlueprintContext blueprintContext) {

        DefaultConversionContext wikiConverter = new DefaultConversionContext(new PageContext());

        /*
          run default transformations
         */
        blueprintContext = super.updateBlueprintContext(blueprintContext);

        try {

            /*
              Create tables
             */
            blueprintContext.getMap().put("tblJobSteps", createTable(_jobStepsProvider.getStepsSession(blueprintContext.getMap().get("sessionKey").toString())));
            blueprintContext.getMap().put("tblSegmentInfo", createTable(_segmentInfoProvider.getSegmentSession(blueprintContext.getMap().get("sessionKey").toString())));

            /*
              Delete Session info
             */
            _jobStepsProvider.deleteStepsSession(blueprintContext.getMap().get("sessionKey").toString());
            _segmentInfoProvider.deleteSegmentSession(blueprintContext.getMap().get("sessionKey").toString());

            /*
              iterate the context map and substitute the wiki and default elements
             */
            for (Entry<String, Object> kvp : blueprintContext.getMap().entrySet()) {
                /*
                  if not in prefix range move next
                 */
                if (!variablePrefixes.contains(kvp.getKey().substring(0, 3))) {
                    continue;
                }
                /*
                  We're only looking for "wiki" items here, any wiki mark-up will be resolved to html
                 */
                if (kvp.getKey().substring(0, 4).equals("wiki")) {

                    kvp.setValue(_editorFormatService.convertWikiToEdit(kvp.getValue().toString(), wikiConverter).replace("\n", "").replaceAll("\t", ""));
                    continue;
                }

                /*
                  if data value
                 */
                if (kvp.getKey().endsWith("Date")) {
                    kvp.setValue(convertToDate(kvp.getValue().toString()));
                    continue;
                }

            }
            /*
              now add default values
             */
            blueprintContext.getMap().put("varCreatedDate", _blueprintProvider.getFriendlyDate());
            //blueprintContext.getMap().put("varClient", serviceProvider.getClientName(blueprintContext.getMap().get("clientkey").toString()));
        } catch (XhtmlException e) {
            LOGGER.warn("DOCUMENT BLUEPRINTS: Xhtml Parsing failed, while constructing the page body", e);
        } catch (Exception e) {
            LOGGER.warn("DOCUMENT BLUEPRINTS: Unhandle Exception, while constructing the page body", e);
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
}

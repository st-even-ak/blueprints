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
package com.itg.plugins.confluence.implementation.listeners;

import com.atlassian.confluence.core.BodyContent;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.labels.Label;
import com.atlassian.confluence.labels.LabelManager;
import com.atlassian.confluence.languages.LocaleManager;
import com.atlassian.confluence.links.TrackbackLink;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.pages.AttachmentManager;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.plugins.createcontent.api.events.BlueprintPageCreateEvent;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.json.jsonorg.JSONObject;
import com.atlassian.plugin.ModuleCompleteKey;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.itg.plugins.confluence.implementation.providers.BlueprintProvider;
import com.itg.plugins.confluence.implementation.utils.JIRAIntegrator;
import com.itg.plugins.confluence.implementation.utils.JSONPayloadFactory;
import com.itg.plugins.confluence.interfaces.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.confluence.labels.Namespace.GLOBAL;
import static java.util.Arrays.asList;
import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 * @author Steve.Killelay
 */
@Component
class TemplateListener implements InitializingBean, DisposableBean {

	private static final Logger LOGGER = getLogger(TemplateListener.class);

	public static final ModuleCompleteKey BLUEPRINT_KEY = new ModuleCompleteKey("com.itg.plugins.confluence.document-blueprints:document-blueprint");

	@Nonnull
	@ComponentImport
	private final EventPublisher _eventPublisher;
	@Nonnull
	@ComponentImport
	private final UserAccessor _userAccessor;
	@Nonnull
	@ComponentImport
	private final LocaleManager _localeManager;
	@Nonnull
	@ComponentImport
	private final AttachmentManager _attachmentManager;
	@Nonnull
	@ComponentImport
	private final PageManager _pageManager;
	@Nonnull
	@ComponentImport
	private final LabelManager _labelManager;

	private final IBlueprintProvider _blueprintProvider;
	private final IClientProvider _clientProvider;
	private final IJiraInstanceProperties _jiraProperties;
	private final IGlobalProperties _globalProperties;

	@Autowired
	public TemplateListener(final EventPublisher eventPublisher,
	                        final PageManager pageManager,
	                        final UserAccessor userAccessor,
	                        final LocaleManager localeManager,
	                        final AttachmentManager attachmentManager,
	                        final LabelManager labelManager,
	                        final IClientProvider clientProvider,
							final IJiraInstanceProperties jiraProperties,
							final IGlobalProperties globalProperties
	) {
		eventPublisher.register(this);
		this._eventPublisher = eventPublisher;
		this._pageManager = pageManager;
		this._userAccessor = userAccessor;
		this._localeManager = localeManager;
		this._attachmentManager = attachmentManager;
		this._labelManager = labelManager;
		this._globalProperties = globalProperties;
		this._jiraProperties = jiraProperties;
		this._clientProvider = clientProvider;

		this._blueprintProvider = new BlueprintProvider(this._userAccessor, this._localeManager);

		LOGGER.warn("WARN: TemplateListener Class Registered.");
	}

	/**
	 *
	 * @param event
	 */
	@EventListener
	public void onBlueprintCreateEvent(BlueprintPageCreateEvent event) {

		if (BLUEPRINT_KEY.equals(event.getBlueprintKey())) {

			// this var will be used in the rest of this method to unescape injected content
			ContentEntityObject pageContentEntity = event.getPage().getEntity();
			BodyContent pageBodyContent = pageContentEntity.getBodyContent();
			String pageBody = pageBodyContent.getBody();
			int clientId = Integer.parseInt(event.getContext().get("clientkey").toString());

			// unescape escaped wiki markup
			pageBody = pageBody.replace("&lt;", "<")
							   .replace("&gt;", ">")
							   .replace("&quot;", "\"")
							   .replace("&amp;", "&")
							   .replace("&#37", "%");

			try {
				Attachment att;
				String fileName = _clientProvider.getClientLogoFileName(clientId);
				Page resource = this._pageManager.getPage(_globalProperties.getDefaultResourceSpaceKey(), _globalProperties.getDefaultResourcePageName());
				try {
					att = this._attachmentManager.getAttachment(resource, fileName).copy();
				} catch (Exception e) {
					att = this._attachmentManager.getAttachment(resource, "notfound-logo.png").copy();
				}
				// update values to copy
				att.setCreator(_blueprintProvider.getCurrentUser());
				att.setCreationDate(new Date());
				att.setLastModificationDate(att.getCreationDate());
				// Save a copy
				this._pageManager.setAttachmentManager(_attachmentManager);
				this._attachmentManager.copyAttachment(att, event.getPage());
				// add reference to the logo file
				pageBody = pageBody.replace("$LogoFile$", att.getTitle());
			} catch (IOException e) {
				LOGGER.warn("DOCUMENT BLUEPRINTS: Attempt to attached client logo failed (perhaps i has not be saved to the correct location?), substituting with default", e);
			} catch (Exception e) {
				LOGGER.warn("DOCUMENT BLUEPRINTS: An handled exception occured, we're unabel to attached the relevant client logo", e);
			} finally {
				// update the pageBody with modified values
				pageBodyContent.setBody(pageBody);
				pageContentEntity.setBodyContent(pageBodyContent);
			}

			try {
				// remove blueprint created labels
				this._labelManager.removeAllLabels(event.getPage());

				List<String> labels;
				labels = asList(event.getContext().get("varLabels").toString().trim().split(";"));

				labels.stream().filter((label) -> (label.hashCode() > 0)).forEachOrdered((label) -> addLabel(event.getPage(), label));

				// add document type label
				addLabel(event.getPage(), _blueprintProvider.getDocumentType(event.getContext().get("contentTemplateKey").toString()));

				// add client label
				addLabel(event.getPage(), _clientProvider.getClientName(Integer.parseInt(event.getContext().get("clientkey").toString())));
			} catch (NumberFormatException e) {
				LOGGER.warn("DOCUMENT BLUEPRINTS: Attempt to attached labels to ITG blueprint template page failed", e);
			}

			addJiraTrackBackLink(event);

		}
	}

	@Override
	public void destroy(){
		this._eventPublisher.unregister(this);
		LOGGER.warn("WARN: Listener Destroyed.");
	}

	@Override
	public void afterPropertiesSet() {

		this._eventPublisher.register(this);
		LOGGER.warn("WARN: Listener afterPropertiesSet.");
	}

	private void addLabel(ContentEntityObject page, String label) {

		this._labelManager.addLabel(page, new Label(label.trim()
															.replace(" ", "-")
															.toLowerCase(), GLOBAL.toString(), _blueprintProvider.getCurrentUser()));
	}

	private void addJiraTrackBackLink(BlueprintPageCreateEvent event){

		try {
			// Add link back to Jira Ticket
			List<TrackbackLink> links = event.getPage().getTrackbackLinks();
			String ticketId = links.get(0).getLowerUrl().substring(links.get(0).getLowerUrl().lastIndexOf('/'));
			String uri = _jiraProperties.getJiraInstanceURL().concat("/rest/api/2/issue/").concat(ticketId).concat("/remotelink");//.concat(JIRAIntegrator.getJiraLink());
			Map<String, String> ctx = new HashMap<>();
			ctx.put("template", _blueprintProvider.getDocumentType(event.getContext().get("contentTemplateKey").toString()));
			ctx.put("title", event.getPage().getTitle());
			ctx.put("url", event.getPage().getUrlPath());
			ctx.put("favicon", "");
			ctx.put("alttext", "");
			JSONObject payload = JSONPayloadFactory.createJiraIssueLink(ctx);
			JIRAIntegrator.createJiraPost(uri, payload);
		} catch (Exception e){
			LOGGER.error("DOCUMENT BLUEPRINTS: Attempt to create JIRA Trackback link failed", e);
		}
	}
}

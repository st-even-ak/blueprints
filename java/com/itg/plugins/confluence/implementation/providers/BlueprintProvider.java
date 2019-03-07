/*
 * Author: steve.killelay
 * Last Updated: 22/02/19 09:42
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
package com.itg.plugins.confluence.implementation.providers;

import com.atlassian.confluence.languages.LocaleManager;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.itg.plugins.confluence.interfaces.IBlueprintProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.atlassian.confluence.user.AuthenticatedUserThreadLocal.get;
import static java.lang.String.join;
import static java.text.DateFormat.*;
import static java.util.Arrays.asList;

/**
 *
 * @author Steve.Killelay
 */
@Component
public class BlueprintProvider  implements IBlueprintProvider {

	@Nonnull
	@ComponentImport
	private final UserAccessor _userAccessor;
	@Nonnull
	@ComponentImport
	private final LocaleManager _localeManager;

	@Autowired
	public BlueprintProvider(
			final UserAccessor userAccessor,
			final LocaleManager localeManager) {

		this._userAccessor = userAccessor;
		this._localeManager = localeManager;

	}

	@Override
	public String getFriendlyDate() {

		return getDateInstance(LONG).format(new Date());
	}

	@Override
	public String getFriendlyDateTime() {

		return getDateTimeInstance(LONG, LONG).format(new Date());
	}

	@Override
	public ConfluenceUser getUser(String userName) {

		return _userAccessor.getUserByName(userName);
	}

	@Override
	public ConfluenceUser getCurrentUser() {

		return get();
	}

	@Override
	public Locale getCurrentLocale() {

		return getCurrentUser() != null ? _localeManager.getLocale(getCurrentUser()) : null;
	}

	/**
	 * *
	 *
	 * @param documentType
	 * @param documentTitle
	 * @return
	 */
	@Override
	public String getPageTitle(String documentType, String documentTitle) {

		return capitalise(getDocumentType(documentType)).concat(": ").concat(documentTitle);
	}

	/**
	 * *
	 * @param documentType
	 * @return
	 */
	@Override
	public String getDocumentType(String documentType) {

		return documentType.split("-")[1];
	}

	/**
	 * *
	 * @use Describe me
	 * @param documentStatus
	 * @return
	 */
	@Override
	public String getDocumentStatusColour(String documentStatus) {

		switch (documentStatus.toLowerCase()) {
			case "confidential":
				return "Red";
			case "internal":
				return "Yellow";
			case "public":
				return "Green";
			default:
				return null;
		}
	}

	/**
	 * *
	 * @use this method will apply Proper Case to words separated by space (" ")
	 * @param text
	 * @return
	 */
	@Override
	public String capitalise(String text) {

		List<String> words;
		words = asList(text.split(" "));

		for (int i = 0; i < words.size(); i++) {
			words.set(i, words.get(i).substring(0, 1).toUpperCase().concat(words.get(i).substring(1)));
		}

		return join(" ", words);
	}

	/**
	 * *
	 * @param macroName
	 * @param params
	 * @return
	 */
	@Override
	public String createMacro(String macroName, HashMap<String, String> params) {

		// TODO: there's probably a better way to do this using MacroManager...
		String macro = "<ac:structured-macro ac:name=\"" + macroName + "\" ac:schema-version=\"1\">";

		macro = params.entrySet().stream().map((kvp) -> "<ac:parameter ac:name=\"" + kvp.getKey() + "\">" + kvp.getValue() + "</ac:parameter>").reduce(macro, String::concat);
		macro += "</ac:structured-macro>";

		return macro;
	}
}

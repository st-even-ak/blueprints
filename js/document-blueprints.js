/* global AJS, Confluence, json, com, Backbone, state */

require(['aui/form-validation'], ['aui/form-notification'], ['aui/experimental-restfultable'], AJS.toInit(function ($) {

	varTemplatesJson = [];
    var dbdsUrl = AJS.contextPath() + "/rest/dbds/latest/";

    populatePropertiesForm = function () {

        var url = dbdsUrl + "admin/department/0";
        $.getJSON(url)
            .done(function (json) {
				$("#session-key").val(json.session_key);
                $.each(json, function (i) {
                    $("#department").append($("<option>", {value: json[i].id, text: json[i].department}));
                });
                $("#spinner-wrapper").removeClass().addClass("hidden");
                $("#department").on('change', function (e, state) {
					var url = dbdsUrl + "admin/template/d/"+e.srcElement.value;
					$("#template-key").empty();
					$("#template-key").append($("<option>", {value: -1, text: AJS.I18n.getText('com.itg.plugin.confluence.templateSelectionForm.form.template-key.label')}));
					$("#spinner-wrapper").removeClass().addClass("itg-spinner-wrapper");
					$.getJSON(url)
						.done(function (json) {
							varTemplatesJson = json;
							$.each(json, function (i) {
								$("#template-key").append($("<option>", {value: json[i].template_key, text: json[i].template_name}));
							});
							$("#template-key").on('change', function () {

								var defaultStatus = null;
								if ($("#template-key").val() === 'Release Note' || $("#template-key").val() === 'Job Request') {
									input = com.itg.plugin.confluence.labelInputPair({strName: 'macro-jira',
										strId: 'varJIRA',
										strLabel: AJS.I18n.getText('com.itg.plugin.confluence.templateSelectionForm.form.varJIRA.label'),
										strRequiredText: AJS.I18n.getText('com.itg.plugin.confluence.templateSelectionForm.form.varJIRA.required')});
									defaultStatus = "internal";
								} else {
									input = com.itg.plugin.confluence.labelInputPair({strName: 'macro-jira',
										strId: 'varJIRA',
										strLabel: AJS.I18n.getText('com.itg.plugin.confluence.templateSelectionForm.form.varJIRA.label'),
										strHelpText: AJS.I18n.getText('com.itg.plugin.confluence.templateSelectionForm.form.varJIRA.helptext')});
									defaultStatus = "confidential";
								}
								$("#frm div:last").replaceWith(input);
								$("#varDocumentStatus").val(defaultStatus).prop('selected', true);
							});
						})
					.fail(function (xhr, textStatus, error) {
						var err = textStatus + ", " + error;
						$("#spinner").removeClass().addClass("itg-spinner-fail");
						console.error("Request Failed: " + err);
					});
					url = dbdsUrl + "client/" + AJS.params.spaceKey;
					$.getJSON(url)
						.done(function (json) {
							$.each(json, function (i) {
								$("#clientkey").append($("<option>", {value: json[i].id, text: json[i].client_name}));
							});
							$("#spinner-wrapper").removeClass().addClass("hidden");
							$("#filter-clients").on('change', function () {
								if ($("#filter-clients").is(':checked')) {
									url = dbdsUrl + "client/0";
								} else {
									url = dbdsUrl + "client/" + AJS.params.spaceKey;
								}
								$.getJSON(url)
									.done(function (json) {
										$("#clientkey").children().slice(1).remove().end();
										$.each(json, function (i) {
											$("#clientkey").append($("<option>", {value: json[i].id, text: json[i].client_name}));
										});
									})
									.fail(function (xhr, textStatus, error) {
										var err = textStatus + ", " + error;
										console.error("Request Failed: " + err);
									});
							});
						})
						.fail(function (xhr, textStatus, error) {
							var err = textStatus + ", " + error;
							$("#spinner").removeClass().addClass("itg-spinner-fail");
							console.error("Request Failed: " + err);
						});
				});
            })
            .fail(function (xhr, textStatus, error) {
                var err = textStatus + ", " + error;
                $("#spinner").removeClass().addClass("itg-spinner-fail");
                console.error("Request Failed: " + err);
            });
    };

//    cascadePropertiesForm = function (e, state) {
//
//        var url = dbdsUrl + "admin/templates/"+e;
//        $.getJSON(url)
//                .done(function (json) {
//                    $.each(json.templates, function (i) {
//                        $("#template-key").append($("<option>", {value: json.templates[i].templatekey, text: json.templates[i].templatename}));
//                    });
//                    $("#session-key").val(json.session_key);
//                    $("#template-key").on('change', function () {
//
//                        var input = null;
//                        var defaultStatus = null;
//                        if ($("#template-key").val() === 'Release Note' || $("#template-key").val() === 'Job Request') {
//                            input = com.itg.plugin.confluence.labelInputPair({strName: 'macro-jira',
//                                strId: 'varJIRA',
//                                strLabel: AJS.I18n.getText('com.itg.plugin.confluence.templateSelectionForm.form.varJIRA.label'),
//                                strRequiredText: AJS.I18n.getText('com.itg.plugin.confluence.templateSelectionForm.form.varJIRA.required')});
//                            defaultStatus = "internal";
//
//                        } else {
//                            input = com.itg.plugin.confluence.labelInputPair({strName: 'macro-jira',
//                                strId: 'varJIRA',
//                                strLabel: AJS.I18n.getText('com.itg.plugin.confluence.templateSelectionForm.form.varJIRA.label'),
//                                strHelpText: AJS.I18n.getText('com.itg.plugin.confluence.templateSelectionForm.form.varJIRA.helptext')});
//                            defaultStatus = "confidential";
//                        }
//                        $("#frm div:last").replaceWith(input);
//                        $("#varDocumentStatus").val(defaultStatus).prop('selected', true);
//                    });
//                })
//                .fail(function (xhr, textStatus, error) {
//                    var err = textStatus + ", " + error;
//                    $("#spinner").removeClass().addClass("itg-spinner-fail");
//                    console.error("Request Failed: " + err);
//                });
//        url = dbdsUrl + "client/" + AJS.params.spaceKey;
//        $.getJSON(url)
//                .done(function (json) {
//                    $.each(json, function (i) {
//                        $("#clientkey").append($("<option>", {value: json[i].id, text: json[i].client_name}));
//                    });
//                    $("#spinner-wrapper").removeClass().addClass("hidden");
//
//                    $("#filter-clients").on('change', function () {
//
//                        if ($("#filter-clients").is(':checked')) {
//                            url = dbdsUrl + "client/0";
//                        } else {
//                            url = dbdsUrl + "client/" + AJS.params.spaceKey;
//                        }
//                        $.getJSON(url)
//                                .done(function (json) {
//                                    $("#clientkey").children().slice(1).remove().end();
//                                    $.each(json, function (i) {
//                                        $("#clientkey").append($("<option>", {value: json[i].id, text: json[i].client_name}));
//                                    });
//                                })
//                                .fail(function (xhr, textStatus, error) {
////                                    var err = textStatus + ", " + error;
//                                    console.error("Request Failed: " + err);
//                                });
//                    });
//                })
//                .fail(function (xhr, textStatus, error) {
//                    var err = textStatus + ", " + error;
//                    $("#spinner").removeClass().addClass("itg-spinner-fail");
//                    console.error("Request Failed: " + err);
//                });
//    };

    /**
     * @description method responsible for two actions 1, set the name for the next for to display, 2 update the contentTemplateKey to the correct blueprint template key to render
     * @param {type} e
     * @param {type} state
     * @returns {undefined}
     */
    choosePath = function (e, state) {

        if (state.pageData.contentTemplateKey === -1 || state.pageData.clientkey === -1 || !validateForm(e, state)) {
            e.preventDefault();
            return;
        }
        state.nextPageId = state.pageData.contentTemplateKey.substr(0, 1).toLowerCase().concat(state.pageData.contentTemplateKey.substr(1).replace(/\ /g, '')).concat("Properties");
        state.pageData.contentTemplateKey = 'itg-' + state.pageData.contentTemplateKey.toLowerCase() + '-template';
    };

    validateForm = function (e, state) {

        $fields = state.$container.find(":text, textarea");
        for (i = 0; i < $fields.length; i++) {

            // if empty set available defaults
            defaultValueContainer = state.$container.find("#" + $fields[i].id + "-default")[0];
            if (defaultValueContainer !== undefined) {
                state.pageData[$fields[i].name] = $fields[i].value === "" ? defaultValueContainer.value : $fields[i].value;
            }
            // now check validity
            if (!$fields[i].validity.valid) {
                e.preventDefault();
                break;
            }
        }
        return true;
    };

    validateFormForNext = function (e, state) {


        return true;
    }

    buildJobStepForm = function (e, state) {

        var JobTypeView = AJS.RestfulTable.CustomEditView.extend({
            render: function (self) {

                var $select = $("<select id='type' name='type' class='select'>");
                $select.append($("<option>", {value: "Operating System (CmdExec)", text: "Operating System (CmdExec)"}));
                $select.append($("<option>", {value: "PowerShell", text: "PowerShell"}));
                $select.append($("<option>", {value: "SQL Server Analysis Service Command", text: "SQL Server Analysis Service Command"}));
                $select.append($("<option>", {value: "SQL Server Analysis Service Query", text: "SQL Server Analysis Service Query"}));
                $select.append($("<option>", {value: "SQL Server Integration Service Package", text: "SQL Server Integration Service Package"}));
                $select.append($("<option>", {value: "Transact-SQL script (T-TSQL)", text: "Transact-SQL script (T-TSQL)"}));
                $select.val(self.value); // select currently selected
                return $select;
            }
        });

        var data = AJS.RestfulTable;
        var url = dbdsUrl + "js";
        $dataTable = $("#job-steps");
        data = new data({
            id: "job-steps",
            el: $dataTable,
            columns: [
                {fieldName: "StepName", id: "step_name", header: "Step Name"},
                {fieldName: "StepCommand", id: "step_command", header: "Step Command"},
                {fieldName: "Database", id: "database", header: "Database"},
                {fieldName: "OnSuccessAction", id: "on_success_action", header: "On Success Action"},
                {fieldName: "OnFailureAction", id: "on_failure_action", header: "On Failure Action"},
                {fieldName: "Type", id: "type", header: "Type", editView: JobTypeView, createView: JobTypeView},
                {fieldName: "RetryAttempts", id: "retry_attempts", header: "Retry Attempts"}
            ],
            resources: {
                all: url + "/" + state.wizardData.pages.templateSelectionForm.sessionKey + "/0",
                self: url + "/" + state.wizardData.pages.templateSelectionForm.sessionKey
            },
            autofocus: true, // auto focus first field of create row
            noEntriesMsg: "Add Job Steps as Required",
            fieldFocusSelector: function (name) {
                return ":input[type!=hidden][name=" + name + "], #" + name + ", .ajs-restfultable-input-" + name;
            },
            createPosition: "bottom"
        });
    };

    buildSegmentInfoForm = function (e, state) {

        var data = AJS.RestfulTable;
        var url = dbdsUrl + "si";
        $dataTable = $("#segment-info");
        data = new data({
            id: "segment-info",
            el: $dataTable, // <table>
            columns: [
                {fieldName: "ServerName", id: "server_name", header: "Server Name"},
                {fieldName: "Database", id: "database", header: "Database"},
                {fieldName: "Schema", id: "schema", header: "Schema"},
                {fieldName: "SegmentTable", id: "segment_table", header: "Segment Table"},
                {fieldName: "Brand", id: "brand", header: "Brand (if applicable)"},
                {fieldName: "MailingId", id: "mailing_id", header: "MailingId"}
            ],
            resources: {
                all: url + "/" + state.wizardData.pages.templateSelectionForm.sessionKey + "/0",
                self: url + "/" + state.wizardData.pages.templateSelectionForm.sessionKey
            },
            autofocus: true, // auto focus first field of create row
            noEntriesMsg: "Add Segement Details as Required",
            fieldFocusSelector: function (name) {
                return ":input[type!=hidden][name=" + name + "], #" + name + ", .ajs-restfultable-input-" + name;
            },
            createPosition: "bottom"
        });
    };

    buildFbSetupPage = function (e, state) {

        if (state.wizardData.pages.socialMediaBriefProperties.varPlatform === 'fb') {
            input = com.itg.plugin.confluence.labelSelectPairBoolean({strName: 'swiBusinessPage',
                strId: 'swiBusinessPage',
                strLabel: AJS.I18n.getText('com.itg.plugin.confluence.socialMediaPageSetup.form.swiBusinessPage.label')});
            state.$container.append(input);
            $('#swiBusinessPage').on('change', function(){
                state.$container.find('.field-group:not(:first-child)').remove()
                if(this.value === 'y'){
	                input = com.itg.plugin.confluence.labelInputPair({strName: 'varFbPageName',
	                    strId: 'varFbPageName',
	                    strLabel: AJS.I18n.getText('com.itg.plugin.confluence.socialMediaPageSetup.form.varFbPageName.label')});
                    state.$container.append(input);

	                input = com.itg.plugin.confluence.labelInputPair({strName: 'varFbDescription',
	                    strId: 'varFbDescription',
	                    strLabel: AJS.I18n.getText('com.itg.plugin.confluence.socialMediaPageSetup.form.varFbDescription.label')});
                    state.$container.append(input);

	                input = com.itg.plugin.confluence.labelInputPair({strName: 'varFbAddress',
	                    strId: 'varFbAddress',
	                    strLabel: AJS.I18n.getText('com.itg.plugin.confluence.socialMediaPageSetup.form.varFbAddress.label')});
                    state.$container.append(input);

	                input = com.itg.plugin.confluence.labelInputPair({strName: 'varFbPhoneNumber',
	                    strId: 'varFbPhoneNumber',
	                    strLabel: AJS.I18n.getText('com.itg.plugin.confluence.socialMediaPageSetup.form.varFbPhoneNumber.label')});
                    state.$container.append(input);

	                input = com.itg.plugin.confluence.labelInputPair({strName: 'varFbWebsiteUrl',
	                    strId: 'varFbWebsiteUrl',
	                    strLabel: AJS.I18n.getText('com.itg.plugin.confluence.socialMediaPageSetup.form.varFbWebsiteUrl.label')});
                    state.$container.append(input);

	                var msg = '<div class="field-group"><div> Attach a Cover & Profile Photo to the Page when it\'s created</div></div>'
                    state.$container.append(msg);
                }
                else {
	                var msg = '<div class="field-group"><div>Please supply admin rights to the existing page</div></div>'
                    state.$container.append(msg);
	                input = com.itg.plugin.confluence.labelInputPair({strName: 'varFbAdminRights',
	                    strId: 'varFbAdminRights',
	                    strLabel: AJS.I18n.getText('com.itg.plugin.confluence.socialMediaPageSetup.form.varFbAdminRights.label')});
                    state.$container.append(input);
                }
            })
        }
    }
    // Register wizard hooks
    Confluence.Blueprint.setWizard('com.itg.plugins.confluence.document-blueprints:create-document-blueprint', function (wizard) {

        wizard.on('pre-render.templateSelectionForm', populatePropertiesForm);
        wizard.on('submit.templateSelectionForm', choosePath);

        //itg-brief-template
        wizard.on('submit.briefProperties', validateForm);
        wizard.on('submit.briefSummary', validateForm);
        wizard.on('submit.briefBackground', validateForm);
        wizard.on('submit.briefObjectives', validateForm);
        wizard.on('submit.briefRequirements', validateForm);
        wizard.on('submit.briefScope', validateForm);
        wizard.on('submit.briefBenefits', validateForm);
        wizard.on('submit.briefTimeframe', validateForm);
        wizard.on('submit.briefRisks', validateForm);
        wizard.on('submit.briefConstraints', validateForm);
        wizard.on('submit.briefStakeholders', validateForm);

        //itg-change request-template
        wizard.on('submit.changeRequestProperties', validateForm);
        wizard.on('submit.changeRequestSummary', validateForm);
        wizard.on('submit.changeRequestBackground', validateForm);
        wizard.on('submit.changeRequestObjectives', validateForm);
        wizard.on('submit.changeRequestRequirements', validateForm);
        wizard.on('submit.changeRequestScope', validateForm);
        wizard.on('submit.changeRequestBenefits', validateForm);
        wizard.on('submit.changeRequestTimeframe', validateForm);
        wizard.on('submit.changeRequestRisks', validateForm);
        wizard.on('submit.changeRequestConstraints', validateForm);
        wizard.on('submit.changeRequestStakeholders', validateForm);

        //itg-release note-template
        wizard.on('submit.releaseNoteProperties', validateForm);
        wizard.on('submit.releaseNoteMethodStatement', validateForm);
        wizard.on('submit.releaseNoteSummary', validateForm);
        wizard.on('submit.releaseNoteRisks', validateForm);
        wizard.on('submit.releaseNoteAuthority', validateForm);
        wizard.on('submit.releaseNoteTimeframe', validateForm);

        //itg-job request-template
        wizard.on('submit.jobRequestProperties', validateForm);
        wizard.on('submit.jobRequestProperties2', validateForm);
        wizard.on('submit.jobRequestDescription', validateForm);
        wizard.on('submit.jobRequestDependencies', validateForm);
        wizard.on('post-render.jobRequestJobSteps', buildJobStepForm);
        wizard.on('submit.jobRequestJobSteps', validateForm);
        wizard.on('submit.jobRequestMonitoring', validateForm);
        wizard.on('submit.jobRequestOnFailure', validateForm);
        wizard.on('submit.jobRequestSegmentInfo', validateForm);
        wizard.on('post-render.jobRequestSegmentInfo', buildSegmentInfoForm);
        wizard.on('submit.jobRequestPrepops', validateForm);
        wizard.on('submit.jobRequestPurpose', validateForm);

        //itg-digital socialmedia brief-template
        wizard.on('submit.socialMediaBriefProperties', validateForm);
        wizard.on('post-render.socialMediaPageSetup', buildFbSetupPage)
        wizard.on('submit.socialMediaPageSetup', validateForm);
        wizard.on('submit.socialMediaContent', validateForm);
        wizard.on('submit.socialMediaSetupAndBudget', validateForm);

        //itg-digital creative brief-template
        wizard.on('submit.commonPagesAdditionalInfo', validateFormForNext);
        wizard.on('submit.commonPagesCopywriting', validateForm);
        wizard.on('submit.commonPagesCreativeKBO', validateForm);
        wizard.on('submit.commonPagesCreativeAudience', validateForm);
        wizard.on('submit.commonPagesCreativeCommsObj', validateForm);
        wizard.on('submit.commonPagesCreativeAdapt', validateForm);
        wizard.on('submit.commonPagesCreativeAssets', validateForm);

//        wizard.on('submit.testPlanProperties', validateForm);
//        wizard.on('submit.testPlanSummary', validateForm);
//        wizard.on('submit.testPlanBackground', validateForm);
//        wizard.on('submit.testPlanObjectives', validateForm);
//        wizard.on('submit.testPlanRequirements', validateForm);
//        wizard.on('submit.testPlanScope', validateForm);
//        wizard.on('submit.testPlanBenefits', validateForm);
//        wizard.on('submit.testPlanTimeframe', validateForm);
//        wizard.on('submit.testPlanRisks', validateForm);
//        wizard.on('submit.testPlanConstraints', validateForm);
//        wizard.on('submit.testPlanStakeholders', validateForm);
    }
    );
}));
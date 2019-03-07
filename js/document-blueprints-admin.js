/* global AJS, _, $, Backbone, $select */

/**
 * @Author: Steve Killelay
 * */
(function ($) {
    // DOM ready
    AJS.$(document).ready(function () {

		$.ajaxSetup({
		    async: false
		});
		url = AJS.contextPath() + "/rest/dbds/latest/admin/department/0";
        $.getJSON(url)
            .done(function (json) {
                $('#departmentJson').val(JSON.stringify(json));
            })
            .fail(function (xhr, textStatus, error) {
                var err = textStatus + ", " + error;
                console.error("Request Failed: " + err);
            });

//		url = "https://jira.itg.co.uk/rest/api/2/project";
//        $.getJSON(url)
//            .done(function (json) {
//                $('#projectJson').val(JSON.stringify(json));
//            })
//            .fail(function (xhr, textStatus, error) {
//                var err = textStatus + ", " + error;
//                console.error("Request Failed: " + err);
//            });

		url = AJS.contextPath() + "/rest/api/space";
        $.getJSON(url)
            .done(function (json) {
                $('#spaceJson').val(JSON.stringify(json.results));
            })
            .fail(function (xhr, textStatus, error) {
                var err = textStatus + ", " + error;
                console.error("Request Failed: " + err);
            });
		$.ajaxSetup({
		    async: true
		});

        var defaultSpaceEditView = AJS.RestfulTable.CustomEditView.extend({
            render: function (self) {
                txt = $("#spaceJson").val();
                var json = [];
                if (txt.length===0){
                    url = AJS.contextPath() + "/rest/api/space";
                    $.getJSON(url)
                        .done(function (json) {
                            $('#spaceJson').val(JSON.stringify(json.results));
                        })
                        .fail(function (xhr, textStatus, error) {
                            var err = textStatus + ", " + error;
                            console.error("Request Failed: " + err);
                        });
                }
                else{
                    var json = JSON.parse(txt);
                }
                var $select = $("<select name='defaultSpaceKey' class='select'>");
                for (var itm in json) {
                    $select.append($("<option>", {value: json[itm].key, text: json[itm].name}));
                }
                $select.selected = self.value;
                return $select;
            }
        });


        var defaultSpaceReadView = AJS.RestfulTable.CustomEditView.extend({
            render: function (self) {
                txt = $("#spaceJson").val();
                if (txt.length>0){
	                json = JSON.parse($("#spaceJson").val());
	                for(var itm in json){
	                    if(json[itm].key === self.value){
	                        return json[itm].name;
	                    }
	                }
	            }
				return self.value;
            }
        });

        var projectView = AJS.RestfulTable.CustomEditView.extend({
            render: function (self) {
                txt = $("#departmentJson").val();
	            var json = [];
	            if (txt.length===0){
	                url = "https://jira.creator.co.uk/rest/api/2/project";
	                $.getJSON(url)
                        .done(function (json) {
                            $('#projectJson').val(JSON.stringify(json));
                        })
                        .fail(function (xhr, textStatus, error) {
                            var err = textStatus + ", " + error;
                            console.error("Request Failed: " + err);
                        });
	            }
	            else{
	                var json = JSON.parse(txt);
	            }
	            var $select = $("<select name='jiraProjectKey' class='select'>");
                    for (var itm in json) {
                        $select.append($("<option>", {value: json[itm].id, text: json[itm].department}));
                    }
                    $select.selected = self.value;
                    return $select;

            }
        });

        var hiddenView = AJS.RestfulTable.CustomReadView.extend({render: function (self) {
                return $("<input type='hidden' />").text(self);
            }});

        var booleanView = AJS.RestfulTable.CustomReadView.extend({render: function (self) {
                return $("<select name='" + self.name + "' class='select'><option value=true>Yes</option><option value=false>No</option></select>").selected(self);
            }});

        var booleanReadView = AJS.RestfulTable.CustomReadView.extend({render: function (self) {
                return self.value === true ? 'Yes' : 'No';
            }});

        var departmentView = AJS.RestfulTable.CustomEditView.extend({
            render: function (self) {
                txt = $("#departmentJson").val();
                var json = [];
                if (txt.length===0){
                    $.getJSON( AJS.contextPath() + "/rest/dbds/latest/admin/department/0")
                        .done(function (json) {
                            $('#departmentJson').val(JSON.stringify(json));
                        })
                        .fail(function (xhr, textStatus, error) {
                            var err = textStatus + ", " + error;
                            console.error("Request Failed: " + err);
                        });
                }
                else {
					var json = JSON.parse(txt);
                }
                var $select = $("<select name='department' class='select'>");
                for (var itm in json) {
                    $select.append($("<option>", {value: json[itm].id, text: json[itm].department}));
                }
                $select.selected = self.value;
                return $select;
            }
        });

        var departmentReadView = AJS.RestfulTable.CustomReadView.extend({
            render: function (self) {
                txt = $("#departmentJson").val();
                if (txt.length>0){
	                json = JSON.parse($("#departmentJson").val());
	                for(var itm in json){
	                    if(json[itm].id === self.value){
	                        return json[itm].department;
	                    }
	                }
	            }
				return self.value;
            }
        });

		var securityStatusEditView = AJS.RestfulTable.CustomReadView.extend({
             render: function (self) {
					var $select = $("<select name='default_status' class='select'>");
					$select.append($("<option>", {value: "confidential", text: "Confidential"}));
					$select.append($("<option>", {value: "internal", text: "Internal"}));
					$select.append($("<option>", {value: "public", text: "Public"}));
					$select.selected = self.value;
					return $select;
             }
         });

        var confirmDelete = function () {
            return '<section role="dialog" id="cep-confirm-delete-dialog" class="aui-dialog2 aui-dialog2-small aui-dialog2-warning">' +
                    '	<header class="aui-dialog2-header">' +
                    '		<h2 class="aui-dialog2-header-main">Delete?</h2>' +
                    '	</header>' +
                    '	<div class="aui-dialog2-content" style="height:100px">' +
                    '       <p>Do you really want to delete?</p>' +
                    '	</div>' +
                    '	<footer class="aui-dialog2-footer">' +
                    '		<div class="aui-dialog2-footer-actions">' +
                    '			<button id="dialog-close-button" class="aui-button cancel">No</button>' +
                    '		</div>' +
                    '		<div class="aui-dialog2-footer-hint">' +
                    '		    <form>' +
                    '			    <input type="submit" id="dialog-submit-button" class="aui-button aui-button-primary" value="Yes"/>' +
                    '		    </form>' +
                    '       </div>' +
                    '	</footer>' +
                    '</section>';
        }

        var modelExtension = Backbone.Model.extend({
             /**
              * Override the default backbone save method as the default only display values of the changed fields after update
              */
             save: function (attributes, options) {
                 options = options || {};

                 var instance = this,
                         Model,
                         syncModel,
                         error = options.error, // we override, so store original
                         success = options.success;


                 // override error handler to provide some defaults
                 options.error = function (model, xhr) {

                     var data = $.parseJSON(xhr.responseText || xhr.data);

                     // call original error handler
                     if (error) {
                         error.call(instance, instance, data, xhr);
                     }
                 };

                 // if it is a new model, we don't have to worry about updating only changed attributes because they are all new
                 if (this.isNew()) {
                     //if(this.attributes == attributes){
                     // call super
                     Backbone.Model.prototype.save.call(this, attributes, options);

                     // only go to server if something has changed
                 } else if (attributes) {

                     // create temporary model
                     Model = AJS.RestfulTable.EntryModel.extend({
                         url: this.url()
                     });

                     syncModel = new Model({
                         id: this.id
                     });

                     syncModel.save = Backbone.Model.prototype.save;

                     options.success = function (model, xhr) {

                         // update original model with saved attributes
                         instance.clear().set(model.toJSON());

                         // call original success handler
                         if (success) {
                             success.call(instance, instance, xhr);
                         }
                     };

                     // update temporary model with the changed attributes
                     syncModel.save(attributes, options);
                 }
             }
         })


        var auiEvents = ["ROW_ADDED", "REORDER_SUCCESS", "ROW_REMOVED", "EDIT_ROW", "INITIALIZED", "VALIDATION_ERROR", "SUBMIT_STARTED", "SUBMIT_FINISHED"];
        _.each(auiEvents, function (event) {
            $(AJS).one(AJS.RestfulTable.Events[event], function (event) {
                opts = {
                    id: event.namespace,
                    title: "Event Check",
                    body: event.namespace + " fired on AJS.",
                    fadeout: true,
                    closable: true
                };
                if (event.namespace === "VALIDATION_ERROR") {
                    msg.error("#message-area", opts);
                } else {
                    msg.info("#message-area", opts);
                }
            });
        });


        var client = AJS.RestfulTable;
        var msg = AJS.messages;
        var url = AJS.contextPath() + "/rest/dbds/latest/client";
        $clientTable = $("#blueprint-client-config");
        client = new client({
            id: "blueprint-client-data",
            el: $clientTable, // <table>
            columns: [
                {fieldName: "id", id: "id", header: "Id", allowEdit: false},
                {fieldName: "clientId", id: "client_key", header: "Client Id", allowEdit: true},
                {fieldName: "clientName", id: "client_name", header: "Client Name", allowEdit: true},
                {fieldName: "defaultSpaceKey", id: "default_space_key", header: "Default Space", allowEdit: true, readView: defaultSpaceReadView, editView: defaultSpaceEditView, createView: defaultSpaceEditView}, //}, taken out not working
                {fieldName: "clientLogoFileName", id: "client_logo_file_name", header: "Logo File Name", allowEdit: true},
                {fieldName: "jiraProjectKey", id: "jira_project_key", header: "JIRA Project Key", allowEdit: true} //, editView: ProjectView, createView: ProjectView} need jira to be on AD
            ],
            resources: {
                all: url + "/0",
                self: url
            },

            autofocus: true, // auto focus first field of create row
            noEntriesMsg: "No Data!",

            fieldFocusSelector: function (name) {
                return ":input[type!=hidden][name=" + name + "], #" + name + ", .ajs-restfultable-input-" + name;
            },

            deleteConfirmation: confirmDelete,
            model: modelExtension
        });

        var template = AJS.RestfulTable;
        url = AJS.contextPath() + "/rest/dbds/latest/admin/template";
        $templateTable = $("#blueprint-template-config");
        template = new template({
            id: "blueprint-template-data",
            el: $templateTable, // <table>
            columns: [
                {fieldName: "id", id: "id", header: "PK", allowEdit: false},
                {fieldName: "template_name", id: "template_name", header: "Template Name", allowEdit: true},
                {fieldName: "department", id: "department", header: "Department", allowEdit: true, createView:departmentView, editView:departmentView, readView:departmentReadView},
                {fieldName: "default_status", id: "default_status", header: "Default Security Status", allowEdit: true, createView:securityStatusEditView, editView:securityStatusEditView},
                {fieldName: "enabled", id: "enabled", header: "Enabled", allowEdit: true, createView: booleanView, editView: booleanView, readView: booleanReadView}
            ],
            resources: {
                all: url + "/0",
                self: url
            },

            autofocus: true, // auto focus first field of create row
            noEntriesMsg: "No Data!",
            addPosition: "bottom",

            fieldFocusSelector: function (name) {
                return ":input[type!=hidden][name=" + name + "], #" + name + ", .ajs-restfultable-input-" + name;
            },

            deleteConfirmation: confirmDelete,
            model: modelExtension
        }
        );

        var department = AJS.RestfulTable;
            url = AJS.contextPath() + "/rest/dbds/latest/admin/department";
            $departmentTable = $("#blueprint-department-config");

        department = new department({
            id: "blueprint-department-data",
            el: $departmentTable, // <table>
            columns: [
                {fieldName: "id", id: "id", header: "Id", allowEdit: false},
                {fieldName: "department", id: "department", header: "Department",createView:departmentView, editView:departmentView, readView:departmentReadView}
            ],
            resources: {
                all: url + "/0",
                self: url
            },

            autofocus: true, // auto focus first field of create row
            noEntriesMsg: "No Data!",
            addPosition: "bottom",

            fieldFocusSelector: function (name) {
                return ":input[type!=hidden][name=" + name + "], #" + name + ", .ajs-restfultable-input-" + name;
            },

            deleteConfirmation: confirmDelete,
            model: modelExtension
        }
        );
    });
})(AJS.$);
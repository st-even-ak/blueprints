/*
 * Author: steve.killelay
 * Last Updated: 22/02/19 12:21
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
package com.itg.plugins.confluence.rest;

import com.atlassian.json.jsonorg.JSONArray;
import com.atlassian.json.jsonorg.JSONObject;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.itg.plugins.confluence.implementation.models.DepartmentModel;
import com.itg.plugins.confluence.implementation.utils.Helpers;
import com.itg.plugins.confluence.interfaces.IDepartmentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Steve.Killelay
 */
@Path("admin/department")
@Produces(MediaType.APPLICATION_JSON)
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class DepartmentService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DepartmentService.class);

	private final IDepartmentProvider _departmentProvider;

	@Autowired
	public DepartmentService(final IDepartmentProvider departmentProvider) {

		this._departmentProvider = departmentProvider;
	}

	@AnonymousAllowed
	@GET
	@Path("{departmentId:([0-9]*)}")
	public Response getDepartment(@PathParam("departmentId") final int departmentId) {

		try {
			List<Map<String, Object>> data = _departmentProvider.getDepartment(departmentId);

			JSONArray json = Helpers.mapToJson(data);
			return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
		} catch (SQLException e) {

			LOGGER.error("SQL error while retrieving client details -->", e);
			return Response.noContent().build();
		}
	}

	@POST
	public Response createDepartment(final DepartmentModel model) {

		try {
			Map<String, Object> data = _departmentProvider.createDepartment(model);

			JSONObject json = Helpers.mapToJson(data);
			return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
		} catch (SQLException e) {

			LOGGER.error("SQL error while creating a new client -->", e);
			return Response.serverError().type(MediaType.APPLICATION_JSON).build();
		}
	}

	@PUT
	@Path("{clientId:([0-9]*)}")
	public Response updateDepartment(final DepartmentModel model, @PathParam("departmentId") final int departmentId) {

		try {
			Map<String, Object> data = _departmentProvider.updateDepartment(model);

			JSONObject json = Helpers.mapToJson(data);
			return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
		} catch (SQLException e) {
			LOGGER.error("SQL error while updating the client record -->", e);
			return Response.serverError().entity(e).type(MediaType.APPLICATION_JSON).build();
		}
	}

	@DELETE
	@Path("{clientId:([0-9]*)}")
	public Response deleteDepartment(@PathParam("departmentId") final int departmentId) {

		try {
			_departmentProvider.deleteDepartment(departmentId);
			return Response.ok().status(202).build();
		} catch (SQLException e) {

			LOGGER.error("SQL error while deleting the client record -->", e);
			return Response.serverError().build();
		}
	}
}

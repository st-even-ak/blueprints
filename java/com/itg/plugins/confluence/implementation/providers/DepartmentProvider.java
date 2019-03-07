/*
 * Author: steve.killelay
 * Last Updated: 22/02/19 12:24
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

import com.itg.plugins.confluence.implementation.abstrct.AbstractJDBCPostgres;
import com.itg.plugins.confluence.implementation.models.DepartmentModel;
import com.itg.plugins.confluence.implementation.properties.ConfluenceDatabaseProperties;
import com.itg.plugins.confluence.implementation.utils.Helpers;
import com.itg.plugins.confluence.interfaces.IDepartmentProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Steve.Killelay
 */
@Component
public class DepartmentProvider extends AbstractJDBCPostgres implements IDepartmentProvider {

    @Autowired
    public DepartmentProvider(final ConfluenceDatabaseProperties properties) {

        super(properties);
    }

    @Override
    public List<Map<String, Object>> getDepartment(int departmentId) {

        StringBuilder sql = new StringBuilder();
        sql.append("select id, department from itg.blueprint_departments\n");
        if (departmentId > 0) {
            sql.append("where id=")
                    .append(departmentId);
        }
        try {
            return ExecuteReturnStatement("confluence", sql.toString(), null);
        } catch (SQLException e) {
            return null;

        }
    }

    @Override
    public Map<String, Object> createDepartment(DepartmentModel model)  {

        try {
            String sql = "insert into itg.blueprint_departments (department, enabled) values ( ?, ?) returning itg.blueprint_departments.*;";
            HashMap<String, Object> params = Helpers.createParamMap(model);
            return ExecuteReturnStatement("confluence", sql, params).get(0);
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public Map<String, Object> updateDepartment(DepartmentModel model)  {

        deleteDepartment(model.id);
        return createDepartment(model);
    }

    @Override
    public void deleteDepartment(int departmentId)  {

        try {
            String sql = "delete from itg.blueprint_departments where id = ".concat(Integer.toString(departmentId));
            ExecuteStatement("confluence", sql, null);
        } catch (SQLException e) {
        }
    }
}

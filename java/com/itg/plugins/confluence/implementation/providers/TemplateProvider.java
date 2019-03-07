/*
 * Author: steve.killelay
 * Last Updated: 26/02/19 10:07
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
import com.itg.plugins.confluence.implementation.utils.Helpers;
import com.itg.plugins.confluence.implementation.models.TemplateModel;
import com.itg.plugins.confluence.implementation.properties.ConfluenceDatabaseProperties;
import com.itg.plugins.confluence.interfaces.ITemplateProvider;
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
public class TemplateProvider extends AbstractJDBCPostgres implements ITemplateProvider {

    @Autowired
    public TemplateProvider(final ConfluenceDatabaseProperties properties) {

        super(properties);
    }

    @Override
    public List<Map<String, Object>> getTemplate(int id) {

        StringBuilder sql = new StringBuilder();
        sql.append("select id, template_name, department, enabled from itg.blueprint_templates\n");
        if (id > 0) {
            sql.append("where id=")
                    .append(id);
        }
        try {
            return ExecuteReturnStatement("confluence", sql.toString(), null);
        } catch (SQLException e) {
            return null;

        }
    }

    @Override
    public List<Map<String, Object>> getTemplateListByDepartment(int department) throws SQLException {

        StringBuilder sql = new StringBuilder();
        sql.append("select *\n");
        sql.append("from itg.blueprint_templates templates\n");
        sql.append("where department=");
        sql.append(department);
        sql.append(" and enabled=true\n");
        sql.append("order by template_name;");

        try {
            return ExecuteQuery("confluence", sql.toString(), null);

        } catch (SQLException e) {

            throw e;
        }
    }

    @Override
    public Map<String, Object> createTemplate(TemplateModel model) {

        try {
            String sql = "insert into itg.blueprint_templates (template_name, department, enabled) values ( ?, ?, ?) returning itg.blueprint_templates.*;";
            HashMap<String, Object> params = Helpers.createParamMap(model);
            return ExecuteReturnStatement("confluence", sql, params).get(0);
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public Map<String, Object> updateTemplate(TemplateModel model) {

        deleteTemplate(model.id);
        return createTemplate(model);
    }

    @Override
    public void deleteTemplate(int id) {

        try {
            String sql = "delete from itg.blueprint_templates where id = ".concat(Integer.toString(id));
            ExecuteStatement("confluence", sql, null);
        } catch (SQLException e) {
        }
    }
}

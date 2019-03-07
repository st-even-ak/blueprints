/*
 * Author: steve.killelay
 * Last Updated: 21/02/19 22:13
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
import com.itg.plugins.confluence.implementation.models.SegmentInfoModel;
import com.itg.plugins.confluence.implementation.properties.ConfluenceDatabaseProperties;
import com.itg.plugins.confluence.interfaces.ISegmentInfoProvider;
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
public class SegmentInfoProvider extends AbstractJDBCPostgres implements ISegmentInfoProvider {

    @Autowired
    public SegmentInfoProvider(final ConfluenceDatabaseProperties properties) {
        super(properties);
    }

    @Override
    public List<Map<String, Object>> getSegment(String sessionId, int id) throws SQLException {

        StringBuilder sql = new StringBuilder();
        sql.append("select * from itg.blueprint_jr_segment_info where session_key = ")
                .append(sessionId);
        if (id > 0) {
            sql.append(" and id = ")
                    .append(id);
        } else {
            sql.append(" order by id");
        }
        try {
            return ExecuteQuery("confluence", sql.toString(), null);

        } catch (SQLException e) {

            throw e;
        }
    }

    @Override
    public List<Map<String, Object>> getSegmentSession(String sessionId) throws SQLException {

        StringBuilder sql = new StringBuilder();
        sql.append("select * from itg.blueprint_jr_segment_info where session_key = '")
                .append(sessionId)
                .append("'");
        try {
            return ExecuteQuery("confluence", sql.toString(), null);

        } catch (SQLException e) {

            throw e;
        }
    }

    @Override
    public Map<String, Object> createSegment(SegmentInfoModel segment) throws SQLException {

        StringBuilder sql = new StringBuilder();
        try {
            sql.append("insert into itg.blueprint_jr_segment_info ");
            HashMap<String, Object> params = Helpers.createParamMap(segment);

            String fields = "( )";
            String values = "values ( )";

            for (Map.Entry<String, Object> kvp : params.entrySet()) {
                if (kvp.getValue() != null) {
                    fields = fields.substring(0, fields.length() - 1).concat("\"").concat(kvp.getKey()).concat("\",").concat(fields.substring(fields.length() - 1));
                    values = values.substring(0, values.length() - 1).concat("?,").concat(values.substring(values.length() - 1));
                }
            }

            sql.append(fields)
                    .append(values.replace(",)", ") "))
                    .append("returning itg.blueprint_jr_segment_info.*");
            return ExecuteReturnStatement("confluence", sql.toString(), params).get(0);

        } catch (SQLException e) {

            throw e;
        }
    }

    @Override
    public Map<String, Object> updateSegment(SegmentInfoModel segment) throws SQLException {

        try {
            String sql = "update itg.blueprint_jr_segment_info set ";

            HashMap<String, Object> params = Helpers.createParamMap(segment);
            sql = params.entrySet().stream().filter((kvp) -> (!kvp.getKey().equals("id") && kvp.getValue() != null)).map((kvp) -> "\"" + kvp.getKey() + "\" = ? ,").reduce(sql, String::concat);

            sql = sql.substring(0, sql.length() - 1);
            sql += "where id = ".concat(params.get("id").toString()).concat(" returning itg.blueprint_jr_segment_info.*");

            params.remove("id");

            return ExecuteReturnStatement("confluence", sql, params).get(0);

        } catch (SQLException e) {

            throw e;
        }
    }

    @Override
    public void deleteSegment(int id) throws SQLException {

        StringBuilder sql = new StringBuilder();
        sql.append("delete from itg.blueprint_jr_segment_info");
        if (id > 0) {
            sql.append(" where id = ").append(id);
        }
        try {
            ExecuteStatement("confluence", sql.toString(), null);
        } catch (SQLException e) {

            throw e;
        }
    }

    @Override
    public void deleteSegmentSession(String sessionId) throws SQLException {

        StringBuilder sql = new StringBuilder();
        sql.append("delete from itg.blueprint_jr_segment_info where session_key = ")
                .append(sessionId);
        try {
            ExecuteStatement("confluence", sql.toString(), null);

        } catch (SQLException e) {

            throw e;
        }
    }
}

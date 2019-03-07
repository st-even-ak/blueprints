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
package com.itg.plugins.confluence.implementation.providers;

import com.atlassian.json.jsonorg.JSONObject;
import com.itg.plugins.confluence.implementation.abstrct.AbstractJDBCPostgres;
import com.itg.plugins.confluence.implementation.utils.Helpers;
import com.itg.plugins.confluence.implementation.models.ClientModel;
import com.itg.plugins.confluence.implementation.properties.ConfluenceDatabaseProperties;
import com.itg.plugins.confluence.interfaces.IClientProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Steve.Killelay
 */
@Component
public class ClientProvider extends AbstractJDBCPostgres implements IClientProvider {

    @Autowired
    public ClientProvider(final ConfluenceDatabaseProperties properties) {

        super(properties);
    }

    /**
     * *
     * @param clientId
     * @throws SQLException
     * @return
     */
    @Override
    public List<Map<String, Object>> getClientData(int clientId) throws SQLException {

        try {
            String sql = "select * from itg.blueprint_documents_clients";
            if (clientId > 0) {
                sql = sql.concat(" where id = ").concat(Integer.toString(clientId));
            } else {
                sql = sql.concat(" order by client_name");
            }
            return ExecuteQuery("confluence", sql, null);

        } catch (SQLException e) {

            throw e;
        }
    }

    /**
     * *
     * @description Describe me
     * @param spaceKey
     * @throws SQLException
     * @return
     */
    @Override
    public List<Map<String, Object>> getClientsInSpace(String spaceKey) throws SQLException {

        try {
            String sql = "select * from itg.getclientsinspace('".concat(spaceKey).concat("')");
            return ExecuteQuery("confluence", sql, null);

        } catch (SQLException e) {
            throw e;
        }
    }

    /**
     * @param clientId
     * @return
     */
    @Override
    public String getClientName(int clientId) {

        try {
            Map<String, Object> clientRecord = getClientData(clientId).get(0);
            JSONObject json = Helpers.mapToJson(clientRecord);

            return json.get("client_name").toString();

        } catch (SQLException ex) {
            Logger.getLogger(BlueprintProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }


    /**
     * *
     * @use Describe me
     * @param clientId
     * @return
     */
    @Override
    public String getClientLogoFileName(int clientId) {

        try {
            Map<String, Object> clientRecord = getClientData(clientId).get(0);
            JSONObject json = Helpers.mapToJson(clientRecord);

            return json.get("client_logo_file_name").toString();

        } catch (SQLException ex) {
            Logger.getLogger(BlueprintProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }

    /**
     * *
     * @description Describe me
     * @param clientId
     * @return
     */
    @Override
    public String getRequiredSpaceKey(int clientId) {

        try {
            Map<String, Object> clientRecord = getClientData(clientId).get(0);
            JSONObject json = Helpers.mapToJson(clientRecord);

            return json.get("default_space_key").toString();

        } catch (SQLException ex) {
            Logger.getLogger(BlueprintProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    /**
     * *
     * @description Describe me
     * @param client
     * @throws SQLException
     * @return
     */
    @Override
    public Map<String, Object> createClient(ClientModel client) throws SQLException {

        try {
            String sql = "insert into itg.blueprint_documents_clients ";
            HashMap<String, Object> params = Helpers.createParamMap(client);

            String fields = "( )";
            String values = "values ( )";

            for (Map.Entry<String, Object> kvp : params.entrySet()) {
                if (kvp.getValue() != null) {
                    fields = fields.substring(0, fields.length() - 1).concat("\"").concat(kvp.getKey()).concat("\",").concat(fields.substring(fields.length() - 1));
                    values = values.substring(0, values.length() - 1).concat("?,").concat(values.substring(values.length() - 1));
                }
            }

            sql = sql.concat(fields).concat(values).replace(",)", ") ").concat("returning itg.blueprint_documents_clients.*");
            return ExecuteReturnStatement("confluence", sql, params).get(0);

        } catch (SQLException e) {

            throw e;
        }
    }

    /**
     * *
     * @param client
     * @throws SQLException
     * @return
     */
    @Override
    public Map<String, Object> updateClient(ClientModel client) throws SQLException {

        try {
            String sql = "update itg.blueprint_documents_clients set ";

            HashMap<String, Object> params = Helpers.createParamMap(client);
            sql = params.entrySet().stream().filter((kvp) -> (!kvp.getKey().equals("id") && kvp.getValue() != null)).map((kvp) -> "\"" + kvp.getKey() + "\" = ? ,").reduce(sql, String::concat);

            sql = sql.substring(0, sql.length() - 1);
            sql += "where id = ".concat(params.get("id").toString()).concat(" returning itg.blueprint_documents_clients.*");

            params.remove("id");

            return ExecuteReturnStatement("confluence", sql, params).get(0);

        } catch (SQLException e) {

            throw e;
        }

    }

    /**
     * *
     * @description Describe me
     * @param clientId
     * @throws SQLException
     */
    @Override
    public void deleteClient(int clientId) throws SQLException {

        try {
            String sql = "delete from itg.blueprint_documents_clients where id = ".concat(Integer.toString(clientId));
            ExecuteStatement("confluence", sql, null);
        } catch (SQLException e) {

            throw e;
        }
    }
}

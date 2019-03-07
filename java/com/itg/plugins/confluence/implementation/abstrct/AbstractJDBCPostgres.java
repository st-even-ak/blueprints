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
package com.itg.plugins.confluence.implementation.abstrct;

import com.itg.plugins.confluence.interfaces.IConfluenceDatabaseProperties;
import org.h2.jdbc.JdbcConnection;

import java.sql.Date;
import java.sql.*;
import java.util.*;
import java.util.Map.Entry;

import static com.atlassian.core.util.ClassLoaderUtils.loadClass;

/**
 *
 * @author Steve.Killelay
 */
public abstract class AbstractJDBCPostgres {

	private final IConfluenceDatabaseProperties _properties;

	protected AbstractJDBCPostgres(IConfluenceDatabaseProperties properties){

		this._properties = properties;
	}

	private Connection getConnection(String database) throws SQLException {

		loadDriver(_properties.getDriverClass());

		Properties connProperties = new Properties();
		connProperties.put("user", _properties.getUsername());
		connProperties.put("password", _properties.getPassword());

		return DriverManager.getConnection(_properties.getConnectionURL().concat(database), connProperties);
	}

	protected List<Map<String, Object>> ExecuteQuery(String database, String sql, HashMap<String, Object> params) throws SQLException {

		Connection conn = getConnection(database);

		if (params == null) {
			params = new HashMap<>();
		}

		try {
			conn.setAutoCommit(false);

			boolean first = true;
			StringBuilder sqlBuilder = new StringBuilder(sql);
			for (Entry<String, Object> kvp : params.entrySet()) {
				if (kvp.getValue() != null) {
					if (first) {
						sqlBuilder.append(" where ").append(kvp.getKey()).append("=?");
						first = false;
					} else {
						sqlBuilder.append(" and ").append(kvp.getKey()).append("=?");
					}
				}
			}
			sql = sqlBuilder.toString();

			PreparedStatement query = conn.prepareStatement(sql);

			int paramNumber = 1;
			for (Entry<String, Object> kvp : params.entrySet()) {
				if (kvp.getValue() != null) {
					if (kvp.getValue() instanceof Date) {
						query.setDate(paramNumber, (Date) kvp.getValue());
					} else if (kvp.getValue() instanceof Integer) {
						query.setInt(paramNumber, (Integer) kvp.getValue());
					} else if (kvp.getValue() instanceof Long) {
						query.setLong(paramNumber, (Long) kvp.getValue());
					} else if (kvp.getValue() instanceof Float) {
						query.setFloat(paramNumber, (Float) kvp.getValue());
					} else if (kvp.getValue() instanceof Byte) {
						query.setByte(paramNumber, (Byte) kvp.getValue());
					} else {
						query.setString(paramNumber, kvp.getValue().toString());
					}
					paramNumber++;
				}
			}

			return getEntitiesFromResultSet(query.executeQuery());
		} catch (SQLException e) {

			return null;
		}
	}

	/**
	 * @author Steve.Killelay
	 * @param database: the name od the db to execute against
	 * @param sql: a basic select/delete statement; if the select statement contains a where clause DO NOT supply params
	 * @param params: key value pair define a [table.]field and value to be injected as a where clause
	 * @throws SQLException
	 */
	protected void ExecuteStatement(String database, String sql, HashMap<String, Object> params) throws SQLException {

		Connection conn = getConnection(database);

		if (params == null) {
			params = new HashMap<>();
		}

		try {
			conn.setAutoCommit(false);

			PreparedStatement query = conn.prepareStatement(sql);

			int paramNumber = 1;
			for (Entry<String, Object> kvp : params.entrySet()) {
				if (kvp.getValue() != null) {
					if (kvp.getValue() instanceof Date) {
						query.setDate(paramNumber, (Date) kvp.getValue());
					} else if (kvp.getValue() instanceof Integer) {
						query.setInt(paramNumber, (Integer) kvp.getValue());
					} else if (kvp.getValue() instanceof Long) {
						query.setLong(paramNumber, (Long) kvp.getValue());
					} else if (kvp.getValue() instanceof Float) {
						query.setFloat(paramNumber, (Float) kvp.getValue());
					} else if (kvp.getValue() instanceof Byte) {
						query.setByte(paramNumber, (Byte) kvp.getValue());
					} else {
						query.setString(paramNumber, kvp.getValue().toString());
					}
					paramNumber++;
				}
			}
			query.execute();

			//  if we get here then no errors so commit
			conn.commit();
		} catch (SQLException e) {

			conn.rollback();
		}
	}

	/**
	 * @author Steve.Killelay
	 * @param database: the name od the db to execute against
	 * @param sql: a basic select/delete statement; if the select statement contains a where clause DO NOT supply params
	 * @param params: key value pair define a [table.]field and value to be injected as a where clause
	 * @return
	 * @throws SQLException
	 */
	protected List<Map<String, Object>> ExecuteReturnStatement(String database, String sql, HashMap<String, Object> params) throws SQLException {

		Connection conn = getConnection(database);

		if (params == null) {
			params = new HashMap<>();
		}

		try {
			conn.setAutoCommit(false);

			PreparedStatement query = conn.prepareStatement(sql);

			int paramNumber = 1;
			for (Entry<String, Object> kvp : params.entrySet()) {
				if (kvp.getValue() != null) {
					if (kvp.getValue() instanceof Date) {
						query.setDate(paramNumber, (Date) kvp.getValue());
					} else if (kvp.getValue() instanceof Integer) {
						query.setInt(paramNumber, (Integer) kvp.getValue());
					} else if (kvp.getValue() instanceof Long) {
						query.setLong(paramNumber, (Long) kvp.getValue());
					} else if (kvp.getValue() instanceof Float) {
						query.setFloat(paramNumber, (Float) kvp.getValue());
					} else if (kvp.getValue() instanceof Byte) {
						query.setByte(paramNumber, (Byte) kvp.getValue());
					} else {
						query.setString(paramNumber, kvp.getValue().toString());
					}
					paramNumber++;
				}
			}
			ResultSet data = query.executeQuery();

			//  if we get here then no errors so commit
			conn.commit();
			return getEntitiesFromResultSet(data);
		} catch (SQLException e) {

			conn.rollback();
			return null;
		}
	}

	private Driver loadDriver(final String driverName) throws SQLException {

		try {
			final Class cls = loadClass(driverName, JdbcConnection.class);
			return (Driver) cls.newInstance();
		} catch (ClassNotFoundException e) {
			throw new SQLException("JDBC driver class not found: " + driverName, e);
		} catch (IllegalAccessException | InstantiationException e) {
			throw new SQLException("Cannot instantiate JDBC driver class " + driverName + ": " + e.getMessage(), e);
		}
	}

	private List<Map<String, Object>> getEntitiesFromResultSet(ResultSet resultSet) throws SQLException {
		ArrayList<Map<String, Object>> entities = new ArrayList<>();
		while (resultSet.next()) {
			entities.add(getEntityFromResultSet(resultSet));
		}
		return entities;
	}

	private Map<String, Object> getEntityFromResultSet(ResultSet resultSet) throws SQLException {
		ResultSetMetaData metaData = resultSet.getMetaData();
		int columnCount = metaData.getColumnCount();
		Map<String, Object> resultsMap = new HashMap<>();
		for (int i = 1; i <= columnCount; ++i) {
			String columnName = metaData.getColumnName(i).toLowerCase();
			Object object = resultSet.getObject(i);
			resultsMap.put(columnName, object);
		}
		return resultsMap;
	}
}

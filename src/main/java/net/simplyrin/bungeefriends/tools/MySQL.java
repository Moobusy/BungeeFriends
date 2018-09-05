package net.simplyrin.bungeefriends.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by SimplyRin on 2018/08/14.
 *
 *  Copyright 2018 SimplyRin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class MySQL {

	private Connection connection;

	private String username;
	private String password;

	private String address;
	private String database;
	private String timezone;
	private boolean useSSL;

	private Statement statement;
	private String table;

	public MySQL(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public MySQL(String username, String password, String address, String database, String table, String timezone, boolean useSSL) {
		this.username = username;
		this.password = password;
		this.address = address;
		this.table = table;
		this.timezone = timezone;
		this.useSSL = useSSL;
	}

	public MySQL setAddress(String address) {
		this.address = address;
		return this;
	}

	public MySQL setDatabase(String database) {
		this.database = database;
		return this;
	}

	public MySQL setTable(String table) {
		this.table = table;
		return this;
	}

	public MySQL setTimezone(String timezone) {
		this.timezone = timezone;
		return this;
	}

	public MySQL setUseSSL(boolean useSSL) {
		this.useSSL = useSSL;
		return this;
	}

	public Editor connect() throws SQLException {
		this.connection = DriverManager.getConnection("jdbc:mysql://" + this.address + "/" + this.database + "?useSSL=" + this.useSSL + "&serverTimezone=" + this.timezone, this.username, this.password);
		this.statement = this.connection.createStatement();
		return new Editor(this.statement, this.table);
	}

	public void disconnect() {
		try {
			this.connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Editor reconnect() throws SQLException {
		this.disconnect();
		return this.connect();
	}

	public class Editor {

		private Statement statement;
		private String table;

		public Editor(Statement statement, String table) {
			this.statement = statement;
			this.table = table;
			try {
				this.statement.executeUpdate("create table if not exists " + this.table + " (_key varchar(4098), value varchar(4098)) charset=utf8;");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		public MySQL getMySQL() {
			return MySQL.this;
		}

		public boolean set(String key, List<String> list) {
			if(list.size() == 0) {
				return this.set(key, "[]");
			}
			String object = "";
			for(String content : list) {
				object += content + ",&%$%&,";
			}
			object = object.substring(0, object.length() - ",&%$%&,".length());
			return this.set(key, object);
		}

		public boolean set(String key, String object) {
			int result = 0;

			if(object == null) {
				try {
					this.statement.executeUpdate("delete from " + this.table + " where _key = '" + key + "';");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			try {
				result = this.statement.executeUpdate("update " + this.table + " set value = '" + object + "' where _key ='" + key + "'");
			} catch (SQLException e) {
				// e.printStackTrace();
				return false;
			}

			if(result == 0) {
				try {
					result = this.statement.executeUpdate("insert into " + this.table + " values ('" + key + "', '" + object + "');");
				} catch (SQLException e) {
					e.printStackTrace();
					return false;
				}
				if(result == 1) {
					return true;
				}
			}

			return false;
		}

		public String get(String key) {
			ResultSet resultSet;
			try {
				resultSet = this.statement.executeQuery("select * from " + this.table + ";");
				while(resultSet.next()) {
					if(resultSet.getString("_key").equals(key)) {
						String value = resultSet.getString("value");
						if(value.equals("null")) {
							return null;
						}
						return resultSet.getString("value");
					}
				}
			} catch (SQLException e) {
				// e.printStackTrace();
			}
			return null;
		}

		public List<String> getList(String key) {
			String value = this.get(key);
			if(value == null || value.equals("[]")) {
				System.out.println(key + " is null return new list");
				return new ArrayList<>();
			}
			String[] result = value.split(Pattern.quote(",&%$%&,"));
			List<String> list = new ArrayList<>();
			for(String content : result) {
				list.add(content);
			}
			return list;
		}

	}

}

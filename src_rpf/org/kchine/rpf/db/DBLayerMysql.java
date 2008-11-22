/*
 * Biocep: R-based Platform for Computational e-Science.
 *  
 * Copyright (C) 2007-2009 Karim Chine - karim.chine@m4x.org
 *  
 * Copyright (C) 2007 EMBL-EBI-Microarray Informatics
 *  
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */ 
package org.kchine.rpf.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.kchine.rpf.db.DBLayer;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class DBLayerMysql extends DBLayer {
	public DBLayerMysql(Connection conn) {
		super(conn);
	}

	protected void lock(Statement stmt) throws SQLException {
		stmt.execute("lock tables SERVANTS write");
	}

	protected void unlock(Statement stmt) throws SQLException {
		stmt.execute("unlock tables");
	}

	protected String sysdateFunctionName() {
		return "SYSDATE()";
	}

	@Override
	boolean isNoConnectionError(SQLException sqle) {
		return true;
	}

	@Override
	boolean isConstraintViolationError(SQLException sqle) {
		return true;
	}
}
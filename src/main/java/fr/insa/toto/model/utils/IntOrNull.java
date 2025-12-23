/*
Copyright 2000- Francois de Bertrand de Beuvron

This file is part of CoursBeuvron.

CoursBeuvron is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

CoursBeuvron is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with CoursBeuvron.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.insa.toto.model.utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author elio
 */
public class IntOrNull {
    public static final void setIntOrNull(PreparedStatement statement, int which, Integer value) throws SQLException {
        if (value == null)
            statement.setNull(which, java.sql.Types.INTEGER);
        else
            statement.setInt(which, value);
    }

    public static final Integer getIntOrNull(ResultSet rs, String which) throws SQLException {
        var tmp = rs.getInt(which);
        return rs.wasNull() ? null : tmp;
    }
}

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
package fr.insa.toto.model;

import fr.insa.beuvron.utils.database.ClasseMiroir;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author elio
 */
public class Matchs extends ClasseMiroir {
    private int ronde;
    private int idEquipeA;
    private int idEquipeB;

    public Matchs(int score, int idEquipeA, int idEquipeB) {
        this.ronde = score;
        this.idEquipeA = idEquipeA;
        this.idEquipeB = idEquipeB;
    }

    public int getIdEquipeA() {
        return idEquipeA;
    }

    public void setIdEquipeA(int idEquipeA) {
        this.idEquipeA = idEquipeA;
    }

    public int getIdEquipeB() {
        return idEquipeB;
    }

    public void setIdEquipeB(int idEquipeB) {
        this.idEquipeB = idEquipeB;
    }

    public int getRonde() {
        return ronde;
    }

    public void setRonde(int taillecm) {
        this.ronde = taillecm;
    }

    @Override
    protected Statement saveSansId(Connection con) throws SQLException {
        var st = con.prepareStatement("insert into matchs (ronde, idEquipeA, idEquipeB) values (?, ?, ?)");
        st.setInt(1, ronde);
        st.setInt(2, idEquipeA);
        st.setInt(3, idEquipeB);

        return st;
    }
}

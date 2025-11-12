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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author elio
 */
public class Matchs extends ClasseMiroir {
    private int ronde;
    private int idEquipeA;
    private int idEquipeB;

    public Matchs(int ronde, int idEquipeA, int idEquipeB) {
        this.ronde = ronde;
        this.idEquipeA = idEquipeA;
        this.idEquipeB = idEquipeB;
    }

    public Matchs(int id, int ronde, int idEquipeA, int idEquipeB) {
        super(id);
        this.ronde = ronde;
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
    
    private static List<Matchs> fromResultSetToList(ResultSet list) throws SQLException {
        List<Matchs> res = new ArrayList<>();
        while (list.next()) {
            res.add(new Matchs(list.getInt("ronde"), list.getInt("idEquipeA"), list.getInt("idEquipeB")));
        }
        return res; 
    }
    
    public static List<Matchs> tousLesMatchs(Connection con) throws SQLException {
        List<Matchs> res = new ArrayList<>();
        try (PreparedStatement pst = con.prepareStatement("select ronde,idEquipeA,idEquipeB from matchs")) {
            try (ResultSet allU = pst.executeQuery()) {
                return fromResultSetToList(allU);
            }
        }
    }
    
    public static Optional<Matchs> findById(Connection con, int id) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement("select ronde,idEquipeA,idEquipeB from score where id=?")) {
            pst.setInt(1, id);
            ResultSet res = pst.executeQuery();

            if (res.next()) {
                int score = res.getInt(2);
                int idEquipeA = res.getInt(3);
                int idEquipeB = res.getInt(4);
                return Optional.of(new Matchs(id, score, idEquipeA, idEquipeB));
            } else {
                return Optional.empty();
            }
        }
    }
    
}

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
public class Score extends ClasseMiroir {
    private int score;
    private int idEquipe;
    private int idMatch;

    public Score(int score, int idEquipe, int idMatch) {
        this.score = score;
        this.idEquipe = idEquipe;
        this.idMatch = idMatch;
    }

    public Score(int id, int score, int idEquipe, int idMatch) {
        super(id);
        this.score = score;
        this.idEquipe = idEquipe;
        this.idMatch = idMatch;
    }
    
    public Score(){
        this.score = 0;
        this.idEquipe = 0;
        this.idMatch = 0;
    }

    public Score(int id) {
        super(id);
        this.score = 0;
        this.idMatch = 0;
        this.idEquipe = 0;
    }
    
    public int getScore() {
        return score;
    }

    public int getIdEquipe() {
        return idEquipe;
    }

    public int getIdMatch() {
        return idMatch;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setIdEquipe(int idEquipe) {
        this.idEquipe = idEquipe;
    }

    public void setIdMatch(int idMatch) {
        this.idMatch = idMatch;
    }
    
    public void deleteFromDB(Connection con) throws EntiteNonSauvegardee, SQLException {
        if (super.getId() == -1) {
            throw new EntiteNonSauvegardee();
        } else {
            var st = con.prepareStatement("delete from score where id = ?");
            st.setInt(1, super.getId());

            st.executeUpdate();
        }
    }
    
    @Override
    protected Statement saveSansId(Connection con) throws SQLException {
        var st = con.prepareStatement("insert into score (score, idEquipe, idMatch) values (?, ?, ?)");
        st.setInt(1, score);
        st.setInt(2, idEquipe);
        st.setInt(3, idMatch);

        return st;
    }
    
    public int updateOrNew(Connection con) throws SQLException {
        try {
            this.update(con);
            return -3;
        } catch (EntiteNonSauvegardee e) {
            return this.saveInDB(con);
        }
    }
    
    public void update(Connection con) throws SQLException, EntiteNonSauvegardee {
        if (super.getId() == -1) {
            throw new EntiteNonSauvegardee();
        }

        var st = con.prepareStatement("update score set score = ?, idMatch = ?, idEquipe=?, where id = ?");
        st.setInt(1, score);
        st.setInt(2, idMatch);
        st.setInt(3, idEquipe);
        st.setInt(4, super.getId());

        st.executeUpdate();
    }
    
        private static List<Score> fromResultSetToList(ResultSet list) throws SQLException {
        List<Score> res = new ArrayList<>();
        while (list.next()) {
            res.add(new Score(list.getInt("score"), list.getInt("idEquipe"), list.getInt("idMatch")));
        }
        return res; 
    }
        
        public static List<Score> tousLesScores(Connection con) throws SQLException {
        List<Score> res = new ArrayList<>();
        try (PreparedStatement pst = con.prepareStatement("select score,idEquipe,idMatch from score")) {
            try (ResultSet allU = pst.executeQuery()) {
                return fromResultSetToList(allU);
            }
        }
    }
        
        public static Optional<Score> findById(Connection con, int id) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement("select score,idEquipe,idMatch from score where id=?")) {
            pst.setInt(1, id);
            ResultSet res = pst.executeQuery();

            if (res.next()) {
                int score = res.getInt(2);
                int idEquipe = res.getInt(3);
                int idMatch = res.getInt(4);
                return Optional.of(new Score(id, score, idEquipe, idMatch));
            } else {
                return Optional.empty();
            }
        }
    }
        
}

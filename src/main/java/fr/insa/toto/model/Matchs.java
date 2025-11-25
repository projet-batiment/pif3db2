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
import fr.insa.beuvron.utils.database.ConnectionPool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 *
 * @author elio
 */
public class Matchs extends ClasseMiroir {
    private int ronde;

    private ModifiedState state;

    private int idEquipeA;
    private int idEquipeB;
    private String nomA;
    private String nomB;
    private int scoreA;
    private int scoreB;

    public Matchs() {
        this.ronde = -1;

        this.state = ModifiedState.CREATED;
    }

    public Matchs(int ronde) {
        this.ronde = ronde;

        this.state = ModifiedState.CREATED;
    }

    public Matchs(int id, int ronde) {
        super(id);
        this.ronde = ronde;

        this.state = id >= 0 ? ModifiedState.NORMAL : ModifiedState.PORCELAINE;
    }

    private class ScoreEquipe {
        public final Score score;
        public final Equipe equipe;

        public ScoreEquipe(Score score, Equipe equipe) {
            this.score = score;
            this.equipe = equipe;
        }
    }

    private List<ScoreEquipe> retreiveScoreEquipe(Connection con) throws IndexOutOfBoundsException, SQLException, EntiteNonSauvegardee {
        var scores = retreiveScore(con);

        List<ScoreEquipe> list = new ArrayList<>();

        for (Score each: scores) {
            var e = Equipe.findById(con, each.getIdEquipe());

            if (e.isPresent()) {
                list.add(new ScoreEquipe(each, e.get()));
            } else {
                throw new EntiteNonSauvegardee();
            }
        }

        return list;
    }

    private List<Score> retreiveScore(Connection con) throws IndexOutOfBoundsException, SQLException, EntiteNonSauvegardee {
        switch (state) {
            case CREATED, PORCELAINE -> throw new EntiteNonSauvegardee();
        }

        var scores = Score.findByMatch(con, super.getId());
        int length = scores.size();

        switch (length) {
            case 2 -> {
                return scores;
            }
            case 0 -> {
                scores.add(new Score());
                scores.add(new Score());
                return scores;
            }
            default -> throw new IndexOutOfBoundsException("1 match should have only 2 scores, retrieved " + length);
        }
    }

    public void populate() throws SQLException, NoSuchElementException {
        this.populate(ConnectionPool.getConnection());
    }
    
    public void populate(Connection con) throws SQLException, NoSuchElementException, IndexOutOfBoundsException {
        if (this.state != ModifiedState.POPULATED) {
            var list = retreiveScoreEquipe(con);

            this.scoreA = list.getFirst().score.getScore();
            this.scoreB = list.getLast().score.getScore();
            this.nomA = list.getFirst().equipe.getNom();
            this.nomB = list.getLast().equipe.getNom();

            this.state = ModifiedState.POPULATED;
        }
    }

    public ModifiedState getState() {
        return state;
    }

    public String getNom() {
        return nomA + " vs " + nomB;
    }

    public String getNomA() {
        return nomA;
    }

    public String getNomB() {
        return nomB;
    }

    public Integer getScoreA() {
        return scoreA;
    }

    public Integer getScoreB() {
        return scoreB;
    }

    public void setScoreA(Integer scoreA) {
        this.state = ModifiedState.DEPTH_EDITED;
        this.scoreA = scoreA;
    }

    public void setScoreB(Integer scoreB) {
        this.state = ModifiedState.DEPTH_EDITED;
        this.scoreB = scoreB;
    }

    public int getIdEquipeA() {
        return idEquipeA;
    }

    public void setIdEquipeA(int idEquipeA) {
        this.state = ModifiedState.DEPTH_EDITED;
        this.idEquipeA = idEquipeA;
    }

    public int getIdEquipeB() {
        return idEquipeB;
    }

    public void setIdEquipeB(int idEquipeB) {
        this.state = ModifiedState.DEPTH_EDITED;
        this.idEquipeB = idEquipeB;
    }

    public int getRonde() {
        return ronde;
    }

    public void setRonde(int taillecm) {
        this.state = ModifiedState.EDITED;
        this.ronde = taillecm;
    }

    public void deleteFromDB(Connection con) throws EntiteNonSauvegardee, SQLException {
        if (super.getId() == -1) {
            throw new EntiteNonSauvegardee();
        } else {
            var st = con.prepareStatement("delete from matchs where id = ?");
            st.setInt(1, super.getId());

            st.executeUpdate();
        }
    }
    
    @Override
    protected Statement saveSansId(Connection con) throws SQLException {
        var st = con.prepareStatement("insert into matchs (ronde) values (?)");
        st.setInt(1, ronde);

        return st;
    }

    public void update(Connection con) throws SQLException, EntiteNonSauvegardee, IndexOutOfBoundsException {
        switch (this.state) {
            case CREATED, PORCELAINE -> throw new EntiteNonSauvegardee();
            case NORMAL, POPULATED -> {}
            case EDITED -> {
                var st = con.prepareStatement("update matchs set ronde = ? where id = ?");
                st.setInt(1, ronde);
                st.setInt(2, super.getId());
            }
            case DEPTH_EDITED -> {
                var list = retreiveScore(con);

                var sa = list.getFirst();
                sa.setScore(this.scoreA);
                sa.setIdEquipe(this.idEquipeA);

                var sb = list.getLast();
                sb.setScore(this.scoreA);
                sb.setIdEquipe(this.idEquipeA);

                for (var each: list) {
                    each.update(con);
                }
            }
        }
    }
    
    private static List<Matchs> fromResultSetToList(ResultSet list) throws SQLException {
        List<Matchs> res = new ArrayList<>();
        while (list.next()) {
            res.add(new Matchs(list.getInt("ronde")));
        }
        return res; 
    }
    
    public static List<Matchs> tousLesMatchs(Connection con) throws SQLException {
        List<Matchs> res = new ArrayList<>();
        try (PreparedStatement pst = con.prepareStatement("select ronde from matchs")) {
            try (ResultSet allU = pst.executeQuery()) {
                return fromResultSetToList(allU);
            }
        }
    }
    
    public static Optional<Matchs> findById(Connection con, int id) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement("select ronde from score where id=?")) {
            pst.setInt(1, id);
            ResultSet res = pst.executeQuery();

            if (res.next()) {
                int score = res.getInt(2);
                int idEquipeA = res.getInt(3);
                int idEquipeB = res.getInt(4);
                return Optional.of(new Matchs(id, score));
            } else {
                return Optional.empty();
            }
        }
    }
}

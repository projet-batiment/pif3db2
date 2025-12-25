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
package fr.insa.toto.webui.equipe;

import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Equipe;
import fr.insa.toto.webui.utils.NotificationError;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author qleveque01
 */
public class EquipeStats implements Serializable {

    private final int nombreDeMatchs;
    private final int victoires;
    private final int defaites;
    private final int nuls;
    private final int butsInscrits;
    private final int butsEncaisses;
    private final int points; // Nouvelle donnée

    // Requête mise à jour avec calcul des points : (Victoires * 3) + (Nuls * 1)
    private static final String SQL_GET_STATS_BY_EQUIPE_ID = """
        SELECT
            COUNT(M.id) AS Nombre_de_matchs,
            SUM(CASE WHEN S1.score > S2.score THEN 1 ELSE 0 END) AS Victoires,
            SUM(CASE WHEN S1.score < S2.score THEN 1 ELSE 0 END) AS Defaites,
            SUM(CASE WHEN S1.score = S2.score THEN 1 ELSE 0 END) AS Nuls,
            SUM(S1.score) AS Buts_Inscrits,
            SUM(S2.score) AS Buts_Encaisses,
            SUM(CASE 
                WHEN S1.score > S2.score THEN 3 
                WHEN S1.score = S2.score THEN 2 
                ELSE 1 
            END) AS Points
        FROM
            matchs M
        JOIN
            score S1 ON M.id = S1.idMatch AND S1.idEquipe = ?
        JOIN
            score S2 ON M.id = S2.idMatch AND S2.idEquipe <> ?
        WHERE
            S1.idEquipe = ?
    """;
    
    private static final String SQL_CLASSEMENT_GLOBAL = """
    SELECT 
        E.id,
        RANK() OVER (ORDER BY 
            SUM(CASE WHEN S1.score > S2.score THEN 3 WHEN S1.score = S2.score THEN 1 ELSE 0 END) DESC, 
            (SUM(S1.score) - SUM(S2.score)) DESC
        ) AS rang
    FROM equipe E
    LEFT JOIN score S1 ON E.id = S1.idEquipe
    LEFT JOIN score S2 ON S1.idMatch = S2.idMatch AND S1.idEquipe <> S2.idEquipe
    GROUP BY E.id
""";
    
    public static List<Equipe> getTop3(Connection con, Optional<Integer> tournoisId) throws SQLException {
        List<Equipe> equipes = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder("""
            SELECT e.id, e.nom 
            FROM equipe e 
            JOIN score s1 ON e.id = s1.idEquipe 
            JOIN matchs m ON s1.idMatch = m.id 
            """);

        if (tournoisId.isPresent()) {
            sql.append("JOIN ronde r ON m.idRonde = r.id ");
        }

        sql.append("JOIN score s2 ON m.id = s2.idMatch AND s1.idEquipe <> s2.idEquipe ");
        
        if (tournoisId.isPresent()) {
            sql.append("WHERE r.idTournois = ? "); 
        }
        
        sql.append("""
            GROUP BY e.id, e.nom 
            ORDER BY SUM(CASE 
                WHEN s1.score > s2.score THEN 3 
                WHEN s1.score = s2.score THEN 2 
                ELSE 1 END) DESC 
            LIMIT 3
            """);

        try (PreparedStatement pst = con.prepareStatement(sql.toString())) {
            if (tournoisId.isPresent()) {
                pst.setInt(1, tournoisId.get());
            }
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    equipes.add(new Equipe(rs.getInt("id"), rs.getString("nom")));
                }
            }
        }
        return equipes;
    }

    public static int getRangEquipe(int equipeId) {
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement pst = con.prepareStatement(SQL_CLASSEMENT_GLOBAL)) {
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    if (rs.getInt("id") == equipeId) {
                        return rs.getInt("rang");
                    }
                }
            }
        } catch (SQLException ex) {
            NotificationError.sql(ex);
        }
        return 0;
    }

    public EquipeStats(int nbMatchs, int wins, int losses, int draws, int goalsFor, int goalsAgainst, int points) {
        this.nombreDeMatchs = nbMatchs;
        this.victoires = wins;
        this.defaites = losses;
        this.nuls = draws;
        this.butsInscrits = goalsFor;
        this.butsEncaisses = goalsAgainst;
        this.points = points;
    }

    // Getters
    public int getNombreDeMatchs() { return nombreDeMatchs; }
    public int getVictoires() { return victoires; }
    public int getDefaites() { return defaites; }
    public int getNuls() { return nuls; }
    public int getButsInscrits() { return butsInscrits; }
    public int getButsEncaisses() { return butsEncaisses; }
    public int getPoints() { return points; }
    
    public int getDifferenceDeButs() {
        return butsInscrits - butsEncaisses;
    }

   
    public static Optional<EquipeStats> findById(int equipeId) {
        List<EquipeStats> list = findStatsForGrid(equipeId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public static List<EquipeStats> findStatsForGrid(int equipeId) {
        Optional<EquipeStats> statsOpt = Optional.empty();

        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement pst = con.prepareStatement(SQL_GET_STATS_BY_EQUIPE_ID)) {

            pst.setInt(1, equipeId);
            pst.setInt(2, equipeId);
            pst.setInt(3, equipeId);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    int nbMatchs = rs.getInt("Nombre_de_matchs");
                    
                    if (nbMatchs == 0) {
                        statsOpt = Optional.of(new EquipeStats(0, 0, 0, 0, 0, 0, 0));
                    } else {
                        statsOpt = Optional.of(new EquipeStats(
                            nbMatchs,
                            rs.getInt("Victoires"),
                            rs.getInt("Defaites"),
                            rs.getInt("Nuls"),
                            rs.getInt("Buts_Inscrits"),
                            rs.getInt("Buts_Encaisses"),
                            rs.getInt("Points")
                        ));
                    }
                }
            }
        } catch (SQLException ex) {
            NotificationError.sql(ex);
        }
        
        return statsOpt.map(List::of).orElse(Collections.emptyList());
    }
}
   

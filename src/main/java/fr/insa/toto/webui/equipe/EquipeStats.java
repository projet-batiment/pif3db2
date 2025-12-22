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
import fr.insa.toto.webui.utils.NotificationError;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author qleveque01
 */
public class EquipeStats {

    private final int nombreDeMatchs;
    private final int victoires;
    private final int defaites;
    private final int nuls;
    private final int butsInscrits;
    private final int butsEncaisses;
    
    private static final String SQL_GET_STATS_BY_EQUIPE_ID = """
        SELECT
            COUNT(M.id) AS Nombre_de_matchs,
            SUM(CASE WHEN S1.score > S2.score THEN 1 ELSE 0 END) AS Victoires,
            SUM(CASE WHEN S1.score < S2.score THEN 1 ELSE 0 END) AS Defaites,
            SUM(CASE WHEN S1.score = S2.score THEN 1 ELSE 0 END) AS Nuls,
            SUM(S1.score) AS Buts_Inscrits,
            SUM(S2.score) AS Buts_Encaisses
        FROM
            matchs M
        JOIN
            score S1 ON M.id = S1.idMatch AND S1.idEquipe = ?
        JOIN
            score S2 ON M.id = S2.idMatch AND S2.idEquipe <> ?
        WHERE
            S1.idEquipe = ?
    """;

    public EquipeStats(int nbMatchs, int wins, int losses, int draws, int goalsFor, int goalsAgainst) {
        this.nombreDeMatchs = nbMatchs;
        this.victoires = wins;
        this.defaites = losses;
        this.nuls = draws;
        this.butsInscrits = goalsFor;
        this.butsEncaisses = goalsAgainst;
    }


    public int getNombreDeMatchs() { return nombreDeMatchs; }
    public int getVictoires() { return victoires; }
    public int getDefaites() { return defaites; }
    public int getNuls() { return nuls; }
    public int getButsInscrits() { return butsInscrits; }
    public int getButsEncaisses() { return butsEncaisses; }
    
    public int getDifferenceDeButs() {
        return butsInscrits - butsEncaisses;
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
                        statsOpt = Optional.of(new EquipeStats(0, 0, 0, 0, 0, 0));
                    } else {
                        int victoires = rs.getInt("Victoires");
                        int defaites = rs.getInt("Defaites");
                        int nuls = rs.getInt("Nuls");
                        int butsInscrits = rs.getInt("Buts_Inscrits");
                        int butsEncaisses = rs.getInt("Buts_Encaisses");

                        statsOpt = Optional.of(new EquipeStats(
                            nbMatchs, victoires, defaites, nuls, butsInscrits, butsEncaisses
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
   

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
    private final int points;

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
    
    // Requête modifiée pour calculer le rang au sein d'un tournoi spécifique
    private static final String SQL_CLASSEMENT_PAR_TOURNOI = """
    SELECT 
        E.id,
        RANK() OVER (ORDER BY 
            SUM(CASE WHEN S1.score > S2.score THEN 3 WHEN S1.score = S2.score THEN 2 ELSE 1 END) DESC, 
            (SUM(S1.score) - SUM(S2.score)) DESC
        ) AS rang
    FROM equipe E
    JOIN score S1 ON E.id = S1.idEquipe
    JOIN matchs M ON S1.idMatch = M.id
    JOIN ronde R ON M.idRonde = R.id
    JOIN score S2 ON S1.idMatch = S2.idMatch AND S1.idEquipe <> S2.idEquipe
    WHERE R.idTournois = ?
    GROUP BY E.id
""";
    
    /**
     * Récupère le Top 3 des équipes pour un tournoi spécifique.
     */
    public static List<Equipe> getTop3(Connection con, int tournoisId) throws SQLException {
        List<Equipe> equipes = new ArrayList<>();
        
        // On force la jointure avec 'ronde' pour filtrer par tournoi
        StringBuilder sql = new StringBuilder("""
            SELECT e.id, e.nom, e.idTournois 
            FROM equipe e 
            JOIN score s1 ON e.id = s1.idEquipe 
            JOIN matchs m ON s1.idMatch = m.id 
            JOIN ronde r ON m.idRonde = r.id
            JOIN score s2 ON m.id = s2.idMatch AND s1.idEquipe <> s2.idEquipe 
            """);
            sql.append("WHERE r.idTournois = ? "); 

        
        sql.append("""
            GROUP BY e.id, e.nom, e.idTournois 
            ORDER BY SUM(CASE 
                WHEN s1.score > s2.score THEN 3 
                WHEN s1.score = s2.score THEN 2 
                ELSE 1 END) DESC,
                (SUM(s1.score) - SUM(s2.score)) DESC
            LIMIT 3
            """);

        try (PreparedStatement pst = con.prepareStatement(sql.toString())) {
            pst.setInt(1, tournoisId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    equipes.add(new Equipe(rs.getInt("id"), rs.getString("nom"), rs.getInt("idTournois")));
                }
            }
        }
        return equipes;
    }

    /**
     * Récupère le rang d'une équipe au sein d'un tournoi spécifique.
     */
    public static int getRangEquipe(int equipeId, int tournoiId) {
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement pst = con.prepareStatement(SQL_CLASSEMENT_PAR_TOURNOI)) {
            
            pst.setInt(1, tournoiId);
            
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
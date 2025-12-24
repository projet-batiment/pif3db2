package fr.insa.toto.webui.joueur;

import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.webui.utils.NotificationError;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JoueurStats implements Serializable {

    private final String nomEquipe;
    private final int nombreDeMatchs;
    private final int victoires;
    private final int defaites;
    private final int nuls;
    private final int butsInscrits;
    private final int butsEncaisses;

private static final String SQL_GET_ALL_STATS = """
        SELECT 
            E.nom AS equipe_nom,
            COUNT(DISTINCT S1.idMatch) AS nb_matchs,
            SUM(CASE WHEN S1.score > S2.score THEN 1 ELSE 0 END) AS victoires,
            SUM(CASE WHEN S1.score < S2.score THEN 1 ELSE 0 END) AS defaites,
            SUM(CASE WHEN S1.score = S2.score THEN 1 ELSE 0 END) AS nuls,
            SUM(S1.score) AS buts_inscrits,
            SUM(S2.score) AS buts_encaisses
        FROM composition C
        JOIN equipe E ON E.id = C.idEquipe
        JOIN score S1 ON S1.idEquipe = C.idEquipe
        JOIN score S2 ON S2.idMatch = S1.idMatch AND S2.idEquipe <> S1.idEquipe
        WHERE C.idJoueur = ?
        GROUP BY E.nom
        
        UNION ALL
        
        SELECT 
            ' TOTAL CUMULÉ' AS equipe_nom,
            COUNT(DISTINCT S1.idMatch),
            SUM(CASE WHEN S1.score > S2.score THEN 1 ELSE 0 END),
            SUM(CASE WHEN S1.score < S2.score THEN 1 ELSE 0 END),
            SUM(CASE WHEN S1.score = S2.score THEN 1 ELSE 0 END),
            SUM(S1.score),
            SUM(S2.score)
        FROM composition C
        JOIN score S1 ON S1.idEquipe = C.idEquipe
        JOIN score S2 ON S2.idMatch = S1.idMatch AND S2.idEquipe <> S1.idEquipe
        WHERE C.idJoueur = ?
        ORDER BY equipe_nom ASC
    """;

    public JoueurStats(String nomEquipe, int nbM, int v, int d, int n, int bi, int be) {
        this.nomEquipe = nomEquipe;
        this.nombreDeMatchs = nbM;
        this.victoires = v;
        this.defaites = d;
        this.nuls = n;
        this.butsInscrits = bi;
        this.butsEncaisses = be;
    }

    public String getNomEquipe() { return nomEquipe; }
    public int getNombreDeMatchs() { return nombreDeMatchs; }
    public int getVictoires() { return victoires; }
    public int getDefaites() { return defaites; }
    public int getNuls() { return nuls; }
    public int getButsInscrits() { return butsInscrits; }
    public int getButsEncaisses() { return butsEncaisses; }
    public int getDifferenceDeButs() { return butsInscrits - butsEncaisses; }

    public static List<JoueurStats> findStatsForGrid(int joueurId) {
        List<JoueurStats> list = new ArrayList<>();
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement pst = con.prepareStatement(SQL_GET_ALL_STATS)) {
            pst.setInt(1, joueurId);
            pst.setInt(2, joueurId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(new JoueurStats(
                        rs.getString("equipe_nom"),
                        rs.getInt("nb_matchs"),
                        rs.getInt("victoires"),
                        rs.getInt("defaites"),
                        rs.getInt("nuls"),
                        rs.getInt("buts_inscrits"),
                        rs.getInt("buts_encaisses")
                    ));
                }
            }
        } catch (SQLException ex) {
            NotificationError.sql(ex);
        }
        return list;
    }
}
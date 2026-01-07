package fr.insa.toto.webui.joueur;

import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.webui.utils.NotificationError;
import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JoueurStats implements Serializable {
    private final String nomEquipe;
    private String nomTournoi; 
    private final int nombreDeMatchs, victoires, defaites, nuls, butsInscrits, butsEncaisses;

    public JoueurStats(String nomEquipe, int nbMatchs, int wins, int losses, int draws, int goalsFor, int goalsAgainst) {
        this.nomEquipe = nomEquipe;
        this.nombreDeMatchs = nbMatchs;
        this.victoires = wins;
        this.defaites = losses;
        this.nuls = draws;
        this.butsInscrits = goalsFor;
        this.butsEncaisses = goalsAgainst;
    }

    // Getters pour la Grid
    public String getNomEquipe() { return nomEquipe; }
    public String getNomTournoi() { return nomTournoi; }
    public int getNombreDeMatchs() { return nombreDeMatchs; }
    public int getVictoires() { return victoires; }
    public int getDefaites() { return defaites; }
    public int getNuls() { return nuls; }
    public int getButsInscrits() { return butsInscrits; }
    public int getButsEncaisses() { return butsEncaisses; }
    public int getDifferenceDeButs() { return butsInscrits - butsEncaisses; }

    /**
     * Grilles par Tournoi
     */
    public static List<JoueurStats> findStatsDetaillees(int joueurId) {
        List<JoueurStats> list = new ArrayList<>();
        // On lie le joueur à l'équipe (Composition), puis l'équipe au score (Match)
        String sql = """
SELECT e.nom as Equipe, t.nom as Tournoi,
       COUNT(m.id) AS NB,
       sum(case when s1.score > s2.score then 1 else 0 end) as V,
       sum(case when s1.score < s2.score then 1 else 0 end) as D,
       sum(case when s1.score = s2.score then 1 else 0 end) as N,
       sum(s1.score) as BI,
        sum(s2.score) as BE
FROM joueur j
JOIN composition c on c.idJoueur = j.id
JOIN equipe e on e.id = c.idEquipe
JOIN tournoi t on t.id = e.idTournois
JOIN score s1 on s1.idEquipe = e.id
JOIN matchs m on m.id = s1.idMatch
JOIN score s2 on s2.idMatch = m.id AND s2.idEquipe <> e.id
WHERE j.id = ?
GROUP BY e.id
        """;
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, joueurId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                JoueurStats s = new JoueurStats(rs.getString("Equipe"), rs.getInt("NB"), rs.getInt("V"),
                                                rs.getInt("D"), rs.getInt("N"), rs.getInt("BI"), rs.getInt("BE"));
                s.nomTournoi = rs.getString("Tournoi");
                list.add(s);
            }
        } catch (SQLException ex) { NotificationError.sql(ex); }
        return list;
    }

    /**
     * Grille Totale : On garde ton récapitulatif global tel quel
     */
    public static List<JoueurStats> findStatsForGrid(int joueurId) {
        List<JoueurStats> list = new ArrayList<>();
        String sql = """
            SELECT E.nom AS Equipe, COUNT(M.id) AS NB,
                   SUM(CASE WHEN S1.score > S2.score THEN 1 ELSE 0 END) AS V,
                   SUM(CASE WHEN S1.score < S2.score THEN 1 ELSE 0 END) AS D,
                   SUM(CASE WHEN S1.score = S2.score THEN 1 ELSE 0 END) AS N,
                   SUM(S1.score) AS BI, SUM(S2.score) AS BE
            FROM composition C
            JOIN equipe E ON C.idEquipe = E.id
            JOIN score S1 ON E.id = S1.idEquipe
            JOIN matchs M ON S1.idMatch = M.id
            JOIN score S2 ON M.id = S2.idMatch AND S2.idEquipe <> S1.idEquipe
            WHERE C.idJoueur = ?
            GROUP BY E.id, E.nom
        """;
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, joueurId);
            ResultSet rs = pst.executeQuery();
            int tM=0, tV=0, tD=0, tN=0, tBI=0, tBE=0;
            while (rs.next()) {
                JoueurStats s = new JoueurStats(rs.getString("Equipe"), rs.getInt("NB"), rs.getInt("V"),
                                                rs.getInt("D"), rs.getInt("N"), rs.getInt("BI"), rs.getInt("BE"));
                list.add(s);
                tM+=s.nombreDeMatchs; tV+=s.victoires; tD+=s.defaites; tN+=s.nuls; tBI+=s.butsInscrits; tBE+=s.butsEncaisses;
            }
            if (!list.isEmpty()) {
                list.add(new JoueurStats(" TOTAL CUMULÉ", tM, tV, tD, tN, tBI, tBE));
            }
        } catch (SQLException ex) { NotificationError.sql(ex); }
        return list;
    }
}
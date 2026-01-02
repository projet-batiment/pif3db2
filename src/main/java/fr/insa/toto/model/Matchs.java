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

import com.vaadin.flow.component.littemplate.IllegalAttributeException;
import fr.insa.toto.model.utils.Named;
import fr.insa.toto.model.utils.ModifiedState;
import fr.insa.beuvron.utils.database.ClasseMiroir;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.utils.ChildFace;
import fr.insa.toto.model.utils.ParentFace;
import fr.insa.toto.webui.utils.NotificationError;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author elio
 */
public class Matchs extends ClasseMiroir implements Named {
    public final static Matchs PORCELAINE = new Matchs(ClasseMiroir.ID_PORCELAINE, 0);

    private static final String nomTable = "matchs";
    protected final String nomTable() {
        return this.nomTable;
    }

    private ModifiedState state;

    private Ronde ronde;
    private int idRonde;

    private ScoreEquipe seA = new ScoreEquipe();
    private ScoreEquipe seB = new ScoreEquipe();

    public Matchs() {
        this.idRonde = ClasseMiroir.ID_UNSAVED;

        this.state = ModifiedState.CREATED;
    }

    public Matchs(int idRonde) {
        this.idRonde = idRonde;

        this.state = ModifiedState.CREATED;
    }

    public Matchs(int id, int idRonde) {
        super(id);
        this.idRonde = idRonde;

        this.state = id >= 0 ? ModifiedState.NORMAL : ModifiedState.PORCELAINE;
    }

    public class ScoreEquipe {
        public final Score score;
        public Equipe equipe;

        public ScoreEquipe(Score score, Equipe equipe) {
            this.score = score;
            this.equipe = equipe;
        }

        public ScoreEquipe() {
            this.equipe = null;
            this.score = new Score();
            score.setIdMatch(getId());
        }
    }

    public static class AsChild extends ChildFace {
        @Override
        public String typeName() {
            return "match";
        }

        @Override
        protected String leChildPrefix() {
            return "le ";
        }

        @Override
        protected String duChildPrefix() {
            return "du ";
        }
    }

    public static final MatchsParent matchs = new MatchsParent();
    private static class MatchsParent extends ParentFace<Matchs> {
        @Override
        public String parentObjectName() {
            return "";
        }

        @Override
        public String parentTypeName() {
            return "";
        }

        @Override
        public String le() {
            return "";
        }

        @Override
        public String du() {
            return "";
        }

        @Override
        public int add(Matchs match, Connection con) throws SQLException, EntiteDejaSauvegardee {
            return match.getId();
        }

        @Override
        public void remove(Matchs match, Connection con) throws SQLException, EntiteNonSauvegardee {
            match.deleteFromDB(con);
        }

        @Override
        public List<Matchs> get(Connection con) throws SQLException {
            var list = Matchs.tousLesMatchs(con);
            for (var each: list)
                each.populate(con);
            return list;
        }

        public MatchsParent() {
            super(new Matchs.AsChild());
        }
    }

    private List<ScoreEquipe> retreiveScoreEquipe(Connection con) throws IndexOutOfBoundsException, SQLException, EntiteNonSauvegardee, NoSuchElementException {
        var scores = retreiveScore(con);

        List<ScoreEquipe> list = new ArrayList<>();

        for (Score each: scores) {
            var e = Equipe.findById(con, each.getIdEquipe());

            if (e.isPresent()) {
                list.add(new ScoreEquipe(each, e.get()));
            } else {
                throw new NoSuchElementException("Equipe " + each.getIdEquipe() + " for Score " + each.getId() + " not found for Matchs " + this.getId());
            }
        }

        return list;
    }

    private List<Score> retreiveScore(Connection con) throws IndexOutOfBoundsException, SQLException, EntiteNonSauvegardee {
        switch (state) {
            case CREATED, PORCELAINE -> {
                throw new EntiteNonSauvegardee();
            }
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
            default -> throw new IndexOutOfBoundsException("1 match should have exactly either 2 or 0 scores, retrieved " + length);
        }
    }

    private Ronde retreiveRonde(Connection con) throws SQLException, NoSuchElementException {
        return Ronde.findById(con, idRonde).get();
    }

    public void populate(Connection con) throws SQLException, NoSuchElementException, IndexOutOfBoundsException {
        switch (this.state) {
            case POPULATED, PORCELAINE -> {}
            default -> {
                var list = retreiveScoreEquipe(con);

                this.seA = list.get(0);
                this.seB = list.get(1);

                this.ronde = this.retreiveRonde(con);

                this.state = ModifiedState.POPULATED;
            }
        }
    }

    public ModifiedState getState() {
        return state;
    }

    public String getName() {
        return this.seA.equipe.getNom() + " vs " + this.seB.equipe.getNom();
    }

    public ScoreEquipe getScoreEquipeA() {
        return seA;
    }

    public ScoreEquipe getScoreEquipeB() {
        return seB;
    }

    public Ronde getRonde() {
        return ronde;
    }

    public Ronde getIdRonde() {
        return ronde;
    }

    // does NOT retreive the updated Ronde
    public void setIdRonde(int idRonde) {
        this.state = ModifiedState.EDITED;
        this.idRonde = idRonde;
    }

    public boolean ofEquipe(Equipe equipe) {
        return this.seA.equipe.equals(equipe) || this.seB.equipe.equals(equipe);
    }

    @Override
    public void deleteChildren(Connection con) throws SQLException {
        this.seA.score.deleteFromDB(con);
        this.seB.score.deleteFromDB(con);
    }

    public void checkSavable(Connection con) throws SQLException, NoSuchElementException, IllegalAttributeException {
        if (this.seA.equipe.equals(this.seB.equipe))
            throw new IllegalAttributeException("Les deux équipes du match sont identiques : " + this.seA.equipe.getName());

        Ronde ronde = Ronde.findById(con, this.idRonde).get();
        int nombreMatchsParalleles = Tournois.findById(con, ronde.getIdTournois()).get().getNombreTerrains();
        int nombreMatchsAutres = findByIdRonde(con, this.idRonde)
                .stream()
                .filter(each -> each.getId() != this.getId())
                .collect(Collectors.toList())
                .size();

        NotificationError.log(nombreMatchsParalleles + " // " + nombreMatchsAutres);
        if (nombreMatchsParalleles <= nombreMatchsAutres)
            throw new IllegalAttributeException("La ronde " + ronde.getName() + " est remplie : le match " + this.getName() + " ne peut pas être ajouté.");
    }

    @Override
    protected Statement saveSansId(Connection con) throws SQLException {
        this.checkSavable(con);

        var st = con.prepareStatement("insert into matchs (idRonde) values (?)",
                PreparedStatement.RETURN_GENERATED_KEYS);
        st.setInt(1, idRonde);
        
        st.executeUpdate();
        return st;
    }

    @Override
    protected void afterSavedInDB(Connection con) throws SQLException {
        this.state = ModifiedState.DEPTH_EDITED;
        this.seA.score.setIdMatch(this.getId());
        this.seB.score.setIdMatch(this.getId());

        this.update(con);
    }

    public void update(Connection con) throws SQLException, EntiteNonSauvegardee, IndexOutOfBoundsException {
        if (this.getId() == ClasseMiroir.ID_UNSAVED)
            throw new EntiteNonSauvegardee();

        switch (this.state) {
            case CREATED, PORCELAINE -> throw new EntiteNonSauvegardee();

            case EDITED, NORMAL, POPULATED, DEPTH_EDITED -> {
                this.checkSavable(con);

                var st = con.prepareStatement("update matchs set idRonde = ? where id = ?");
                st.setInt(1, idRonde);
                st.setInt(2, super.getId());
                st.executeUpdate();

                this.seA.score.setIdEquipe(this.seA.equipe.getId());
                this.seB.score.setIdEquipe(this.seB.equipe.getId());

                this.seA.equipe.updateOrNew(con);
                this.seB.equipe.updateOrNew(con);

                this.seA.score.updateOrNew(con);
                this.seB.score.updateOrNew(con);
            }

        }

        this.state = ModifiedState.NORMAL;
    }
    
    private static List<Matchs> fromResultSetToList(ResultSet list) throws SQLException {
        List<Matchs> res = new ArrayList<>();
        while (list.next()) {
            res.add(new Matchs(list.getInt("id"), list.getInt("idRonde")));
        }
        return res; 
    }
    
    public static List<Matchs> tousLesMatchs(Connection con) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement("select id, idRonde from matchs")) {
            try (ResultSet allU = pst.executeQuery()) {
                return fromResultSetToList(allU);
            }
        }
    }
    
    public static List<Matchs> findByIdRonde(Connection con, int idRonde) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement("select id, idRonde from matchs where idRonde=?")) {
            pst.setInt(1, idRonde);

            try (ResultSet allU = pst.executeQuery()) {
                return fromResultSetToList(allU);
            }
        }
    }

    public static List<Matchs> findByIdTournois(Connection con, int idTournois) throws SQLException {
        List<Ronde> rondes = Ronde.findByIdTournois(con, idTournois);
        List<Matchs> matchs = new ArrayList<>();

        for (Ronde ronde: rondes) {
            matchs.addAll(findByIdRonde(con, ronde.getId()));
        }

        return matchs;
    }

    public static Optional<Matchs> findById(Connection con, int id) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement("select idRonde from matchs where id=?")) {
            pst.setInt(1, id);
            ResultSet res = pst.executeQuery();

            if (res.next()) {
                int idRonde = res.getInt("idRonde");
                return Optional.of(new Matchs(id, idRonde));
            } else {
                return Optional.empty();
            }
        }
    }
}

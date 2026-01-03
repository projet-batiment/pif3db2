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

import fr.insa.toto.model.utils.Named;
import fr.insa.toto.model.utils.ParentFace;
import fr.insa.toto.model.utils.ModifiedState;
import com.vaadin.flow.component.notification.Notification;
import fr.insa.beuvron.utils.database.ClasseMiroir;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.utils.ChildFace;
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
import java.util.stream.Collector;

/**
 *
 * @author elio
 */
public class Equipe extends ClasseMiroir implements Named {
    private String nom;
    private ModifiedState state;
    private int idTournois;

    public final static Equipe PORCELAINE = new Equipe(Equipe.ID_PORCELAINE, "Nouveau...", ClasseMiroir.ID_UNSAVED);

    public static class AsChild extends ChildFace {
        @Override
        public String typeName() {
            return "équipe";
        }

        @Override
        protected String leChildPrefix() {
            return "l'";
        }

        @Override
        protected String duChildPrefix() {
            return "de l'";
        }
    }

    public static final EquipeParent equipes = new EquipeParent();
    private static class EquipeParent extends ParentFace<Equipe> {
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
        public int add(Equipe match, Connection con) throws SQLException, EntiteDejaSauvegardee {
            return match.getId();
        }

        @Override
        public void remove(Equipe match, Connection con) throws SQLException, EntiteNonSauvegardee {
            match.deleteFromDB(con);
        }

        @Override
        public List<Equipe> get(Connection con) throws SQLException {
            var list = Equipe.toutesLesEquipes(con);
            for (var each: list)
                each.populate(con);
            return list;
        }

        public EquipeParent() {
            super(new Equipe.AsChild());
        }
    }

    public final JoueurParent joueurs = new JoueurParent();
    private class JoueurParent extends ParentFace<Joueur> {
        @Override
        public String parentObjectName() {
            return getNom();
        }

        @Override
        public String parentTypeName() {
            return "équipe";
        }

        @Override
        public String le() {
            return "l'";
        }

        @Override
        public String du() {
            return "de l'";
        }

        @Override
        public int add(Joueur joueur, Connection con) throws SQLException, EntiteDejaSauvegardee {
            int idJoueur = joueur.getId();
            var composition = Composition.findByIdEquipeIdJoueur(con, getId(), idJoueur);

            if (composition.isPresent()) {
                throw new EntiteDejaSauvegardee();
            } else {
                int compositionId = new Composition(getId(), idJoueur).updateOrNew(con);
                state = ModifiedState.DEPTH_EDITED;
                populate(con);

                NotificationError.todo("Ajouter le nouveau joueur au tournois ?");

                return compositionId;
            }
        }

        @Override
        public void remove(Joueur joueur, Connection con) throws SQLException, EntiteNonSauvegardee {
            int idJoueur = joueur.getId();
            var composition = Composition.findByIdEquipeIdJoueur(con, getId(), idJoueur);

            if (composition.isPresent()) {
                composition.get().deleteFromDB(con);
                state = ModifiedState.DEPTH_EDITED;
                populate(con);
            } else {
                throw new EntiteNonSauvegardee();
            }
        }

        @Override
        public List<Joueur> get(Connection con) throws SQLException {
            return Composition
                    .findByIdEquipe(con, getId())
                    .stream()
                    .collect(Collector.of(
                            ArrayList::new, 
                            (out, each) -> {
                                try {
                                    Joueur.findById(con, each.getIdJoueur())
                                        .ifPresent(e -> out.add(e));
                                } catch (SQLException ex) {
                                    NotificationError.sql(ex);
                                }
                            },
                            (out, next) -> {
                                out.addAll(next);
                                return out;
                            },
                            Collector.Characteristics.UNORDERED)
                    );
        }

        public JoueurParent() {
            super(new Joueur.AsChild());
        }
    }

    public final MatchsParent matchss = new MatchsParent();
    private class MatchsParent extends ParentFace<Matchs> {
        @Override
        public String parentObjectName() {
            return getNom();
        }

        @Override
        public String parentTypeName() {
            return "équipe";
        }

        @Override
        public String le() {
            return "l'";
        }

        @Override
        public String du() {
            return "de l'";
        }

        @Override
        public int add(Matchs matchs, Connection con) throws SQLException, EntiteDejaSauvegardee, NoSuchElementException {
            if (matchs.ofEquipe(Equipe.this))
                return matchs.updateOrNew(con);
            else
                throw new NoSuchElementException("Le match n'est pas avec l'équipe " + Equipe.this.getNom());
        }

        @Override
        public void remove(Matchs matchs, Connection con) throws SQLException, EntiteNonSauvegardee, NoSuchElementException {
            if (matchs.ofEquipe(Equipe.this))
                matchs.deleteFromDB(con);
            else
                throw new NoSuchElementException("Le match n'est pas avec l'équipe " + Equipe.this.getNom());
        }

        @Override
        public List<Matchs> get(Connection con) throws SQLException {
            return Matchs
                    .tousLesMatchs(con)
                    .stream()
                    .collect(Collector.of(
                            ArrayList::new, 
                            (out, each) -> {
                                try {
                                    each.populate(con);

                                    if (each.ofEquipe(Equipe.this))
                                        out.add(each);
                                } catch (SQLException ex) {
                                    NotificationError.sql(ex);
                                }
                            },
                            (out, next) -> {
                                out.addAll(next);
                                return out;
                            },
                            Collector.Characteristics.UNORDERED)
                    );
        }

        public MatchsParent() {
            super(new Matchs.AsChild());
        }
    }

    private static final String nomTable = "equipe";
    protected final String nomTable() {
        return this.nomTable;
    }

    private Integer nbJoueurs = null;

    public Equipe(int idTournois) {
        this.nom = "";
        this.idTournois = idTournois;

        this.state = ModifiedState.CREATED;
    }

    public Equipe(int id, int idTournois) {
        super(id);
        this.nom = "";
        this.idTournois = idTournois;

        this.state = id >= 0 ? ModifiedState.NORMAL : ModifiedState.PORCELAINE;
    }

    public Equipe(String nom, int idTournois) {
        this.nom = nom;
        this.idTournois = idTournois;

        this.state = ModifiedState.CREATED;
    }

    public Equipe(int id, String nom, int idTournois) {
        super(id);
        this.nom = nom;
        this.idTournois = idTournois;

        this.state = id >= 0 ? ModifiedState.NORMAL : ModifiedState.PORCELAINE;
    }

    public ModifiedState getState() {
        return state;
    }

    public String getName() {
        return this.getNom();
    }
    
    public String getNom() {
        return nom;
    }
    
    public void setNom(String nom) {
        this.state = ModifiedState.EDITED;
        this.nom = nom;
    }

    public Integer getNbJoueurs() {
        return this.nbJoueurs;
    }

    public int getIdTournoi() {
        return idTournois;
    }

    public void populate(Connection con) throws SQLException {
        if (this.state != ModifiedState.POPULATED) {
            this.nbJoueurs = Composition.findByIdEquipe(con, super.getId()).size();

            this.state = ModifiedState.POPULATED;
        }
    }

    @Override
    protected Statement saveSansId(Connection con) throws SQLException {
        var st = con.prepareStatement("insert into equipe (nom, idTournois) values (?, ?)",
                PreparedStatement.RETURN_GENERATED_KEYS);
        st.setString(1, nom);
        st.setInt(2, idTournois);

        st.executeUpdate();
        return st;
    }

    @Override
    public void deleteChildren(Connection con) throws SQLException {
        for (Composition each: Composition.findByIdEquipe(con, super.getId())) {
            each.deleteFromDB(con);
        }
    }

    public void update(Connection con) throws SQLException, EntiteNonSauvegardee {
        if (super.getId() == -1) {
            throw new EntiteNonSauvegardee();
        }

        var st = con.prepareStatement("update equipe set nom = ? where id = ?");
        st.setString(1, nom);
        st.setInt(2, super.getId());

        st.executeUpdate();
    }
    
    private static List<Equipe> fromResultSetToList(ResultSet list) throws SQLException {
        List<Equipe> res = new ArrayList<>();
        while (list.next()) {
            res.add(new Equipe(list.getInt("id"), list.getString("nom"), list.getInt("idTournois")));
        }
        return res; 
    }
    
    public static List<Equipe> toutesLesEquipes(Connection con) throws SQLException {
        List<Equipe> res = new ArrayList<>();
        try (PreparedStatement pst = con.prepareStatement("select id,nom,idTournois from equipe")) {
            try (ResultSet allU = pst.executeQuery()) {
                return fromResultSetToList(allU);
            }
        }
    }

    public static List<Equipe> findByIdTournois(Connection con, int idTournois) throws SQLException {
        List<Equipe> res = new ArrayList<>();
        try (PreparedStatement pst = con.prepareStatement("select id,nom,idTournois from equipe where idTournois=?")) {
            pst.setInt(1, idTournois);

            try (ResultSet allU = pst.executeQuery()) {
                return fromResultSetToList(allU);
            }
        }
    }

    public static List<Equipe> findByIdRonde(Connection con, int idRonde) throws SQLException {
        List<Equipe> res = new ArrayList<>();
        String sql = """
select e.id, e.nom, e.idTournois from equipe e
join ronde r on r.id = ?
join matchs m on m.idRonde = r.id
join score s on s.idEquipe = e.id and s.idMatch = m.id
group by e.id, e.nom, e.idTournois
                     """;

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, idRonde);

            try (ResultSet allU = pst.executeQuery()) {
                return fromResultSetToList(allU);
            }
        }
    }
    
    public static Optional<Equipe> findById(Connection con, int id) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement("select id,nom,idTournois from equipe where id=?")) {
            pst.setInt(1, id);
            ResultSet res = pst.executeQuery();

            if (res.next()) {
                String nom = res.getString(2);
                int idTournois = res.getInt("idTournois");
                return Optional.of(new Equipe(id, nom, idTournois));
            } else {
                return Optional.empty();
            }
            
        }
    }
}

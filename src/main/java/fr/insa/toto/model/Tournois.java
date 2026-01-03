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

import fr.insa.toto.model.utils.ParentFace;
import com.vaadin.flow.component.notification.Notification;
import fr.insa.beuvron.utils.database.ClasseMiroir;
import fr.insa.toto.model.utils.ChildFace;
import fr.insa.toto.model.utils.Named;
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
public class Tournois extends ClasseMiroir implements Named {
    private String nom;
    private int nombreTerrains;

    private int nombreRondes = -1;
    private int nombreMatchs = -1;

    public static class AsChild extends ChildFace {
        @Override
        public String typeName() {
            return nomTable;
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
    
    public static final TournoisParent tournois = new TournoisParent();
    private static class TournoisParent extends ParentFace<Tournois> {
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
        public int add(Tournois tournois, Connection con) throws SQLException, EntiteDejaSauvegardee {
            return tournois.getId();
        }

        @Override
        public void remove(Tournois tournois, Connection con) throws SQLException, EntiteNonSauvegardee {
            tournois.deleteFromDB(con);
        }

        @Override
        public List<Tournois> get(Connection con) throws SQLException {
            return Tournois.tousLesTournois(con);
        }

        public TournoisParent() {
            super(new Tournois.AsChild());
        }
    }

    public final JoueurParent joueurs = new JoueurParent();
    private class JoueurParent extends ParentFace<Joueur> {
        @Override
        public String parentObjectName() {
            return getName();
        }

        @Override
        public String parentTypeName() {
            return nomTable();
        }

        @Override
        public String le() {
            return "le ";
        }

        @Override
        public String du() {
            return "du ";
        }

        @Override
        public int add(Joueur joueur, Connection con) throws SQLException, EntiteDejaSauvegardee {
            // pour l'instant, les joueurs ne sont pas assignés aux tournois
            return joueur.getId();
        }

        @Override
        public void remove(Joueur joueur, Connection con) throws SQLException, EntiteNonSauvegardee {
            // pour l'instant, les joueurs ne sont pas assignés aux tournois
            joueur.deleteFromDB(con);
        }

        @Override
        public List<Joueur> get(Connection con) throws SQLException {
            // pour l'instant, les joueurs ne sont pas assignés aux tournois
            return Joueur.tousLesJoueurs(con);
        }

        public JoueurParent() {
            super(new Joueur.AsChild());
        }
    }

    public final MatchsParent matchs = new MatchsParent();
    private class MatchsParent extends ParentFace<Matchs> {
        @Override
        public String parentObjectName() {
            return getName();
        }

        @Override
        public String parentTypeName() {
            return nomTable();
        }

        @Override
        public String le() {
            return "le ";
        }

        @Override
        public String du() {
            return "du ";
        }

        @Override
        public int add(Matchs match, Connection con) throws SQLException, EntiteDejaSauvegardee {
            // pour l'instant, les matchs ne sont pas assignés aux tournois
            return match.getId();
        }

        @Override
        public void remove(Matchs match, Connection con) throws SQLException, EntiteNonSauvegardee {
            // pour l'instant, les matchs ne sont pas assignés aux tournois
            match.deleteFromDB(con);
        }

        @Override
        public List<Matchs> get(Connection con) throws SQLException {
            var list = Matchs.findByIdTournois(con, getId());
            for (var each: list)
                each.populate(con);
            return list;
        }

        public MatchsParent() {
            super(new Matchs.AsChild());
        }
    }

    public final EquipeParent equipes = new EquipeParent();
    private class EquipeParent extends ParentFace<Equipe> {
        @Override
        public String parentObjectName() {
            return getName();
        }

        @Override
        public String parentTypeName() {
            return nomTable();
        }

        @Override
        public String le() {
            return "le ";
        }

        @Override
        public String du() {
            return "du ";
        }

        @Override
        public int add(Equipe match, Connection con) throws SQLException, EntiteDejaSauvegardee {
            // TODO
            // pour l'instant, les equipe ne sont pas assignés aux tournois
            return match.getId();
        }

        @Override
        public void remove(Equipe match, Connection con) throws SQLException, EntiteNonSauvegardee {
            // TODO
            // pour l'instant, les equipe ne sont pas assignés aux tournois
            match.deleteFromDB(con);
        }

        @Override
        public List<Equipe> get(Connection con) throws SQLException {
            // TODO
            // pour l'instant, les equipe ne sont pas assignés aux tournois
            var list = Equipe.findByIdTournois(con, getId());
            for (var each: list)
                each.populate(con);
            return list;
        }

        public EquipeParent() {
            super(new Equipe.AsChild());
        }
    }

    public final RondeParent rondes = new RondeParent();
    private class RondeParent extends ParentFace<Ronde> {
        @Override
        public String parentObjectName() {
            return getName();
        }

        @Override
        public String parentTypeName() {
            return nomTable();
        }

        @Override
        public String le() {
            return "le ";
        }

        @Override
        public String du() {
            return "du ";
        }

        @Override
        public int add(Ronde match, Connection con) throws SQLException, EntiteDejaSauvegardee {
            // TODO
            // pour l'instant, les ronde ne sont pas assignés aux tournois
            return match.getId();
        }

        @Override
        public void remove(Ronde match, Connection con) throws SQLException, EntiteNonSauvegardee {
            // TODO
            // pour l'instant, les ronde ne sont pas assignés aux tournois
            match.deleteFromDB(con);
        }

        @Override
        public List<Ronde> get(Connection con) throws SQLException {
            var list = Ronde.findByIdTournois(con, getId());
            return list;
        }

        public RondeParent() {
            super(new Ronde.AsChild());
        }
    }

    private static final String nomTable = "tournoi";
    protected final String nomTable() {
        return this.nomTable;
    }

    public String getName() {
        return nom;
    }

    public void setName(String nom) {
        this.nom = nom;
    }

    public int getNombreTerrains() {
        return nombreTerrains;
    }

    public void setNombreTerrains(int nombreTerrains) {
        this.nombreTerrains = nombreTerrains;
    }

    public int getNombreRondes() {
        return nombreRondes;
    }

    public int getNombreMatchs() {
        return nombreMatchs;
    }

    public Tournois(int id, String nom, int nombreTerrains) {
        super(id);
        this.nom = nom;
        this.nombreTerrains = nombreTerrains;
    }

    public Tournois(String nom, int nombreTerrains) {
        this.nom = nom;
        this.nombreTerrains = nombreTerrains;
    }

    public Tournois(int id) {
        super(id);
        this.nom = "";
        this.nombreTerrains = 0;
    }

    public Tournois() {
        this.nom = "";
        this.nombreTerrains = 0;
    }

    public Tournois clone() {
        return new Tournois(getId(), nom, nombreTerrains);
    }

    @Override
    protected Statement saveSansId(Connection con) throws SQLException {
        var st = con.prepareStatement("insert into tournois (nom, nombreTerrains) values (?, ?)",
                PreparedStatement.RETURN_GENERATED_KEYS);
        st.setString(1, nom);
        st.setInt(2, nombreTerrains);

        st.executeUpdate();
        return st;
    }

    @Override
    public void populate(Connection con) throws SQLException {
        var rondes = Ronde.findByIdTournois(con, this.getId());
        this.nombreRondes = rondes.size();
        this.nombreMatchs = 0;

        for (Ronde ronde: rondes) {
            this.nombreMatchs += ronde.getNbMatchs(con);
        }
    }

    public void update(Connection con) throws SQLException, EntiteNonSauvegardee {
        if (super.getId() == -1) {
            throw new EntiteNonSauvegardee();
        }

        var st = con.prepareStatement("update tournois set nom = ?, nombreTerrains = ? where id = ?");
        st.setString(1, nom);
        st.setInt(2, nombreTerrains);
        st.setInt(3, super.getId());

        st.executeUpdate();
    }

    private static List<Tournois> fromResultSetToList(ResultSet list) throws SQLException {
        List<Tournois> res = new ArrayList<>();
        while (list.next()) {
            res.add(new Tournois(list.getInt("id"), list.getString("nom"), list.getInt("nombreTerrains")));
        }
        return res;
        
    }

    public static List<Tournois> tousLesTournois(Connection con) throws SQLException {
        List<Tournois> res = new ArrayList<>();
        try (PreparedStatement pst = con.prepareStatement("select id,nom,nombreTerrains from tournois")) {
            try (ResultSet allU = pst.executeQuery()) {
                return fromResultSetToList(allU);
            }
        }
    }

    public static Optional<Tournois> findById(Connection con, int id) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement("select id,nom,nombreTerrains from tournois where id=?")) {
            pst.setInt(1, id);
            ResultSet res = pst.executeQuery();

            if (res.next()) {
                String nom = res.getString(2);
                int nombreTerrains = res.getInt(3);
                return Optional.of(new Tournois(id, nom, nombreTerrains));
            } else {
                return Optional.empty();
            }
            
        }
    }
    
    public static List<Tournois> findByEquipe(Connection con, String nom) throws SQLException{
        try (PreparedStatement pst = con.prepareStatement("select id,nom,from equipe where nom=?")) {
            pst.setString(1, nom);
            try (ResultSet allU = pst.executeQuery()) {
                return fromResultSetToList(allU);
            }
        }
    }
    
}

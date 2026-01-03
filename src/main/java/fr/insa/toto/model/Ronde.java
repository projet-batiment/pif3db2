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
import fr.insa.beuvron.utils.database.ClasseMiroir;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.utils.ChildFace;
import fr.insa.toto.model.utils.ModifiedState;
import fr.insa.toto.model.utils.Named;
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
public class Ronde extends ClasseMiroir implements Named {
    private int idTournois;
    private int numero;
    private boolean enCours;

    private ModifiedState state;
    private String tournoisName = "(Tournoi inconnu)";

    private static final String nomTable = "ronde";
    protected final String nomTable() {
        return this.nomTable;
    }

    @Override
    public String getName() {
        return this.tournoisName + " #" + Integer.toString(this.getNumero());
    }

    public Ronde(int id, int idTournois, int numero, boolean enCours) {
        super(id);
        this.enCours = enCours;
        this.idTournois = idTournois;
        this.numero = numero;
    }

    public Ronde(int idTournois, int numero, boolean enCours) {
        this.enCours = enCours;
        this.idTournois = idTournois;
        this.numero = numero;
    }

    public Ronde(int idTournois) {
        this.enCours = true;
        this.idTournois = idTournois;

        try (Connection con = ConnectionPool.getConnection()) {
            this.numero = findNextByTournois(con, idTournois);

        } catch (SQLException ex) {
            NotificationError.sql(ex);
            this.numero = 99;
        }
    }

    public static class AsChild extends ChildFace {
        @Override
        public String typeName() {
            return nomTable;
        }

        @Override
        protected String leChildPrefix() {
            return "la ";
        }

        @Override
        protected String duChildPrefix() {
            return "de la ";
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
            return "la ";
        }

        @Override
        public String du() {
            return "de la ";
        }

        @Override
        public int add(Joueur joueur, Connection con) throws SQLException, EntiteDejaSauvegardee {
            throw new IllegalArgumentException("On n'ajoute pas un joueur à une ronde");
        }

        @Override
        public void remove(Joueur joueur, Connection con) throws SQLException, EntiteNonSauvegardee {
            // pour l'instant, les joueurs ne sont pas assignés aux tournois
            joueur.deleteFromDB(con);
        }

        @Override
        public List<Joueur> get(Connection con) throws SQLException {
            return Joueur.findByIdRonde(con, getId());
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
            return "la ";
        }

        @Override
        public String du() {
            return "de la ";
        }

        @Override
        public int add(Matchs match, Connection con) throws SQLException, EntiteDejaSauvegardee {
            if (match.getIdRonde() != getId())
                throw new IllegalAttributeException("ronde.add: " + match.getIdRonde() + " match.idRonde != ronde.id " + getId());

            return match.getId();
        }

        @Override
        public void remove(Matchs match, Connection con) throws SQLException, EntiteNonSauvegardee {
            // pour l'instant, les matchs ne sont pas assignés aux tournois
            match.deleteFromDB(con);
        }

        @Override
        public List<Matchs> get(Connection con) throws SQLException {
            var list = Matchs.findByIdRonde(con, getId());
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
            return "la ";
        }

        @Override
        public String du() {
            return "de la ";
        }

        @Override
        public int add(Equipe match, Connection con) throws SQLException, EntiteDejaSauvegardee {
            throw new IllegalArgumentException("On n'ajoute pas une équipe à une ronde");
        }

        @Override
        public void remove(Equipe match, Connection con) throws SQLException, EntiteNonSauvegardee {
            // pour l'instant, les equipe ne sont pas assignés aux tournois
            match.deleteFromDB(con);
        }

        @Override
        public List<Equipe> get(Connection con) throws SQLException {
            // TODO
            // pour l'instant, les equipe ne sont pas assignés aux tournois
            var list = Equipe.findByIdRonde(con, getId());
            for (var each: list)
                each.populate(con);
            return list;
        }

        public EquipeParent() {
            super(new Equipe.AsChild());
        }
    }

    public int getIdTournois() {
        return idTournois;
    }

    public String getNomTournois() {
        return this.tournoisName;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public boolean isEnCours() {
        return enCours;
    }

    public void setEnCours(boolean enCours) {
        this.enCours = enCours;
    }

    public int getNbMatchs(Connection con) throws SQLException {
        int nbMatchs = 0;

        PreparedStatement pst = con.prepareStatement("select count(*) as total from matchs where idRonde = ?");
        pst.setInt(1, this.getId());

        try (ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                nbMatchs = rs.getInt("total");
            }
        }

        return nbMatchs;
    }
    
    public int getNbMatchs(){
        try (Connection con = ConnectionPool.getConnection()) {
            return this.getNbMatchs(con);

        } catch (SQLException ex) {
            NotificationError.sql(ex);
            return -1;
        }
    }

    @Override
    protected Statement saveSansId(Connection con) throws SQLException {
        var st = con.prepareStatement("insert into ronde (idTournois, numero, enCours) values (?, ?, ?)",
                PreparedStatement.RETURN_GENERATED_KEYS);
        st.setInt(1, idTournois);
        st.setInt(2, numero);
        st.setBoolean(3, enCours);
        
        st.executeUpdate();
        return st;
    }

    public void update(Connection con) throws SQLException, EntiteNonSauvegardee {
        if (super.getId() == -1) {
            throw new EntiteNonSauvegardee();
        }

        var st = con.prepareStatement("update ronde set idTournois = ?, numero = ?, enCours = ? where id = ?");
        st.setInt(1, idTournois);
        st.setInt(2, numero);
        st.setBoolean(3, enCours);
        st.setInt(4, super.getId());

        st.executeUpdate();
    }

    public void populate(Connection con) throws SQLException, NoSuchElementException {
        if (this.state != ModifiedState.POPULATED) {
            if (this.idTournois == ClasseMiroir.ID_UNSAVED)
                throw new NoSuchElementException("Aucun tournois n'est renseigné pour la ronde " + this.getId());

            var tournois = Tournois.findById(con, this.idTournois);
            if (tournois.isEmpty())
                throw new NoSuchElementException("Le tournois " + idTournois + " de la ronde " + this.getId() + " est introuvable dans la base de données");

            this.tournoisName = tournois.get().getName();

            this.state = ModifiedState.POPULATED;
        }
    }
    private static List<Ronde> fromResultSetToList(ResultSet list) throws SQLException {
        List<Ronde> res = new ArrayList<>();
        while (list.next()) {
            res.add(new Ronde(list.getInt("id"), list.getInt("idTournois"), list.getInt("numero"), list.getBoolean("enCours")));
        }
        return res; 
    }
        
    public static List<Ronde> toutesLesRondes(Connection con) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement("select id,idTournois,numero,enCours from ronde")) {
            try (ResultSet allU = pst.executeQuery()) {
                return fromResultSetToList(allU);
            }
        }
    }

    public static Optional<Ronde> findById(Connection con, int id) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement("select idTournois,numero,enCours from ronde where id=?")) {
            pst.setInt(1, id);
            ResultSet res = pst.executeQuery();

            if (res.next()) {
                int idTournois = res.getInt("idTournois");
                int numero = res.getInt("numero");
                boolean enCours = res.getBoolean("enCours");
                return Optional.of(new Ronde(id, idTournois, numero, enCours));
            } else {
                return Optional.empty();
            }
        }
    }

    public static List<Ronde> findByIdTournois(Connection con, int idTournois) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement("select id,idTournois,numero,enCours from ronde where idTournois=?")) {
            pst.setInt(1, idTournois);

            try (ResultSet allU = pst.executeQuery()) {
                return fromResultSetToList(allU)
                        .stream()
                        .sorted((a, b) -> a.numero - b.numero)
                        .collect(Collectors.toList());
            }
        }
    }
        
    public static Optional<Ronde> findByTournoisNumero(Connection con, int idTournois, int numero) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement("select id,mumero,enCours from ronde where idTournois=?")) {
            pst.setInt(1, idTournois);
            ResultSet res = pst.executeQuery();

            if (res.next()) {
                boolean enCours = res.getBoolean("enCours");
                int id = res.getInt("id");
                return Optional.of(new Ronde(id, idTournois, numero, enCours));
            } else {
                return Optional.empty();
            }
        }
    }

    public static int findNextByTournois(Connection con, int idTournois) throws SQLException {
        var list = findByIdTournois(con, idTournois);

        int max = 0;
        for (var each: list) {
            if (each.numero > max)
                max = each.numero;
        }

        return max + 1;
    }

    public static Optional<Ronde> findByTournoisEnCours(Connection con, int idTournois) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement("select id,numero,enCours from ronde where idTournois=?")) {
            pst.setInt(1, idTournois);
            ResultSet res = pst.executeQuery();

            if (res.next()) {
                boolean enCours = res.getBoolean("enCours");
                int numero = res.getInt("numero");
                int id = res.getInt("id");
                return Optional.of(new Ronde(id, idTournois, numero, enCours));
            } else {
                return Optional.empty();
            }
        }
    }
}

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
import fr.insa.toto.model.utils.ChildFace;
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
import java.util.Optional;

/**
 *
 * @author elio
 */
public class User extends ClasseMiroir implements Named {
    private String username;
    private String password;
    private boolean admin;

    private static final String nomTable = "user";
    protected final String nomTable() {
        return this.nomTable;
    }

    public static final User PORCELAINE = new User(ClasseMiroir.ID_PORCELAINE, "Nouveau...", "", false);

    public User(String uname, String pwd, boolean admin) {
        this.username = uname;
        this.password = pwd;
        this.admin = admin;
    }

    public User(int id, String uname, String pwd, boolean admin) {
        super(id);

        this.username = uname;
        this.password = pwd;
        this.admin = admin;
    }

    public User() {
        this.username = "";
        this.password = "";
        this.admin = false;
    }

    public static class AsChild extends ChildFace {
        @Override
        public String typeName() {
            return "utilisateur";
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

    public static final UserParent users = new UserParent();
    private static class UserParent extends ParentFace<User> {
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
        public int add(User user, Connection con) throws SQLException, EntiteDejaSauvegardee {
            return user.getId();
        }

        @Override
        public void remove(User user, Connection con) throws SQLException, EntiteNonSauvegardee {
            user.deleteFromDB(con);
        }

        @Override
        public List<User> get(Connection con) throws SQLException {
            return User.tousLesUsers(con);
        }

        public UserParent() {
            super(new User.AsChild());
        }
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String getName() {
        return this.getUsername();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    @Override
    public void deleteChildren(Connection con) throws SQLException {
        var joueur = Joueur.findByIdUser(con, this.getId());
        if (joueur.isPresent()) {
            joueur.get().setIdUser(null);
        }
    }

    @Override
    protected Statement saveSansId(Connection con) throws SQLException {
        var st = con.prepareStatement("insert into user (username, password, admin) values (?, ?, ?)",
                PreparedStatement.RETURN_GENERATED_KEYS);
        st.setString(1, username);
        st.setString(2, password);
        st.setBoolean(3, admin);

        st.executeUpdate();
        return st;
    }

    @Override
    public void update(Connection con) throws SQLException, EntiteNonSauvegardee {
        if (super.getId() == -1) {
            throw new EntiteNonSauvegardee();
        }

        var st = con.prepareStatement("update user set username = ?, password = ?, admin = ? where id = ?");
        st.setString(1, username);
        st.setString(2, password);
        st.setBoolean(3, admin);
        st.setInt(4, super.getId());

        st.executeUpdate();
    }

    public static Optional<User> findByUsername(Connection con, String username) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement("select id,password,admin from user where username=?")) {
            pst.setString(1, username);
            ResultSet res = pst.executeQuery();

            if (res.next()) {
                int id = res.getInt("id");
                String password = res.getString("password");
                boolean admin = res.getBoolean("admin");
                return Optional.of(new User(id, username, password, admin));
            } else {
                return Optional.empty();
            }
            
        }
    }

    public static Optional<User> findById(Connection con, int id) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement("select id,username,password,admin from user where id=?")) {
            pst.setInt(1, id);
            ResultSet res = pst.executeQuery();

            if (res.next()) {
                String username = res.getString("username");
                String password = res.getString("password");
                boolean admin = res.getBoolean("admin");
                return Optional.of(new User(id, username, password, admin));
            } else {
                return Optional.empty();
            }
            
        }
    }

    public static Optional<User> findByJoueur(Connection con, Joueur joueur) throws SQLException {
        if (joueur.getIdUser() == null) {
            return Optional.of(new User());
        } else {
            return findById(con, joueur.getIdUser());
        }
    }

    private static List<User> fromResultSetToList(ResultSet list) throws SQLException {
        List<User> res = new ArrayList<>();
        while (list.next()) {
            res.add(new User(list.getInt("id"), list.getString("username"), list.getString("password"), list.getBoolean("admin")));
        }
        return res;
    }
    
    public static List<User> tousLesUsers(Connection con) throws SQLException {
        List<User> res = new ArrayList<>();
        try (PreparedStatement pst = con.prepareStatement("select id,username,password,admin from user")) {
            try (ResultSet allU = pst.executeQuery()) {
                return fromResultSetToList(allU);
            }
        }
    }

    public static List<User> tousLesAdmins(Connection con) throws SQLException {
        List<User> res = new ArrayList<>();
        try (PreparedStatement pst = con.prepareStatement("select id,username,password,admin from user where admin = ?")) {
            pst.setBoolean(1, true);

            try (ResultSet allU = pst.executeQuery()) {
                return fromResultSetToList(allU);
            }
        }
    }
}

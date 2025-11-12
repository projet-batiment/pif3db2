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

import com.vaadin.flow.component.notification.Notification;
import fr.insa.beuvron.utils.database.ClasseMiroir;
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
public class Tournois extends ClasseMiroir {
    private String nom;
    private int nombreRondes;

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getNombreRondes() {
        return nombreRondes;
    }

    public void setNombreRondes(int nombreRondes) {
        this.nombreRondes = nombreRondes;
    }

    public Tournois(int id, String nom, int nombreRondes) {
        super(id);
        this.nom = nom;
        this.nombreRondes = nombreRondes;
    }

    public Tournois(String nom, int nombreRondes) {
        this.nom = nom;
        this.nombreRondes = nombreRondes;
    }

    public Tournois(int id) {
        super(id);
        this.nom = "";
        this.nombreRondes = 0;
    }

    public Tournois() {
        this.nom = "";
        this.nombreRondes = 0;
    }

    public Tournois clone() {
        return new Tournois(getId(), nom, nombreRondes);
    }

    public void deleteFromDB(Connection con) throws EntiteNonSauvegardee, SQLException {
        if (super.getId() == -1) {
            throw new EntiteNonSauvegardee();
        } else {
            var st = con.prepareStatement("delete from tournois where id = ?");
            st.setInt(1, super.getId());

            st.executeUpdate();
        }
    }

    @Override
    protected Statement saveSansId(Connection con) throws SQLException {
        var st = con.prepareStatement("insert into tournois (nom, nombreRondes) values (?, ?)",
                PreparedStatement.RETURN_GENERATED_KEYS);
        st.setString(1, nom);
        st.setInt(2, nombreRondes);

        st.executeUpdate();
        return st;
    }

    public int updateOrNew(Connection con) throws SQLException {
        try {
            this.update(con);
            return -3;
        } catch (EntiteNonSauvegardee e) {
            return this.saveInDB(con);
        }
    }

    public void update(Connection con) throws SQLException, EntiteNonSauvegardee {
        if (super.getId() == -1) {
            throw new EntiteNonSauvegardee();
        }

        var st = con.prepareStatement("update tournois set nom = ?, nombreRondes = ? where id = ?");
        st.setString(1, nom);
        st.setInt(2, nombreRondes);
        st.setInt(3, super.getId());

        st.executeUpdate();
    }

    private static List<Tournois> fromResultSetToList(ResultSet list) throws SQLException {
        List<Tournois> res = new ArrayList<>();
        while (list.next()) {
            res.add(new Tournois(list.getInt("id"), list.getString("nom"), list.getInt("nombreRondes")));
        }
        return res;
        
    }

    public static List<Tournois> tousLesTournois(Connection con) throws SQLException {
        List<Tournois> res = new ArrayList<>();
        try (PreparedStatement pst = con.prepareStatement("select id,nom,nombreRondes from tournois")) {
            try (ResultSet allU = pst.executeQuery()) {
                return fromResultSetToList(allU);
            }
        }
    }

    public static Optional<Tournois> findById(Connection con, int id) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement("select id,nom,nombreRondes from tournois where id=?")) {
            pst.setInt(1, id);
            ResultSet res = pst.executeQuery();

            if (res.next()) {
                String nom = res.getString(2);
                int nombreRondes = res.getInt(3);
                return Optional.of(new Tournois(id, nom, nombreRondes));
            } else {
                return Optional.empty();
            }
            
        }
    }
}

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
public class Equipe extends ClasseMiroir {
    private String nom;

    public Equipe(String nom) {
        this.nom = nom;
    }

    public String getNom() {
        return nom;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }

    public Equipe(int id, String nom) {
        super(id);
        this.nom = nom;
    }
    
    @Override
    protected Statement saveSansId(Connection con) throws SQLException {
        var st = con.prepareStatement("insert into equipe (nom) values (?)");
        st.setString(1, nom);

        return st;
    }
    
    private static List<Equipe> fromResultSetToList(ResultSet list) throws SQLException {
        List<Equipe> res = new ArrayList<>();
        while (list.next()) {
            res.add(new Equipe(list.getInt("id"), list.getString("nom")));
        }
        return res; 
    }
    
    public static List<Equipe> toutesLesEquipes(Connection con) throws SQLException {
        List<Equipe> res = new ArrayList<>();
        try (PreparedStatement pst = con.prepareStatement("select id,nom from equipe")) {
            try (ResultSet allU = pst.executeQuery()) {
                return fromResultSetToList(allU);
            }
        }
    }
    
    public static Optional<Equipe> findById(Connection con, int id) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement("select id,nom from equipe where id=?")) {
            pst.setInt(1, id);
            ResultSet res = pst.executeQuery();

            if (res.next()) {
                String nom = res.getString(2);
                return Optional.of(new Equipe(id, nom));
            } else {
                return Optional.empty();
            }
            
        }
    }
}

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
public class Joueur extends ClasseMiroir {
    private String surnom;
    private String categorie;
    private int taillecm;

    public Joueur(String surnom, String categorie, int taillecm) {
        this.surnom = surnom;
        this.categorie = categorie;
        this.taillecm = taillecm;
    }
    
    public String getSurnom() {
        return surnom;
    }

    public void setSurnom(String surnom) {
        this.surnom = surnom;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public int getTaillecm() {
        return taillecm;
    }

    public void setTaillecm(int taillecm) {
        this.taillecm = taillecm;
    }

    public Joueur(int id, String surnom, String categorie, int taillecm) {
        super(id);
        this.surnom = surnom;
        this.categorie = categorie;
        this.taillecm = taillecm;
    }

    
    @Override
    protected Statement saveSansId(Connection con) throws SQLException {
        var st = con.prepareStatement("insert into joueur (surnom, categorie, taillecm) values (?, ?, ?)");
        st.setString(1, surnom);
        st.setString(2, categorie);
        st.setInt(3, taillecm);

        return st;
    }
    
    private static List<Joueur> fromResultSetToList(ResultSet list) throws SQLException {
        List<Joueur> res = new ArrayList<>();
        while (list.next()) {
            res.add(new Joueur(list.getInt("id"), list.getString("surnom"), list.getString("categorie"), list.getInt("taillecm")));
        }
        return res;
    }
    
    public static List<Joueur> tousLesJoueurs(Connection con) throws SQLException {
        List<Joueur> res = new ArrayList<>();
        try (PreparedStatement pst = con.prepareStatement("select id,surnom,categorie,taillecm from joueur")) {
            try (ResultSet allU = pst.executeQuery()) {
                return fromResultSetToList(allU);
            }
        }
    }
    
    public static Optional<Joueur> findById(Connection con, int id) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement("select id,surnom,categorie,taillecm from joueur where id=?")) {
            pst.setInt(1, id);
            ResultSet res = pst.executeQuery();

            if (res.next()) {
                String surnom = res.getString(2);
                String categorie = res.getString(3);
                int taillecm = res.getInt(4);
                return Optional.of(new Joueur(id, surnom, categorie, taillecm));
            } else {
                return Optional.empty();
            }
            
        }
    }
}

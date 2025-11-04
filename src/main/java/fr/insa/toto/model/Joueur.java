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
import java.sql.SQLException;
import java.sql.Statement;

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

    @Override
    protected Statement saveSansId(Connection con) throws SQLException {
        var st = con.prepareStatement("insert into joueur (surnom, categorie, taillecm) values (?, ?, ?)");
        st.setString(1, surnom);
        st.setString(2, categorie);
        st.setInt(3, taillecm);

        return st;
    }
}

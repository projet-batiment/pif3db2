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
public class Composition extends ClasseMiroir {
    private int idEquipe;
    private int idJoueur;

    public Composition(int idEquipe, int idJoueur) {
        this.idEquipe = idEquipe;
        this.idJoueur = idJoueur;
    }

    public int getIdEquipe() {
        return idEquipe;
    }

    public void setIdEquipe(int idEquipe) {
        this.idEquipe = idEquipe;
    }

    public int getIdJoueur() {
        return idJoueur;
    }

    public void setIdJoueur(int idJoueur) {
        this.idJoueur = idJoueur;
    }

    public Composition(int id, int idEquipe, int idJoueur) {
        super(id);
        this.idEquipe = idEquipe;
        this.idJoueur = idJoueur;
    }
    
    

    @Override
    protected Statement saveSansId(Connection con) throws SQLException {
        var st = con.prepareStatement("insert into composition (idEquipe, idJoueur) values (?, ?)");
        st.setInt(1, idEquipe);
        st.setInt(2, idJoueur);

        return st;
    }

    public void update(Connection con) throws SQLException, EntiteNonSauvegardee {
        if (super.getId() == -1) {
            throw new EntiteNonSauvegardee();
        }

        var st = con.prepareStatement("update composition set idEquipe = ?, idJoueur = ? where id = ?");
        st.setInt(1, idEquipe);
        st.setInt(2, idJoueur);
        st.setInt(3, super.getId());

        st.executeUpdate();
    }
    
    private static List<Composition> fromResultSetToList(ResultSet list) throws SQLException {
        List<Composition> res = new ArrayList<>();
        while (list.next()) {
            res.add(new Composition(list.getInt("id"), list.getInt("idEquipe"), list.getInt("idJoueur")));
        }
        return res; 
    }
    
    public static List<Composition> toutesLesCompositions(Connection con) throws SQLException {
        List<Composition> res = new ArrayList<>();
        try (PreparedStatement pst = con.prepareStatement("select id,nom from composition")) {
            try (ResultSet allU = pst.executeQuery()) {
                return fromResultSetToList(allU);
            }
        }
    }
    
    public static Optional<Composition> findById(Connection con, int id) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement("select id,idEquipe,idJoueur from composition where id=?")) {
            pst.setInt(1, id);
            ResultSet res = pst.executeQuery();

            if (res.next()) {
                int idEquipe = res.getInt(2);
                int idJoueur = res.getInt(3);
                return Optional.of(new Composition(id, idEquipe, idJoueur));
            } else {
                return Optional.empty();
            }
            
        }
    }
}

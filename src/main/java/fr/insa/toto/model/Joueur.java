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
import fr.insa.toto.model.utils.ModifiedState;
import fr.insa.toto.model.utils.ChildFace;
import fr.insa.beuvron.utils.database.ClasseMiroir;
import fr.insa.toto.model.utils.IntOrNull;
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
public class Joueur extends ClasseMiroir implements Named {
    public final static Joueur PORCELAINE = new Joueur(ClasseMiroir.ID_PORCELAINE, "Nouveau...", "", 0, ClasseMiroir.ID_UNSAVED);

    private String surnom;
    private String categorie;
    private int taillecm;
    private Integer idUser;

    private static final String nomTable = "joueur";
    protected final String nomTable() {
        return this.nomTable;
    }

    private ModifiedState state;

    public Joueur() {
        this.surnom = "";
        this.categorie = "";
        this.taillecm = 170;
        this.idUser = null;

        this.state = ModifiedState.CREATED;
    }

    public Joueur(String surnom, String categorie, int taillecm) {
        this.surnom = surnom;
        this.categorie = categorie;
        this.taillecm = taillecm;
        this.idUser = null;

        this.state = ModifiedState.CREATED;
    }

    public Joueur(int id, String surnom, String categorie, int taillecm, Integer userId) {
        super(id);

        this.surnom = surnom;
        this.categorie = categorie;
        this.taillecm = taillecm;
        this.idUser = userId;

        this.state = id >= 0 ? ModifiedState.NORMAL : ModifiedState.PORCELAINE;
    }

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
    
    public String getSurnom() {
        return surnom;
    }

    @Override
    public String getName() {
        return this.getSurnom();
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

    public Integer getIdUser() {
        return idUser;
    }

    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }

    @Override
    protected Statement saveSansId(Connection con) throws SQLException {
        var st = con.prepareStatement("insert into joueur (surnom, categorie, taillecm, idUser) values (?, ?, ?, ?)",
                PreparedStatement.RETURN_GENERATED_KEYS);
        st.setString(1, surnom);
        st.setString(2, categorie);
        st.setInt(3, taillecm);
        IntOrNull.setIntOrNull(st, 4, idUser);

        st.executeUpdate();
        return st;
    }

    public void update(Connection con) throws SQLException, EntiteNonSauvegardee {
        if (super.getId() == -1) {
            throw new EntiteNonSauvegardee();
        }

        var st = con.prepareStatement("update joueur set surnom = ?, categorie = ?, taillecm = ?, idUserr = ? where id = ?");
        st.setString(1, surnom);
        st.setString(2, categorie);
        st.setInt(3, taillecm);
        IntOrNull.setIntOrNull(st, 4, idUser);
        st.setInt(5, super.getId());

        st.executeUpdate();
    }

    private static List<Joueur> fromResultSetToList(ResultSet list) throws SQLException {
        List<Joueur> res = new ArrayList<>();
        while (list.next()) {
            res.add(new Joueur(list.getInt("id"), list.getString("surnom"), list.getString("categorie"), list.getInt("taillecm"), IntOrNull.getIntOrNull(list, "idUser")));
        }
        return res;
    }
    
    public static List<Joueur> tousLesJoueurs(Connection con) throws SQLException {
        List<Joueur> res = new ArrayList<>();
        try (PreparedStatement pst = con.prepareStatement("select id,surnom,categorie,taillecm,idUser from joueur")) {
            try (ResultSet allU = pst.executeQuery()) {
                return fromResultSetToList(allU);
            }
        }
    }
    
    public static Optional<Joueur> findById(Connection con, int id) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement("select id,surnom,categorie,taillecm,idUser from joueur where id=?")) {
            pst.setInt(1, id);
            ResultSet res = pst.executeQuery();

            if (res.next()) {
                String surnom = res.getString(2);
                String categorie = res.getString(3);
                int taillecm = res.getInt(4);
                Integer idUser = IntOrNull.getIntOrNull(res, "idUser");
                return Optional.of(new Joueur(id, surnom, categorie, taillecm, idUser));
            } else {
                return Optional.empty();
            }
        }
    }
}
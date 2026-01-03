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
package fr.insa.toto.webui.joueur;

import com.vaadin.flow.component.button.Button;
import fr.insa.toto.webui.utils.Editor;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Joueur;
import fr.insa.toto.model.User;
import fr.insa.toto.webui.user.UserEditor;
import fr.insa.toto.webui.utils.DialogDeleteChild;
import fr.insa.toto.webui.utils.NotificationError;
import fr.insa.toto.webui.utils.Utils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;

/**
 *
 * @author elio
 */
public class JoueurEditor extends Editor<Joueur> {
    private TextField surnom;
    private TextField taillecm;
    private TextField categorie;

    private Button addUser; 
    private Button openUser; 

    private static void deleteUserCallback(User user) {
        new DialogDeleteChild<>(User.users, user, () -> {}).open();
    }

    @Override
    protected Joueur newObject() {
        return new Joueur();
    }

    protected void setObject() {
        super.setVisibleLegitimate(null);

        if (this.object instanceof Joueur joueur) {
            this.surnom.setValue(joueur.getSurnom());
            this.surnom.setEnabled(true);
            this.taillecm.setValue("" + joueur.getTaillecm());
            this.taillecm.setEnabled(true);
            this.categorie.setValue(joueur.getCategorie());
            this.categorie.setEnabled(true);

            if (this.object.getIdUser() == null) {
                if (this.object.getId() == Joueur.ID_UNSAVED) {
                    this.addUser.setVisible(false);
                } else {
                    Utils.visibleAdmin(this.addUser);
                }
                this.openUser.setVisible(false);
            } else {
                User user = null;
                try (Connection con = ConnectionPool.getConnection()) {
                    user = User.findById(con, this.object.getIdUser()).orElse(null);

                    super.setVisibleLegitimate(user);
                } catch (SQLException ex) {
                    NotificationError.sql(ex);
                }

                this.addUser.setVisible(false);
                Utils.visibleLegitimate(this.openUser, user);
            }
        } else {
            this.surnom.setValue("");
            this.surnom.setEnabled(false);
            this.taillecm.setValue("");
            this.taillecm.setEnabled(false);
            this.categorie.setValue("");
            this.categorie.setEnabled(false);

            this.addUser.setVisible(false);
            this.openUser.setVisible(false);
        }
    }

    @Override
    protected List<Joueur> openObject(Connection con) throws SQLException {
        return Joueur.tousLesJoueurs(con);
    }

    public Joueur compile() {
        if (this.surnom.getValue().isBlank()) {
            NotificationError.userError("Il manque le surnom du joueur");
            return null;
        }
        if (this.taillecm.getValue().isBlank()) {
            NotificationError.userError("Il manque la taille du joueur");
            return null;
        }
        if (this.categorie.getValue().isBlank()) {
            NotificationError.userError("Il manque la catégorie du joueur");
            return null;
        }

        object.setSurnom(surnom.getValue());
        object.setTaillecm(Integer.parseInt(taillecm.getValue()));
        object.setCategorie(categorie.getValue());

        return this.object;
    }

    @Override
    protected void onSaved() {
        NotificationError.info("Le joueur " + object.getSurnom()+ " a bien été sauvegardé");
    }

    @Override
    protected String generatedUrl() {
        return "joueur";
    }

    public JoueurEditor() {
        nouveau = Joueur.PORCELAINE;

        super.setHeaderTitle("Aperçu du joueur");

        super.setSelectItemLabelGenerator(Joueur::getSurnom);
        super.setSelectLabel("Joueur");

        surnom = new TextField();
        surnom.setLabel("Surnom du joueur");

        taillecm = new TextField();
        taillecm.setLabel("Taille (cm)");
        taillecm.setAllowedCharPattern("[0-9]");
        taillecm.setMaxLength(3);

        categorie = new TextField();
        categorie.setLabel("Catégorie");
        categorie.setMaxLength(1);

        addUser = new Button("Créer un utilisateur associé");
        addUser.addClickListener(e -> {
            super.close();

            if (this.object instanceof Joueur joueur) {
                var editor = new UserEditor();
                editor.setOnDeletedCallback(user -> deleteUserCallback(user));
                editor.setJoueur(joueur);
                editor.open(null);
            } else {
                NotificationError.internError("Trying to create a user but joueur is null", null);
            }
        });
        openUser = new Button("Voir l'utilisateur associé");
        openUser.addClickListener(e -> {
            if (this.object instanceof Joueur joueur) {
                try (Connection con = ConnectionPool.getConnection()) {
                    var editor = new UserEditor();
                    editor.setOnDeletedCallback(user -> deleteUserCallback(user));
                    editor.open(User.findById(con, joueur.getIdUser()).get());
                    super.close();
                } catch (SQLException ex) {
                    NotificationError.sql(ex);
                } catch (NoSuchElementException ex) {
                    NotificationError.internError("L'utilisateur " + joueur.getIdUser() + " n'a pas été trouvé dans la base de données", ex);
                }
            } else {
                NotificationError.internError("Trying to create a user but joueur is null", null);
            }
        });

        super.addChildren(
            surnom,
            new HorizontalLayout(
                taillecm,
                categorie
            ),
            addUser,
            openUser
        );
    }
}

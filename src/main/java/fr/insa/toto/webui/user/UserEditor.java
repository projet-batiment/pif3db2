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
package fr.insa.toto.webui.user;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import fr.insa.beuvron.utils.database.ClasseMiroir;
import fr.insa.beuvron.utils.database.ConnectionPool;
import fr.insa.toto.model.Joueur;
import fr.insa.toto.model.User;
import fr.insa.toto.webui.joueur.JoueurEditor;
import fr.insa.toto.webui.session.Session;
import fr.insa.toto.webui.utils.DialogDeleteChild;
import fr.insa.toto.webui.utils.Editor;
import fr.insa.toto.webui.utils.NotificationError;
import fr.insa.toto.webui.utils.Utils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

/**
 *
 * @author elio
 */
public class UserEditor extends Editor<User> {
    private Consumer<User> onSavedCallback;

    private TextField nom;
    private PasswordField password;
    private Select<String> admin;
    private Button resetPwd;
    private Button viewJoueur;
    private Span viewJoueurAbsent;
    private Span newJoueur;
    private Span newPassword;

    private Integer joueurId;

    private boolean isNewObject = false;

    // Le joueur auquel associer le nouveau User
    private Joueur joueur = null;

    private static final String ADMIN = "Administrateur";
    private static final String NORMAL = "Normal";

    private static void deleteUserCallback(Joueur joueur) {
        new DialogDeleteChild<>(Joueur.joueurs, joueur, () -> {}).open();
    }

    @Override
    protected User newObject() {
        return new User();
    }

    protected void setObject() {
        if (this.object instanceof User user) {
            super.setVisibleLegitimate(this.object);

            if (this.joueur == null) {
                try (Connection con = ConnectionPool.getConnection()) {
                    var joueur = Joueur.findByIdUser(con, this.object.getId());
                    if (joueur.isPresent()) {
                        this.joueurId = joueur.get().getId();
                        this.viewJoueur.setVisible(true);
                        this.viewJoueurAbsent.setVisible(false);
                    } else {
                        this.viewJoueur.setVisible(false);
                        this.viewJoueurAbsent.setVisible(true);
                    }
                    this.newJoueur.setVisible(false);
                } catch (SQLException ex) {
                    NotificationError.sql(ex);
                }
            } else {
                this.viewJoueur.setVisible(false);
                this.viewJoueurAbsent.setVisible(false);
                this.newJoueur.setVisible(true);
                this.newJoueur.setText("Création d'un utilisateur pour le joueur " + this.joueur.getName() + ".");
            }

            this.nom.setValue(user.getUsername());
            this.nom.setEnabled(true);
            this.admin.setValue(this.object.isAdmin() ? ADMIN : NORMAL);
            this.admin.setEnabled(Session.isAdmin());

            if (this.object.getId() == Session.getUser().getId()) {
                this.resetPwd.setVisible(false);
                this.password.setVisible(true);
                this.password.setValue(this.object.getPassword());
                this.newPassword.setVisible(false);
            } else if (this.object.getId() == ClasseMiroir.ID_UNSAVED) {
                this.resetPwd.setVisible(false);
                this.password.setVisible(false);
                this.newPassword.setVisible(true);
            } else {
                this.resetPwd.setVisible(true);
                this.password.setVisible(false);
                this.newPassword.setVisible(false);
            }
        } else {
            this.nom.setValue("");
            this.nom.setEnabled(false);
            this.admin.setValue("");
            this.admin.setEnabled(false);
            this.resetPwd.setVisible(false);

            this.viewJoueur.setVisible(false);
            this.viewJoueurAbsent.setVisible(false);
            this.newJoueur.setVisible(false);
        }
    }

    @Override
    protected List<User> openObject(Connection con) throws SQLException {
        return User.tousLesUsers(con);
    }

    public User compile() {
        if (this.nom.getValue().isBlank()) {
            NotificationError.userError("Il manque le nom de l'utilisateur");
            return null;
        }

        if (this.admin.getValue().isBlank()) {
            NotificationError.userError("Il manque le type de l'utilisateur");
            return null;
        }

        object.setUsername(nom.getValue());
        object.setAdmin(admin.getValue().equals(ADMIN));

        if (this.object.getId() == ClasseMiroir.ID_UNSAVED) {
            this.isNewObject = true;
        } else {
            if (Session.getUser().equals(this.object))
                this.object.setPassword(password.getValue());
            this.isNewObject = false;
        }

        return this.object;
    }

    @Override
    protected void onSaved() {
        if (this.joueur == null)
            NotificationError.info("L'utilisateur " + object.getUsername() + " a bien été sauvegardé");
        else {
            this.joueur.setIdUser(this.object.getId());
            try (Connection con = ConnectionPool.getConnection()) {
                this.joueur.update(con);
            } catch (SQLException ex) {
                NotificationError.sql(ex);
            }

            NotificationError.info("L'utilisateur " + object.getUsername() + " a bien été sauvegardé et lié au joueur " + this.joueur.getName());
        }

        if (this.isNewObject) {
            ResetPasswordDialog.open(this.object);
        }
    }

    @Override
    protected String generatedUrl() {
        return "user/";
    }

    public void setJoueur(Joueur joueur) {
        this.joueur = joueur;
    }

    public UserEditor() {
        nouveau = User.PORCELAINE;

        super.setHeaderTitle("Aperçu de l'utilisateur");

        super.setSelectItemLabelGenerator(User::getUsername);
        super.setSelectLabel("Utilisateur");

        nom = new TextField();
        nom.setLabel("Nom de l'utilisateur");

        password = new PasswordField();
        password.setLabel("Mot de passe");

        admin = new Select<>();
        admin.setItems(new String[]{
            NORMAL,
            ADMIN,
        });
        Utils.enableAdmin(admin);

        resetPwd = new Button("Réinitialiser le mot de passe");
        resetPwd.addThemeVariants(ButtonVariant.LUMO_ERROR);
        resetPwd.addClickListener(e -> {
            var dialog = new ConfirmDialog();
            dialog.setHeader("Confirmer");
            dialog.setText("Réinitialiser le mot de passe de l'utilisateur " + this.object.getUsername() + " ?");

            dialog.setRejectable(false);

            dialog.setCancelable(true);
            dialog.setCancelText("Annuler");

            dialog.setConfirmText("Réinitialiser");
            dialog.setConfirmButtonTheme("error primary");
            dialog.addConfirmListener(t -> {
                ResetPasswordDialog.open(this.object);
            });

            dialog.open();
        });

        viewJoueur = new Button("Voir le joueur associé");
        viewJoueur.addClickListener(e -> {
            if (this.joueurId != null) {
                try (Connection con = ConnectionPool.getConnection()) {
                    var editor = new JoueurEditor();
                    editor.setOnDeletedCallback(joueur -> deleteUserCallback(joueur));
                    editor.open(Joueur.findById(con, this.joueurId).get());
                    super.close();

                } catch (SQLException ex) {
                    NotificationError.sql(ex);
                } catch (NoSuchElementException ex) {
                    NotificationError.internError("Le joueur " + this.joueurId + " n'a pas été trouvé dans la base de données", ex);
                }
            }
        });

        viewJoueurAbsent = new Span("Cet utilisateur n'est relié à aucun joueur.");
        newPassword = new Span("Le mot de passe sera généré automatiquement.");
        newJoueur = new Span();

        super.setSelectEnableAdmin();

        super.addChildren(nom, admin, resetPwd, password, viewJoueur, viewJoueurAbsent, newJoueur, newPassword);
    }
}

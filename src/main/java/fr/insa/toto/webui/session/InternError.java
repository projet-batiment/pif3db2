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
package fr.insa.toto.webui.session;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import fr.insa.toto.webui.utils.Layout;

/**
 *
 * @author elio
 */
@Route(value = "erreur", layout = Layout.Default.class)
public class InternError extends VerticalLayout {
    public InternError() {
        super.add(new H2("Erreur : identifiant introuvable"));
        super.add(new Span("Avez-vous tenté d'accéder à une URL saisie manuellement, sans passer par l'interface habituelle ?"));

        var messages = Session.getErrorMessages();
        if (messages.isEmpty()) {
            super.add(new Span("(Erreur inconnue)"));
        } else {
            for (var each : messages) {
                super.add(new Span(each));
            }
            Session.clearErrorMessages();
        }
    }
}

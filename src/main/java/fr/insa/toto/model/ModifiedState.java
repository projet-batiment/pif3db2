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

/**
 *
 * @author elio
 */
public enum ModifiedState {
    CREATED,        // l'objet n'a pas encore été sauvegardé
    NORMAL,         // l'objet vient d'être téléchargé depuis la base de données, n'est pas modifié
    EDITED,         // les attributs directs de l'objet ont été modifiés
    POPULATED,      // l'objet contient des informations sur ses attributs externes, rien n'est modifié
    DEPTH_EDITED,   // les attributs externes de l'objet ont été modifiés
    PORCELAINE,     // l'objet est utilisé temporairement et n'a pas vocation à être sauvegardé
}

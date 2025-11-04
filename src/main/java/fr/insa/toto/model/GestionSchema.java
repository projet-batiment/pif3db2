/*
Copyright 2000- Francois de Bertrand de Beuvron

This file is ecole of CoursBeuvron.

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

import fr.insa.beuvron.utils.database.ConnectionSimpleSGBD;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author francois
 * @author toto
 */
public class GestionSchema {

    /**
     * concisely create and destroy sql schemas thanks to abstraction via
     * classes; each instance of the subclasses represents data about the sql
     * elements' structure
     */
    class Beton {

        TableSkeleton[] tables;

        /**
         * Returns: if b then returns s, else ""
         *
         * @param b boolean
         * @param s String
         * @return
         */
        private static String ifTrue(boolean b, String s) {
            return b ? s : "";
        }

        interface CreateSkeleton {
            public abstract String createString();
            public abstract String getName();
        }

        interface DeleteSkeleton extends CreateSkeleton {
            public abstract String deleteString();
        }

        /**
         * holds the info about an sql constraint
         */
        static class ConstraintSkeleton implements DeleteSkeleton {

            private String name;
            private TableSkeleton borrower;
            private TableSkeleton lender;
            private ColumnSkeleton borrowed;
            private ColumnSkeleton lended;

            public ConstraintSkeleton(TableSkeleton borrower, TableSkeleton lender, ColumnSkeleton borrowed, ColumnSkeleton lended) {
                this.name = String.join("_", new String[]{
                    "CONSTRAINT",
                    borrower.name,
                    "FROM",
                    lender.name,}
                );
                this.borrower = borrower;
                this.lender = lender;
                this.borrowed = borrowed;
                this.lended = lended;
            }

            /**
             * generate the constraint creation string based on the contents
             *
             * @return
             */
            public String createString() {
                return String.join(" ", new String[]{
                    "ALTER TABLE",
                    this.borrower.name,
                    "ADD CONSTRAINT",
                    this.name,
                    "FOREIGN KEY",
                    "(" + this.borrowed.name + ")",
                    "REFERENCES",
                    this.lender.name,
                    "(" + this.lended.name + ")",}
                );
            }

            @Override
            public String deleteString() {
                return String.join(" ", new String[]{
                    "ALTER TABLE",
                    this.borrower.name,
                    "DROP CONSTRAINT",
                    this.name,
                });
            }

            public String getName() {
                return name;
            }
        }

        /**
         * holds the info about an sql table
         */
        static class TableSkeleton implements DeleteSkeleton {

            private String name;
            private ColumnSkeleton[] columns;
            private List<ConstraintSkeleton> constraints;

            private static String idString;

            public static String getIdString() {
                return idString;
            }

            public static void setIdString(String idString) {
                TableSkeleton.idString = idString;
            }

            public TableSkeleton(String name, ColumnSkeleton[] columns) {
                this.name = name;
                this.columns = columns;
                this.constraints = new ArrayList<>();
            }

            /**
             * generate the table creation string based on the contents
             *
             * @param idString
             * @return
             */
            public String createString() {
                return String.join(" ", new String[]{
                    "CREATE TABLE",
                    this.name,
                    "(",
                    Stream.concat(
                        Stream.of(idString),
                        Arrays.stream(this.columns).map(each -> each.createString())
                    ).collect(Collectors.joining(", ")),
                    ")",
                });
            }

            @Override
            public String deleteString() {
                return String.join(" ", new String[]{
                    "DROP TABLE",
                    this.name,
                });
            }

            public String getName() {
                return name;
            }
        }

        /**
         * holds the info about an sql column, attached to a table
         */
        static class ColumnSkeleton implements CreateSkeleton {

            /**
             * the name of an sqltype, without its potential parameters; we
             * admit the only possible parameter is an integer held by the
             * mother class, ColumnSkeleton
             */
            public enum SQLType {
                INTEGER("INTEGER"),
                FLOAT("FLOAT"),
                VARCHAR("VARCHAR");

                private final String name;

                private SQLType(String s) {
                    name = s;
                }

                public String create() {
                    return this.name;
                }
            }

            private String name;
            private SQLType type;
            private int length;
            private boolean notNull;
            private boolean unique;
            private boolean primary;

            private static final ColumnSkeleton id = new ColumnSkeleton("id", SQLType.INTEGER);

            /**
             * generate the column creation string based on the contents
             *
             * @return
             */
            public String createString() {
                return String.join(" ", new String[]{
                    name,
                    switch (type) {
                        case VARCHAR ->
                            type + "(" + length + ")";
                        default ->
                            type.create();
                    },
                    ifTrue(notNull, "NOT NULL"),
                    ifTrue(unique, "UNIQUE"),
                    ifTrue(primary, "PRIMARY"),}
                ).trim();
            }

            /**
             * Defaults notNull, unique and primary to false
             * 
             * @param name
             * @param type
             * @param length 
             */
            public ColumnSkeleton(String name, SQLType type, int length) {
                this.name = name;
                this.type = type;
                this.length = length;
                this.notNull = false;
                this.unique = false;
                this.primary = false;
            }

            /**
             * Defaults notNull, unique and primary to false
             * 
             * @param name
             * @param type
             */
            public ColumnSkeleton(String name, SQLType type) {
                this(name, type, 0);
            }

            /**
             * set notNull to true
             * @return 
             */
            public ColumnSkeleton setNotNull() {
                this.notNull = true;
                return this;
            }

            /**
             * set unique to true
             * @return 
             */
            public ColumnSkeleton setUnique() {
                this.unique = true;
                return this;
            }

            /**
             * set primary to true
             * @return 
             */
            public ColumnSkeleton setPrimary() {
                this.primary = true;
                return this;
            }

            public String getName() {
                return name;
            }
        }
    }

    /**
     * holds a skeleton structure
     */
    public static class SkeletonList {
        Beton.TableSkeleton[] tables;
        Beton.ConstraintSkeleton[] constraints;

        private SkeletonList(Beton.TableSkeleton[] tables, Beton.ConstraintSkeleton[] constraints) {
            this.tables = tables;
            this.constraints = constraints;
        }

        private Beton.DeleteSkeleton[] join(Beton.DeleteSkeleton[] first, Beton.DeleteSkeleton[] then) {
            return Stream.concat(Arrays.stream(first), Arrays.stream(then)).toArray(Beton.DeleteSkeleton[]::new);
        }

        public Beton.DeleteSkeleton[] joinTablesFirst() {
            return this.join(this.tables, this.constraints);
        }
        public Beton.DeleteSkeleton[] joinConstraintsFirst() {
            return this.join(this.constraints, this.tables);
        }
    }

    /**
     * generates the appropriate SkeletonList
     * 
     * @param con
     * @return 
     */
    public static SkeletonList skeletons() {
        /// shared columns

        var scoreIdMatch = new Beton.ColumnSkeleton(
                "idMatch",
                Beton.ColumnSkeleton.SQLType.INTEGER
        );

        var scoreIdEquipe = new Beton.ColumnSkeleton(
                "idEquipe",
                Beton.ColumnSkeleton.SQLType.INTEGER
        );

        var compositionIdEquipe = new Beton.ColumnSkeleton(
                "idEquipe",
                Beton.ColumnSkeleton.SQLType.INTEGER
        );

        var compositionIdJoueur = new Beton.ColumnSkeleton(
                "idJoueur",
                Beton.ColumnSkeleton.SQLType.INTEGER
        );

        /// tables and unshared columns

        Beton.TableSkeleton joueur = new Beton.TableSkeleton(
                "joueur",
                new Beton.ColumnSkeleton[]{
                    new Beton.ColumnSkeleton(
                            "surnom",
                            Beton.ColumnSkeleton.SQLType.VARCHAR,
                            24
                    ).setUnique(),
                    new Beton.ColumnSkeleton(
                            "categorie",
                            Beton.ColumnSkeleton.SQLType.VARCHAR,
                            1
                    ),
                    new Beton.ColumnSkeleton(
                            "taillecm",
                            Beton.ColumnSkeleton.SQLType.INTEGER
                    ),}
        );

        Beton.TableSkeleton matchs = new Beton.TableSkeleton(
                "matchs",
                new Beton.ColumnSkeleton[]{
                    new Beton.ColumnSkeleton(
                            "ronde",
                            Beton.ColumnSkeleton.SQLType.INTEGER
                    ),
                    new Beton.ColumnSkeleton(
                            "idEquipeA",
                            Beton.ColumnSkeleton.SQLType.INTEGER
                    ),
                    new Beton.ColumnSkeleton(
                            "idEquipeB",
                            Beton.ColumnSkeleton.SQLType.INTEGER
                    ),}
        );

        Beton.TableSkeleton equipe = new Beton.TableSkeleton(
                "equipe",
                new Beton.ColumnSkeleton[]{
                    new Beton.ColumnSkeleton(
                            "nom",
                            Beton.ColumnSkeleton.SQLType.VARCHAR,
                            24
                    ).setUnique(),
                }
        );

        Beton.TableSkeleton composition = new Beton.TableSkeleton(
                "composition",
                new Beton.ColumnSkeleton[]{
                    compositionIdEquipe,
                    compositionIdJoueur,
                }
        );

        Beton.TableSkeleton score = new Beton.TableSkeleton(
                "score",
                new Beton.ColumnSkeleton[]{
                    new Beton.ColumnSkeleton(
                            "score",
                            Beton.ColumnSkeleton.SQLType.INTEGER
                    ),
                    scoreIdEquipe,
                    scoreIdMatch
                }
        );

        /// constraints

        Beton.ConstraintSkeleton ctrScoreMatch = new Beton.ConstraintSkeleton(
                score,
                matchs,
                scoreIdMatch,
                Beton.ColumnSkeleton.id
        );

        Beton.ConstraintSkeleton ctrCompositionEquipe = new Beton.ConstraintSkeleton(
                composition,
                equipe,
                compositionIdEquipe,
                Beton.ColumnSkeleton.id
        );

        Beton.ConstraintSkeleton ctrCompositionJoueur = new Beton.ConstraintSkeleton(
                composition,
                joueur,
                compositionIdJoueur,
                Beton.ColumnSkeleton.id
        );

        return new SkeletonList(
            new Beton.TableSkeleton[] {
                joueur,
                equipe,
                matchs,
                composition,
                score,
            },

            new Beton.ConstraintSkeleton[] {
                ctrScoreMatch,
                ctrCompositionEquipe,
                ctrCompositionJoueur,
            }
        );
    }

    /**
     * create a database's structure based on the given skeletons
     *
     * @param con
     * @param skeletons
     * @throws SQLException
     */
    public static void creeSchema(Connection con, Beton.CreateSkeleton[] skeletons) throws SQLException {
        try {
            con.setAutoCommit(false);

            try (Statement st = con.createStatement()) {
                // apply the updates
                for (Beton.CreateSkeleton each: skeletons) {
                    try {
                        st.executeUpdate(each.createString());
                    } catch (SQLException ex) {
                        throw new SQLException("exception encountered when parsing sql command for creating skeleton " + each.getName() + ": " + ex.getMessage());
                    }
                }

                // commit the updates
                con.commit();
            }
        } catch (SQLException ex) {
            con.rollback();
            throw ex;
        } finally {
            con.setAutoCommit(true);
            System.out.println("creeSchema: finished");
        }
    }

    /**
     *
     * @param con
     * @throws SQLException
     */
    public static void deleteSchema(Connection con, Beton.DeleteSkeleton[] skeletons) throws SQLException {
        try {
            con.setAutoCommit(false);

            try (Statement st = con.createStatement()) {
                // apply the updates
                for (Beton.DeleteSkeleton each: skeletons) {
                    try {
                        st.executeUpdate(each.deleteString());
                    } catch (SQLException ex) {
                        System.out.println("exception encountered when parsing sql command for deleting skeleton " + each.getName() + ": " + ex.getMessage());
                        System.out.println("this error has not been thrown");
                    }
                }

                // commit the updates
                con.commit();
            }
        } catch (SQLException ex) {
            con.rollback();
            throw ex;
        } finally {
            con.setAutoCommit(true);
            System.out.println("deleteSchema: finished");
        }
    }

    /**
     *
     * @param con
     * @param skeletons
     * @throws SQLException
     */
    public static void razBdd(Connection con, SkeletonList skeletons) throws SQLException {
        deleteSchema(con, skeletons.joinConstraintsFirst());
        creeSchema(con, skeletons.joinTablesFirst());
        System.out.println("razBdd: finished");
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        try (Connection con = ConnectionSimpleSGBD.defaultCon()) {

            // set the generic string for primary id key based on the database manager's format
            Beton.TableSkeleton.setIdString(ConnectionSimpleSGBD.sqlForGeneratedKeys(con, "id"));

            SkeletonList skeletons = skeletons();

            razBdd(con, skeletons);

        } catch (SQLException ex) {
            throw new Error(ex);
        }

//        try (Connection con = ConnectionSimpleSGBD.defaultCon()) {
//            razBdd(con);
//        } catch (SQLException ex) {
//            throw new Error(ex);
//        }
    }
}

package org.tybaco.types.model;

/*-
 * #%L
 * library
 * %%
 * Copyright (C) 2023 Montoni
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

public enum Primitive implements Type {
    VOID {
        @Override
        public String toString() {
            return "void";
        }
    },
    BOOLEAN {
        @Override
        public String toString() {
            return "boolean";
        }
    },
    CHAR {
        @Override
        public String toString() {
            return "char";
        }
    },
    FLOAT {
        @Override
        public String toString() {
            return "float";
        }
    },
    DOUBLE {
        @Override
        public String toString() {
            return "double";
        }
    },
    BYTE {
        @Override
        public String toString() {
            return "byte";
        }
    },
    SHORT {
        @Override
        public String toString() {
            return "short";
        }
    },
    INT {
        @Override
        public String toString() {
            return "int";
        }
    },
    LONG {
        @Override
        public String toString() {
            return "long";
        }
    }
}

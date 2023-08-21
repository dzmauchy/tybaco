package org.tybaco.model;

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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class Constant {

    private final int id;
    private final Type type;
    private final String value;

    public String expr() {
        return type.expr(value);
    }

    public enum Type {
        NULL {
            @Override
            protected String expr(String value) {
                return "null";
            }
        },
        BOOLEAN {
            @Override
            protected String expr(String value) {
                return value;
            }
        },
        BYTE {
            @Override
            protected String expr(String value) {
                return "((byte) " + value + ")";
            }
        },
        SHORT {
            @Override
            protected String expr(String value) {
                return "((short) " + value + ")";
            }
        },
        INT {
            @Override
            protected String expr(String value) {
                return value;
            }
        },
        LONG {
            @Override
            protected String expr(String value) {
                return value + "L";
            }
        },
        FLOAT {
            @Override
            protected String expr(String value) {
                return value + "f";
            }
        },
        DOUBLE {
            @Override
            protected String expr(String value) {
                return value + "d";
            }
        },
        CHAR {
            @Override
            protected String expr(String value) {
                return "((char) " + value + ")";
            }
        },
        STRING {
            @Override
            protected String expr(String value) {
                return "\"" + value + "\"";
            }
        };

        protected abstract String expr(String value);
    }
}

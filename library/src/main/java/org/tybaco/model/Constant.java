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
import lombok.Getter;
import org.w3c.dom.Element;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class Constant extends AbstractModelElement {

    private final int id;
    private final Type type;
    private final String value;

    void save(Element element) {
        element.setAttribute("id", Integer.toString(id));
        element.setAttribute("type", type.name());
        element.setAttribute("value", value);
        saveAttributes(element);
    }

    static Constant load(Element element) {
        var id = Integer.parseInt(element.getAttribute("id"));
        var type = Type.valueOf(element.getAttribute("type"));
        var value = element.getAttribute("value");
        var constant = new Constant(id, type, value);
        constant.loadAttributes(element);
        return constant;
    }

    @Override
    public int hashCode() {
        return id ^ type.hashCode() ^ value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Constant c && id == c.id && type == c.type && value.equals(c.value);
    }

    @Override
    public String toString() {
        return "Constant(" + id + "," + type + "," + value + ")";
    }

    public enum Type {
        NULL,
        BOOLEAN,
        BYTE,
        SHORT,
        INT,
        LONG,
        FLOAT,
        DOUBLE,
        CHAR,
        STRING
    }
}

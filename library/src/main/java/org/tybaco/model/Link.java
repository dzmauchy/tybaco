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

import static org.tybaco.model.Xml.elementByTag;
import static org.tybaco.model.Xml.withChild;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public final class Link extends AbstractModelElement {

    private final Out out;
    private final In in;

    void save(Element element) {
        withChild(element, "out", out::save);
        withChild(element, "in", in::save);
    }

    static Link load(Element element) {
        var out = elementByTag(element, "out");
        var in = elementByTag(element, "in");
        var link = new Link(Out.load(out), In.load(in));
        link.loadAttributes(element);
        return link;
    }

    @Override
    public int hashCode() {
        return out.hashCode() ^ in.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Link l && out.equals(l.out) && in.equals(l.in);
    }

    @Override
    public String toString() {
        return out + "-->" + in;
    }
}

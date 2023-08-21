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

import org.w3c.dom.Element;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.stream.IntStream.range;

class Xml {

    static Stream<Element> elementsByTag(Element element, String tag) {
        var list = element.getElementsByTagName(tag);
        return range(0, list.getLength())
                .mapToObj(list::item)
                .filter(Element.class::isInstance)
                .map(Element.class::cast);
    }

    static Stream<Element> elementsByTags(Element element, String enclosingTag, String tag) {
        return elementsByTag(element, enclosingTag).flatMap(e -> elementsByTag(e, tag));
    }

    static void withChild(Element element, String tag, Consumer<Element> consumer) {
        var doc = element.getOwnerDocument();
        var child = doc.createElement(tag);
        element.appendChild(child);
        consumer.accept(child);
    }
}

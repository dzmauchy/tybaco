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

import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static org.tybaco.model.Xml.elementsByTags;

abstract class AbstractModelElement {

    private final TreeMap<String, String> attributes = new TreeMap<>();

    public void setAttribute(String key, String value) {
        attributes.put(key, value);
    }

    public void removeAttribute(String key) {
        attributes.remove(key);
    }

    public Stream<String> attributes() {
        return attributes.keySet().stream();
    }

    public void forEachAttribute(BiConsumer<String, String> consumer) {
        attributes.forEach(consumer);
    }

    void saveAttributes(Element element) {
        var doc = element.getOwnerDocument();
        var attrsElement = (Element) element.appendChild(doc.createElement("attributes"));
        forEachAttribute((key, value) -> {
            var attrElement = (Element) attrsElement.appendChild(doc.createElement("attribute"));
            attrElement.setAttribute("key", key);
            attrElement.setAttribute("value", value);
        });
    }

    void loadAttributes(Element element) {
        elementsByTags(element, "attributes", "attribute").forEach(attrElement -> {
            var key = attrElement.getAttribute("key");
            var value = attrElement.getAttribute("value");
            setAttribute(key, value);
        });
    }
}

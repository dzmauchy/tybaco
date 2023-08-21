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

import java.util.ArrayList;
import java.util.Collections;

import static org.tybaco.model.Xml.elementsByTags;
import static org.tybaco.model.Xml.withChild;

public final class Diagram extends AbstractModelElement {

    private final ModelIds blockIds = new ModelIds();
    private final ArrayList<Block> blocks = new ArrayList<>();

    public Block createBlock(String type, String method) {
        return createBlock(freeId(), type, method);
    }

    private Block createBlock(int id, String type, String method) {
        var block = new Block(id, type, method);
        var idx = Collections.binarySearch(blocks, block);
        if (idx < 0) {
            blocks.add(-(idx + 1), block);
        }
        return block;
    }

    public void removeBlock(Block block) {
        var idx = Collections.binarySearch(blocks, block);
        if (idx >= 0) {
            blockIds.clear(idx);
            blocks.remove(idx);
        }
    }

    int freeId() {
        var i = blockIds.nextClearBit(0);
        blockIds.set(i);
        return i;
    }

    public void save(Element element) {
        withChild(element, "blocks", bse -> blocks.forEach(block -> withChild(bse, "block", be -> {
            be.setAttribute("id", Integer.toString(block.getId()));
            be.setAttribute("type", block.getType());
            be.setAttribute("method", block.getMethod());
            block.saveAttributes(be);
        })));
    }

    public void load(Element element) {
        elementsByTags(element, "blocks", "block").forEach(blockElement -> {
            var id = Integer.parseInt(blockElement.getAttribute("id"));
            var type = blockElement.getAttribute("type");
            var method = blockElement.getAttribute("method");
            var block = createBlock(id, type, method);
            block.loadAttributes(blockElement);
        });
    }
}

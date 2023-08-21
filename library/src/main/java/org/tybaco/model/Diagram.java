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
    private final ModelIds constantIds = new ModelIds();
    private final ArrayList<Block> blocks = new ArrayList<>();
    private final ArrayList<Constant> constants = new ArrayList<>();
    private final ArrayList<Link> links = new ArrayList<>();

    public Block createBlock(String type, String method) {
        return createBlock(blockIds.newId(), type, method);
    }

    private Block createBlock(int id, String type, String method) {
        var block = new Block(id, type, method);
        var idx = Collections.binarySearch(blocks, block);
        blocks.add(-(idx + 1), block);
        return block;
    }

    public Constant createConstant(Constant.Type type, String value) {
        return createConstant(constantIds.newId(), type, value);
    }

    private Constant createConstant(int id, Constant.Type type, String value) {
        var constant = new Constant(id, type, value);
        var idx = Collections.binarySearch(constants, constant);
        constants.add(-(idx + 1), constant);
        return constant;
    }

    public void removeBlock(Block block) {
        var idx = Collections.binarySearch(blocks, block);
        if (idx >= 0) {
            blockIds.clear(idx);
            blocks.remove(idx);
        }
    }

    public void save(Element element) {
        withChild(element, "blocks", bse -> blocks.forEach(block -> withChild(bse, "block", be -> {
            be.setAttribute("id", Integer.toString(block.getId()));
            be.setAttribute("type", block.getType());
            be.setAttribute("method", block.getMethod());
            block.saveAttributes(be);
        })));
        withChild(element, "constants", cse -> constants.forEach(c -> withChild(cse, "constant", ce -> {
            ce.setAttribute("id", Integer.toString(c.getId()));
            ce.setAttribute("type", c.getType().name());
            ce.setAttribute("value", c.getValue());
            c.saveAttributes(ce);
        })));
    }

    public void load(Element element) {
        elementsByTags(element, "blocks", "block").forEach(blockElement -> {
            var id = Integer.parseInt(blockElement.getAttribute("id"));
            var type = blockElement.getAttribute("type");
            var method = blockElement.getAttribute("method");
            var block = createBlock(id, type, method);
            blockIds.set(block.getId());
            block.loadAttributes(blockElement);
        });
        elementsByTags(element, "constants", "constant").forEach(constantElement -> {
            var id = Integer.parseInt(constantElement.getAttribute("id"));
            var type = Constant.Type.valueOf(constantElement.getAttribute("type"));
            var value = constantElement.getAttribute("value");
            var constant = createConstant(id, type, value);
            constantIds.set(constant.getId());
            constant.loadAttributes(constantElement);
        });
    }

    public void validate() {
        if (blockIds.cardinality() != blocks.size()) {
            throw new IllegalStateException(
                    "Blocks: cardinality = %d, size = %d".formatted(blockIds.cardinality(), blocks.size())
            );
        }
        if (constantIds.cardinality() != constants.size()) {
            throw new IllegalStateException(
                    "Constants: cardinality = %d, size = %d".formatted(constantIds.cardinality(), constants.size())
            );
        }
    }
}

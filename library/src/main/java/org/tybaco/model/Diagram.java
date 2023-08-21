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

import org.eclipse.collections.api.factory.primitive.IntObjectMaps;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.w3c.dom.Element;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.tybaco.model.Xml.elementsByTags;
import static org.tybaco.model.Xml.withChild;

public final class Diagram extends AbstractModelElement {

    private final BitSet blockIds = new BitSet();
    private final BitSet constantIds = new BitSet();
    private final MutableIntObjectMap<Block> blocks = IntObjectMaps.mutable.empty();
    private final MutableIntObjectMap<Constant> constants = IntObjectMaps.mutable.empty();
    private final LinkedList<Link> links = new LinkedList<>();

    public Block createBlock(String type, String method) {
        return createBlock(new Block(blockIds.nextClearBit(0), type, method));
    }

    private Block createBlock(Block block) {
        blocks.put(block.getId(), block);
        blockIds.set(block.getId());
        return block;
    }

    public Constant createConstant(Constant.Type type, String value) {
        return createConstant(new Constant(constantIds.nextClearBit(0), type, value));
    }

    private Constant createConstant(Constant constant) {
        constants.put(constant.getId(), constant);
        constantIds.set(constant.getId());
        return constant;
    }

    public void removeBlock(Block block) {
        if (blocks.remove(block.getId()) != null) blockIds.clear(block.getId());
    }

    public void removeConstant(Constant constant) {
        if (constants.remove(constant.getId()) != null) constantIds.clear(constant.getId());
    }

    public void forEachBlock(Consumer<Block> consumer) {
        blocks.forEach(consumer);
    }

    public void forEachConstant(Consumer<Constant> consumer) {
        constants.forEach(consumer);
    }

    public void forEachLink(Consumer<Link> consumer) {
        links.forEach(consumer);
    }

    public Optional<Block> getBlock(int id) {
        return Optional.ofNullable(blocks.get(id));
    }

    public Optional<Constant> getConstant(int id) {
        return Optional.ofNullable(constants.get(id));
    }

    public Optional<Block> findBlock(Predicate<Block> predicate) {
        return blocks.stream().filter(predicate).findFirst();
    }

    public Optional<Constant> findConstant(Predicate<Constant> predicate) {
        return constants.stream().filter(predicate).findFirst();
    }

    public Optional<Link> findLink(Predicate<Link> predicate) {
        return links.stream().filter(predicate).findFirst();
    }

    public void save(Element element) {
        withChild(element, "blocks", bse -> blocks.forEach(block -> withChild(bse, "block", block::save)));
        withChild(element, "constants", cse -> constants.forEach(c -> withChild(cse, "constant", c::save)));
        withChild(element, "links", lse -> links.forEach(link -> withChild(lse, "link", link::save)));
        saveAttributes(element);
    }

    public void load(Element element) {
        elementsByTags(element, "blocks", "block").map(Block::load).forEach(this::createBlock);
        elementsByTags(element, "constants", "constant").map(Constant::load).forEach(this::createConstant);
        elementsByTags(element, "links", "link").map(Link::load).forEach(links::add);
        loadAttributes(element);
    }
}

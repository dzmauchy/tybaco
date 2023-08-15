package org.tybaco.types.resolver;

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

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.tybaco.types.model.Array;
import org.tybaco.types.model.Atomic;
import org.tybaco.types.model.Intersection;
import org.tybaco.types.model.Parameterized;
import org.tybaco.types.model.Primitive;
import org.tybaco.types.model.Type;
import org.tybaco.types.model.Wildcard;

import java.util.stream.Stream;

import static java.util.Arrays.stream;

class Types {

    static Type from(ITypeBinding type) {
        if (type.isPrimitive()) {
            return switch (type.getName()) {
                case "int" -> Primitive.INT;
                case "long" -> Primitive.LONG;
                case "double" -> Primitive.DOUBLE;
                case "float" -> Primitive.FLOAT;
                case "short" -> Primitive.SHORT;
                case "byte" -> Primitive.BYTE;
                case "boolean" -> Primitive.BOOLEAN;
                case "void" -> Primitive.VOID;
                case "char" -> Primitive.CHAR;
                default -> throw new IllegalArgumentException(type.getName());
            };
        } else if (type.isArray()) {
            return new Array(toGround(type.getComponentType()));
        } else if (type.isParameterizedType()) {
            return new Parameterized(
                    type.getTypeDeclaration().getQualifiedName(),
                    stream(type.getTypeArguments()).map(Types::toGround).toList()
            );
        } else if (type.isWildcardType()) {
            return new Wildcard(toGround(type.getBound()), type.isUpperbound());
        } else if (type.isTypeVariable()) {
            return toGround(type);
        } else if (type.isIntersectionType()) {
            var list = stream(type.getTypeBounds())
                    .flatMap(Types::ground)
                    .distinct()
                    .filter(e -> !"java.lang.Object".equals(e.getQualifiedName()))
                    .map(Types::from)
                    .toList();
            return switch (list.size()) {
                case 0 -> new Atomic("java.lang.Object");
                case 1 -> list.get(0);
                default -> new Intersection(list);
            };
        } else {
            return new Atomic(type.getQualifiedName());
        }
    }

    private static Type toGround(ITypeBinding t) {
        if (t.isTypeVariable()) {
            var list = ground(t)
                    .distinct()
                    .filter(e -> !"java.lang.Object".equals(e.getQualifiedName()))
                    .map(Types::from)
                    .toList();
            return switch (list.size()) {
                case 0 -> new Atomic("java.lang.Object");
                case 1 -> list.get(0);
                default -> new Intersection(list);
            };
        } else {
            return from(t);
        }
    }

    private static Stream<ITypeBinding> ground(ITypeBinding t) {
        return t.isTypeVariable() ? stream(t.getTypeBounds()).flatMap(Types::ground) : Stream.of(t);
    }
}

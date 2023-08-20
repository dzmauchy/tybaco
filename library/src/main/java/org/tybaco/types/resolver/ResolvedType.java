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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.eclipse.jdt.internal.compiler.lookup.TypeBinding.VOID;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class ResolvedType {

    final TypeBinding type;

    public boolean isPrimitive() {
        return type.isPrimitiveType();
    }

    public boolean isParameterized() {
        return type.isParameterizedType();
    }

    public boolean isWildcard() {
        return type.isWildcard();
    }

    public boolean isIntersection() {
        return type.isIntersectionType();
    }

    public boolean isArray() {
        return type.isArrayType();
    }

    public boolean isBoxed() {
        return type.isBoxedPrimitiveType();
    }

    public boolean isVariable() {
        return type.isTypeVariable();
    }

    public boolean isFreeVariable() {
        return type.isFreeTypeVariable();
    }

    public boolean isRaw() {
        return type.isRawType();
    }

    public boolean isInterface() {
        return type.isInterface();
    }

    public boolean isClass() {
        return type.isClass();
    }

    public boolean isRecord() {
        return type.isRecord();
    }

    public boolean isEnum() {
        return type.isEnum();
    }

    public boolean isVoid() {
        return VOID == type;
    }

    public boolean isGround() {
        return (type.tagBits & TagBits.HasTypeVariable) == 0;
    }

    public boolean hasTypeVariables() {
        return (type.tagBits & TagBits.HasTypeVariable) != 0;
    }

    public ResolvedType getTypeParameter(int index) {
        if (type instanceof ParameterizedTypeBinding b) {
            if (index < 0 || index >= b.arguments.length) {
                return new ResolvedType(VOID);
            } else {
                return new ResolvedType(b.arguments[index]);
            }
        } else {
            return new ResolvedType(VOID);
        }
    }

    public ResolvedType getActual() {
        return new ResolvedType(type.actualType());
    }

    private Stream<MethodBinding> methods() {
        return type instanceof ReferenceBinding b ? Arrays.stream(b.methods()) : Stream.empty();
    }

    public Stream<Method> staticFactories() {
        return methods()
                .filter(m -> m.isStatic() && m.isPublic())
                .map(m -> new Method(type, m));
    }

    public Stream<Method> factories() {
        return methods()
                .filter(m -> m.isPublic() && !m.isStatic() && m.parameters.length > 1 && m.returnType != VOID)
                .map(m -> new Method(type, m));
    }

    public Stream<Method> inputs() {
        return methods()
                .filter(m -> m.isPublic() && !m.isStatic() && m.parameters.length == 1)
                .map(m -> new Method(type, m));
    }

    public Stream<Method> outputs() {
        return methods()
                .filter(m -> m.isPublic() && !m.isStatic() && m.parameters.length == 0 && m.returnType != VOID)
                .map(m -> new Method(type, m));
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ResolvedType t && type.isEquivalentTo(t.type);
    }

    @Override
    public String toString() {
        return String.valueOf(type.readableName());
    }
}

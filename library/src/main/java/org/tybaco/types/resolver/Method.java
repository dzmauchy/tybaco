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
import lombok.Getter;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class Method {

    final ITypeBinding owner;
    @Getter private final String parent;
    final IMethodBinding method;
    @Getter private final List<Arg> args;

    public String getName() {
        return method.getName();
    }

    public boolean isVarargs() {
        return method.isVarargs();
    }

    @AllArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class Arg {

        final ITypeBinding type;
        @Getter private final int index;
    }
}

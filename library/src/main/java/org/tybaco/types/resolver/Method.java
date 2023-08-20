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
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;
import java.util.stream.Stream;

import static java.util.stream.IntStream.range;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class Method {

    final TypeBinding owner;
    final MethodBinding method;

    public String getName() {return new String(method.selector);}
    public boolean isVarargs() {return method.isVarargs();}
    public List<Arg> getArgs() {return new ArgList(method);}
    public ResolvedType getReturnType() {return new ResolvedType(method.returnType);}
    public Stream<Input> inputs() {return getReturnType().inputs().map(Input::new);}
    public Stream<Output> outputs() {return getReturnType().outputs().map(Output::new);}

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Arg {

        private final MethodBinding method;
        @Getter private final int index;

        public ResolvedType getType() {return new ResolvedType(method.parameters[index]);}

        public String getName() {
            var names = method.parameterNames;
            return names.length == method.parameters.length ? new String(names[index]) : "arg" + index;
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ArgList extends AbstractList<Arg> implements RandomAccess {

        private final MethodBinding method;

        @Override public Arg get(int index) {return new Arg(method, index);}
        @Override public int size() {return method.parameters.length;}
        @Override public Stream<Arg> stream() {return range(0, method.parameters.length).mapToObj(i -> new Arg(method, i));}
    }

    public record Input(Method method) {
        public ResolvedType getType() {return new ResolvedType(method.method.parameters[0]);}
        public String getName() {return method.getName();}
    }

    public record Output(Method method) {
        public ResolvedType getType() {return method.getReturnType();}
        public String getName() {return method.getName();}
    }

    public record Factory(Method method) {
        public ResolvedType getType() {return method.getReturnType();}
        public List<Arg> getArgs() {return new ArgList(method.method);};
        public String getName() {return method.getName();}
    }
}

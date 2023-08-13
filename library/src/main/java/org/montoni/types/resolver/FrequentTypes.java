package org.montoni.types.resolver;

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

import org.montoni.types.model.Atomic;
import org.montoni.types.model.Parameterized;
import org.montoni.types.model.Type;

import java.util.List;

public class FrequentTypes {

    private FrequentTypes() {
        throw new UnsupportedOperationException();
    }

    public static final Atomic BOXED_INT = new Atomic("java.lang.Integer");
    public static final Atomic BOXED_LONG = new Atomic("java.lang.Long");

    public static final Atomic STRING = new Atomic("java.lang.String");

    public static Parameterized listOf(Type arg) {
        return new Parameterized("java.util.List", List.of(arg));
    }

    public static Parameterized mapOf(Type k, Type v) {
        return new Parameterized("java.util.Map", List.of(k, v));
    }
}

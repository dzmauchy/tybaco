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

import lombok.Getter;
import org.montoni.types.model.Type;

import java.util.List;

@Getter
public class TypeResolverException extends Exception {

    private final String name;
    private final Type type;

    public TypeResolverException(String name, List<String> messages, Type type) {
        super("Type resolver exception at " + name, null, true, false);
        this.type = type;
        this.name = name;
        messages.forEach(m -> addSuppressed(new Problem(m)));
    }

    public static final class Problem extends Exception {

        private Problem(String message) {
            super(message, null, false, false);
        }
    }
}

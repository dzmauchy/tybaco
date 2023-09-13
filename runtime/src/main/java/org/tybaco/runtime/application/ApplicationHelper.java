package org.tybaco.runtime.application;

/*-
 * #%L
 * runtime
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

import java.util.*;

import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.joining;

public interface ApplicationHelper {

  static Optional<String> setting(String name) {
    var propName = compile("_").splitAsStream(name).map(String::toLowerCase).collect(joining("."));
    var propValue = System.getProperty(propName);
    if (propValue != null) {
      return Optional.of(propValue);
    }
    return Optional.ofNullable(System.getenv(name));
  }

  static OptionalInt intSetting(String name) {
    return setting(name)
      .map(v -> {
        try {
          return OptionalInt.of(Integer.parseInt(v));
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("Unable to parse %s as int: %s".formatted(v, name), e);
        }
      })
      .orElse(OptionalInt.empty());
  }

  static OptionalLong longSetting(String name) {
    return setting(name)
      .map(v -> {
        try {
          return OptionalLong.of(Long.parseLong(v));
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("Unable to parse %s as long: %s".formatted(v, name), e);
        }
      })
      .orElse(OptionalLong.empty());
  }

  static Optional<Boolean> booleanSetting(String name) {
    return setting(name).map("true"::equalsIgnoreCase);
  }
}

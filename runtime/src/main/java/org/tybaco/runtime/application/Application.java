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

import java.util.List;

import static java.util.Objects.requireNonNull;

public record Application(
  String id,
  List<ApplicationConstant> constants,
  List<ApplicationBlock> blocks,
  List<ApplicationLink> links
) {

  static final ThreadLocal<Application> CURRENT_APPLICATION = new ThreadLocal<>();

  public static Application activeApplication() {
    return requireNonNull(CURRENT_APPLICATION.get(), "No active application found");
  }

  public int maxInternalId() {
    return Math.max(
      constants.stream().mapToInt(ApplicationConstant::id).max().orElse(0),
      blocks.stream().mapToInt(ApplicationBlock::id).max().orElse(0)
    );
  }
}

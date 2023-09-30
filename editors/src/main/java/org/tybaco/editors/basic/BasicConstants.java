package org.tybaco.editors.basic;

/*-
 * #%L
 * editors
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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.tybaco.editors.model.*;

import java.util.List;

@Component
@Descriptor(id = "basic", name = "Basic constants", icon = "ion4-ios-baseball", description = "Basic constants")
public final class BasicConstants implements ConstLib {

  private final List<? extends LibConst<?>> constants;

  public BasicConstants(@Qualifier("basic") List<? extends LibConst<?>> constants) {
    this.constants = constants;
  }

  @Override
  public List<? extends LibConst<?>> constants() {
    return constants;
  }
}

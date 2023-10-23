package org.tybloco.editors.text;

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

import javafx.beans.binding.StringBinding;

public interface TextSupport {

  default StringBinding text(String text) {
    return Texts.text(textClassLoader(), text);
  }

  default StringBinding text(String format, Object... args) {
    return Texts.text(textClassLoader(), format, args);
  }

  default StringBinding msg(String text) {
    return Texts.msg(textClassLoader(), text);
  }

  default StringBinding msg(String format, Object... args) {
    return Texts.msg(textClassLoader(), format, args);
  }

  default ClassLoader textClassLoader() {
    return getClass().getClassLoader();
  }
}

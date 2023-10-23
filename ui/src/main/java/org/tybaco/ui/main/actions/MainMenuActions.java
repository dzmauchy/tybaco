package org.tybaco.ui.main.actions;

/*-
 * #%L
 * ui
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

import org.kordamp.ikonli.materialdesign2.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.tybloco.editors.action.Action;
import org.tybloco.editors.icon.IconViewer;
import org.tybloco.editors.text.Texts;

import java.util.List;
import java.util.Locale;

@Component
public class MainMenuActions {

  @Qualifier("mainMenu")
  @Bean
  public Action projectMenu(@Qualifier("projectMenu") List<Action> actions) {
    return new Action("Project").actions(actions);
  }

  @Qualifier("mainMenu")
  @Bean
  public Action langsMenu() {
    return new Action("Languages").actions(
      new Action("English", "icon/us.png", e -> Texts.setLocale(Locale.ENGLISH)),
      new Action("Español", "icon/es.png", e -> Texts.setLocale(Locale.of("es"))),
      new Action("Italiano", "icon/it.png", e -> Texts.setLocale(Locale.ITALIAN)),
      new Action(),
      new Action("Set to system default", MaterialDesignE.ERASER, e -> Texts.setLocale(null))
    );
  }

  @Qualifier("mainMenu")
  @Bean
  public Action iconsMenu() {
    return new Action("Icons").actions(
      new Action("Viewer", MaterialDesignV.VIEW_ARRAY_OUTLINE, e -> IconViewer.show())
    );
  }
}

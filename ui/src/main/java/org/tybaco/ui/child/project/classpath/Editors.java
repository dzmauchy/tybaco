package org.tybaco.ui.child.project.classpath;

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

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.tybaco.editors.model.BlockLib;
import org.tybaco.editors.model.ConstLib;

import java.time.Duration;
import java.util.List;

import static java.lang.Thread.startVirtualThread;

@Component
public final class Editors {

  public final SimpleObjectProperty<List<ConstLib>> constLibs = new SimpleObjectProperty<>(this, "constLibs");
  public final SimpleObjectProperty<List<BlockLib>> blockLibs = new SimpleObjectProperty<>(this, "blockLibs");

  public Editors(ProjectClasspath classpath) {
    classpath.classPath.addListener((o, ov, nv) -> {
      constLibs.set(null);
      blockLibs.set(null);
      startVirtualThread(() -> update(nv.classLoader));
    });
  }

  private void update(ClassLoader classLoader) {
    try (var ctx = new GenericXmlApplicationContext()) {
      ctx.load("classpath*:tybaco/editors/config.xml");
      ctx.setClassLoader(classLoader);
      ctx.setAllowBeanDefinitionOverriding(false);
      ctx.setAllowCircularReferences(false);
      ctx.refresh();
      var constList = ctx.getBeanProvider(ConstLib.class).stream().toList();
      Platform.runLater(() -> constLibs.set(FXCollections.observableList(constList)));
      var blockLibList = ctx.getBeanProvider(BlockLib.class).stream().toList();
      Platform.runLater(() -> blockLibs.set(FXCollections.observableList(blockLibList)));
    }
  }
}

package org.tybaco.ui.main.services;

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

import javafx.collections.ObservableList;
import org.springframework.stereotype.Component;
import org.tybaco.ui.model.Project;

@Component
public class Projects {

  public final ObservableList<Project> projects = Project.newList();

  public void newProject() {
    var prefix = "Project ";
    var nextNum = projects.stream()
      .map(p -> p.name.get())
      .filter(name -> name.startsWith(prefix))
      .map(name -> name.substring(prefix.length()))
      .filter(s -> s.chars().allMatch(Character::isDigit))
      .mapToInt(Integer::parseInt)
      .max()
      .orElse(1);
    projects.add(new Project(prefix + nextNum));
  }
}

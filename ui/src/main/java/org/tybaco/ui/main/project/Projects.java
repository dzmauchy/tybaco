package org.tybaco.ui.main.project;

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

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.springframework.stereotype.Component;
import org.tybaco.ui.model.Project;

import java.math.BigInteger;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Projects implements AutoCloseable {

  public final ObservableList<Project> projects = Project.newList();
  private final TreeMap<String, Project> map = new TreeMap<>();

  public Projects() {
    projects.addListener(this::onChange);
  }

  public void newProject() {
    var prefix = "Project ";
    var pattern = Pattern.compile("Project (\\d++)");
    var nextNum = projects.stream()
      .map(p -> pattern.matcher(p.name.get()))
      .filter(Matcher::matches)
      .map(m -> new BigInteger(m.group(1)))
      .max(BigInteger::compareTo)
      .orElse(BigInteger.ZERO)
      .add(BigInteger.ONE);
    projects.add(new Project(prefix + nextNum));
  }

  public Project getById(String id) {
    return map.get(id);
  }

  public Optional<Project> byId(String id) {
    return Optional.ofNullable(getById(id));
  }

  private void onChange(ListChangeListener.Change<? extends Project> c) {
    while (c.next()) {
      if (c.wasRemoved()) {
        c.getRemoved().forEach(p -> map.remove(p.id));
      }
      if (c.wasAdded()) {
        c.getAddedSubList().forEach(p -> map.put(p.id, p));
      }
    }
  }

  @Override
  public void close() {
    map.clear();
  }
}

package org.tybaco.editors.font;

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

import org.junit.jupiter.api.*;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Tag("manual")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FontViewerTest {

  private final FontViewerFrame fontViewerFrame = new FontViewerFrame();

  @Test
  void show() throws Exception {
    EventQueue.invokeAndWait(() -> fontViewerFrame.setVisible(true));
  }

  @AfterAll
  void afterAll() throws Exception {
    while (true) {
      var r = new AtomicBoolean();
      EventQueue.invokeAndWait(() -> r.set(!fontViewerFrame.isShowing()));
      if (r.get()) break;
    }
    EventQueue.invokeLater(() -> fontViewerFrame.dispose());
  }
}

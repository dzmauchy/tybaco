package org.tybaco.ui.lib.window;

import java.awt.*;
import java.util.Optional;

public final class Windows {

  private Windows() {
  }

  public static <W extends Window> Optional<W> findWindow(Class<W> windowType) {
    for (var window : Window.getWindows()) {
      if (windowType.isInstance(window)) {
        return Optional.of(windowType.cast(window));
      }
    }
    return Optional.empty();
  }
}

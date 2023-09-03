package org.tybaco.ide.splash;

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

import java.util.prefs.Preferences;

import static java.util.prefs.Preferences.userNodeForPackage;

public class SplashStatus {

  private static final Preferences preferences = userNodeForPackage(SplashStatus.class);
  private static int step;
  static volatile boolean finished;

  private SplashStatus() {
  }

  static synchronized int maxStep() {
    var max = preferences.getInt("maxSteps", 100);
    return Math.max(step, max);
  }

  public static synchronized int incrementStep() {
    return ++step;
  }

  static synchronized int step() {
    return step;
  }

  public static synchronized void updateSplashStatus() {
    preferences.putInt("maxSteps", step);
    finished = true;
  }
}

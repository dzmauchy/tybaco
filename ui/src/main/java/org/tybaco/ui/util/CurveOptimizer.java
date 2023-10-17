package org.tybaco.ui.util;

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

import javafx.geometry.Bounds;

import java.awt.geom.CubicCurve2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import static java.awt.geom.CubicCurve2D.getFlatnessSq;
import static java.util.concurrent.atomic.AtomicIntegerFieldUpdater.newUpdater;

public final class CurveOptimizer {

  private static final int ITERATIONS_PER_TASK = 100;
  private static final int MAX_ITERATIONS = 1_000;
  private static final AtomicIntegerFieldUpdater<CurveOptimizer> ITERATIONS = newUpdater(CurveOptimizer.class, "iterations");

  private final float xs;
  private final float ys;
  private final float xe;
  private final float ye;
  private final Bounds[] restricted;
  private final double safeDist;
  private final ConcurrentSkipListMap<Float, CubicCurve2D.Float> best = new ConcurrentSkipListMap<>();
  private final float minX;
  private final float minY;
  private final float maxX;
  private final float maxY;

  private volatile int iterations;

  public CurveOptimizer(double xs, double ys, double xe, double ye, Bounds[] restricted, double safeDist) {
    this.xs = (float) xs;
    this.ys = (float) ys;
    this.xe = (float) xe;
    this.ye = (float) ye;
    this.restricted = restricted;
    this.safeDist = safeDist;
    var worldBounds = getWorldBounds();
    this.minX = worldBounds.x;
    this.minY = worldBounds.y;
    this.maxX = worldBounds.x + worldBounds.width;
    this.maxY = worldBounds.y + worldBounds.height;
  }

  private Rectangle2D.Float getWorldBounds() {
    float minX = 0f, maxX = 0f, minY = 0f, maxY = 0f;
    for (var b : restricted) {
      if (b.getMinX() < minX) minX = (float) b.getMinX();
      if (b.getMaxX() > maxX) maxX = (float) b.getMaxX();
      if (b.getMinY() < minY) minY = (float) b.getMinY();
      if (b.getMaxY() > maxY) maxY = (float) b.getMaxY();
    }
    return new Rectangle2D.Float(minX - 300f, minY - 300f, (maxX - minX) + 600f, (maxY - minY) + 600f);
  }

  public Optional<CubicCurve2D.Float> bestFit() {
    new Task(0L).fork().join();
    var entry = best.pollFirstEntry();
    return Optional.ofNullable(entry).map(Map.Entry::getValue);
  }

  private boolean notIntersects(CurveDivider divider) {
    for (var b : restricted) {
      if (divider.intersects(b, safeDist)) {
        return false;
      }
    }
    return true;
  }

  private float fitFunc(CurveDivider divider, float cx1, float cy1, float cx2, float cy2) {
    return (float) (getFlatnessSq(xs, ys, cx1, cy1, cx2, cy2, xe, ye) + divider.length());
  }

  private final class Task extends RecursiveAction {

    private final long seed;
    private final Random random;

    private Task(long seed) {
      this.seed = seed;
      this.random = new Random(seed);
    }

    @Override
    protected void compute() {
      var divider = CurveDividers.curveDivider(5);
      for (int i = 0; i < ITERATIONS_PER_TASK; i++) {
        var cx1 = random.nextFloat(minX, maxX);
        var cx2 = random.nextFloat(minX, maxX);
        var cy1 = random.nextFloat(minY, maxY);
        var cy2 = random.nextFloat(minY, maxY);
        divider.divide(xs, ys, cx1, cy1, cx2, cy2, xe, ye);
        if (notIntersects(divider)) {
          best.put(fitFunc(divider, cx1, cy1, cx2, cy2), new CubicCurve2D.Float(xs, ys, cx1, cy1, cx2, cy2, xe, ye));
        }
        if (ITERATIONS.incrementAndGet(CurveOptimizer.this) > MAX_ITERATIONS) return;
      }
      var f1 = new Task(1000L * seed + 1000L).fork();
      var f2 = new Task(1000L * seed + 2000L).fork();
      f1.join();
      f2.join();
    }
  }
}

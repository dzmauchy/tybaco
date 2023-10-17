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
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

import static java.awt.geom.CubicCurve2D.getFlatnessSq;
import static java.lang.Boolean.TRUE;

public final class CurveOptimizer {

  private static final float STEP = 30f;

  private final float xs;
  private final float ys;
  private final float xe;
  private final float ye;
  private final Bounds[] restricted;
  private final double safeDist;
  private final ConcurrentSkipListMap<Float, CubicCurve2D.Float> best = new ConcurrentSkipListMap<>();
  private final ConcurrentHashMap<Key, Boolean> passed = new ConcurrentHashMap<>(128, 0.75f, 16);
  private final float minX;
  private final float minY;
  private final float maxX;
  private final float maxY;

  private volatile boolean running = true;

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
    var w = (xe - xs) / 5f;
    var primordialTask = new Task(xs + w, ys, xe - w, ye).fork();
    while (!primordialTask.isDone()) {
      if (best.size() > 1) {
        running = false;
        return Optional.of(best.firstEntry().getValue());
      }
      LockSupport.parkNanos(1000L);
    }
    return Optional.empty();
  }

  private boolean notIntersects(CurveDivider divider) {
    for (var b : restricted) {
      if (divider.intersects(b, safeDist)) {
        return false;
      }
    }
    return true;
  }

  private record Key(float cx1, float cy1, float cx2, float cy2) {
  }

  private final class Task extends RecursiveAction {

    private final float cx1;
    private final float cy1;
    private final float cx2;
    private final float cy2;

    private Task(float cx1, float cy1, float cx2, float cy2) {
      this.cx1 = cx1;
      this.cy1 = cy1;
      this.cx2 = cx2;
      this.cy2 = cy2;
    }

    private float fitFunc(CurveDivider divider) {
      return (float) (getFlatnessSq(xs, ys, cx1, cy1, cx2, cy2, xe, ye) + divider.length());
    }

    private ForkJoinTask<Void> tryFork() {
      if (cx1 < minX || cx1 > maxX || cx2 < minX || cx2 > maxX || cy1 < minY || cy1 > maxY || cy2 < minY || cy2 > maxY) {
        return null;
      } else {
        return fork();
      }
    }

    @Override
    protected void compute() {
      if (passed.putIfAbsent(new Key(cx1, cy1, cx2, cy2), TRUE) != null) {
        return;
      }
      var divider = CurveDividers.curveDivider(5);
      divider.divide(xs, ys, cx1, cy1, cx2, cy2, xe, ye);
      if (notIntersects(divider)) {
        best.put(fitFunc(divider), new CubicCurve2D.Float(xs, ys, cx1, cy1, cx2, cy2, xe, ye));
      }
      if (running) {
        var f1 = new Task(cx1 + STEP, cy1, cx2, cy2).tryFork();
        var f2 = new Task(cx1 - STEP, cy1, cx2, cy2).tryFork();
        var f3 = new Task(cx1, cy1 + STEP, cx2, cy2).tryFork();
        var f4 = new Task(cx1, cy1 - STEP, cx2, cy2).tryFork();
        var f5 = new Task(cx1, cy1, cx2 + STEP, cy2).tryFork();
        var f6 = new Task(cx1, cy1, cx2 - STEP, cy2).tryFork();
        var f7 = new Task(cx1, cy1, cx2, cy2 + STEP).tryFork();
        var f8 = new Task(cx1, cy1, cx2, cy2 - STEP).tryFork();
        if (f1 != null) f1.join();
        if (f2 != null) f2.join();
        if (f3 != null) f3.join();
        if (f4 != null) f4.join();
        if (f5 != null) f5.join();
        if (f6 != null) f6.join();
        if (f7 != null) f7.join();
        if (f8 != null) f8.join();
      }
    }
  }
}

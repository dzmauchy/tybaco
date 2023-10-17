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
import org.jetbrains.annotations.NotNull;

import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.IntStream;

import static java.awt.geom.CubicCurve2D.getFlatnessSq;

public final class CurveOptimizer {

  private static final int PARALLELISM = 6;
  private static final int ORGANISMS = 16;
  private static final int ITERATIONS = 32;
  private static final float MUTATION_PROBABILITY = 0.4f;
  private static final float INTERSECTION_SCORE = 1e7f;

  private final float xs;
  private final float ys;
  private final float xe;
  private final float ye;
  private final Bounds[] restricted;
  private final double safeDist;
  private final float minX;
  private final float minY;
  private final float maxX;
  private final float maxY;

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
    return new Rectangle2D.Float(minX - 500f, minY - 500f, (maxX - minX) + 1000f, (maxY - minY) + 1000f);
  }

  public Optional<OptimizedCurve> bestFit() {
    return IntStream.range(0, PARALLELISM)
      .mapToObj(i -> new Task(i * 1_000_000L).fork())
      .toList()
      .stream()
      .map(ForkJoinTask::join)
      .sorted()
      .findFirst()
      .filter(c -> c.func < INTERSECTION_SCORE);
  }

  private boolean intersects(CurveDivider divider) {
    for (var b : restricted) {
      if (divider.intersects(b, safeDist)) {
        return true;
      }
    }
    return false;
  }

  private float fitFunc(CurveDivider divider, float cx1, float cy1, float cx2, float cy2) {
    var base = (float) (getFlatnessSq(xs, ys, cx1, cy1, cx2, cy2, xe, ye) + divider.length());
    return intersects(divider) ? base + INTERSECTION_SCORE : base;
  }

  private final class Task extends RecursiveTask<OptimizedCurve> {

    private final long seed;

    private Task(long seed) {
      this.seed = seed;
    }

    private float score(Organism organism, CurveDivider divider) {
      divider.divide(xs, ys, organism.cx1, organism.cy1, organism.cx2, organism.cy2, xe, ye);
      return fitFunc(divider, organism.cx1, organism.cy1, organism.cx2, organism.cy2);
    }

    private Organism mutate(Organism original, SplittableRandom random) {
      return random.nextFloat() < MUTATION_PROBABILITY ? randomOrganism(random) : original;
    }

    @Override
    protected OptimizedCurve compute() {
      var random = new SplittableRandom(seed);
      var divider = CurveDividers.curveDivider(5);
      var organisms = initialize(random, divider);
      for (int i = 0; i < ITERATIONS; i++) {
        for (int j = 1; j < ORGANISMS; j++) {
          var o = organisms[j];
          var mutated = mutate(o, random);
          if (mutated != o) {
            mutated.score = score(mutated, divider);
            organisms[j] = mutated;
          }
        }
        for (int j = 1; j < ORGANISMS; j++) {
          var male = organisms[random.nextInt(ORGANISMS)];
          var female = organisms[random.nextInt(ORGANISMS)];
          var child = male.crossover(female);
          child.score = score(child, divider);
          organisms[j] = child;
        }
        Arrays.sort(organisms);
      }
      var best = organisms[0];
      return new OptimizedCurve(xs, ys, best.cx1, best.cy1, best.cx2, best.cy2, xe, ye, best.score);
    }

    private Organism randomOrganism(SplittableRandom random) {
      return new Organism(
        random.nextFloat(minX, maxX),
        random.nextFloat(minY, maxY),
        random.nextFloat(minX, maxX),
        random.nextFloat(minY, maxY)
      );
    }

    private Organism[] initialize(SplittableRandom random, CurveDivider divider) {
      var organisms = new Organism[ORGANISMS];
      for (int i = 0; i < ORGANISMS; i++) {
        var o = randomOrganism(random);
        organisms[i] = o;
        o.score = score(o, divider);
      }
      return organisms;
    }
  }

  private static final class Organism implements Comparable<Organism> {

    private final float cx1;
    private final float cy1;
    private final float cx2;
    private final float cy2;
    private float score;

    private Organism(float cx1, float cy1, float cx2, float cy2) {
      this.cx1 = cx1;
      this.cy1 = cy1;
      this.cx2 = cx2;
      this.cy2 = cy2;
    }

    private Organism crossover(Organism that) {
      return new Organism(
        this.cx1,
        this.cy1,
        that.cx2,
        that.cy2
      );
    }

    @Override
    public int compareTo(@NotNull CurveOptimizer.Organism o) {
      return Float.compare(score, o.score);
    }
  }
}

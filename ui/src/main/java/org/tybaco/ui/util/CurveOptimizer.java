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
import java.util.stream.IntStream;

import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.NONNULL;
import static java.util.stream.StreamSupport.stream;

public final class CurveOptimizer {

  private static final int PARALLELISM = 4;
  private static final int ORGANISMS = 16;
  private static final int ITERATIONS = 8;
  private static final float INTERSECTION_SCORE = 1e7f;

  private final float xs;
  private final float ys;
  private final float xe;
  private final float ye;
  private final Spliterator<Bounds> restricted;
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
    this.restricted = Spliterators.spliterator(restricted, NONNULL | IMMUTABLE);
    this.safeDist = safeDist;
    var worldBounds = getWorldBounds(restricted);
    this.minX = worldBounds.x;
    this.minY = worldBounds.y;
    this.maxX = worldBounds.x + worldBounds.width;
    this.maxY = worldBounds.y + worldBounds.height;
  }

  private Rectangle2D.Float getWorldBounds(Bounds[] restricted) {
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
    return IntStream.range(0, PARALLELISM).parallel()
      .mapToObj(i -> new Task(i * 1_000_000L).compute())
      .sorted()
      .findFirst()
      .filter(c -> c.func < INTERSECTION_SCORE);
  }

  private boolean intersects(CurveDivider divider) {
    return stream(restricted, true).anyMatch(b -> divider.intersects(b, safeDist));
  }

  private final class Task {

    private final long seed;

    private Task(long seed) {
      this.seed = seed;
    }

    private float score(float cx1, float cy1, float cx2, float cy2, CurveDivider divider) {
      divider.divide(xs, ys, cx1, cy1, cx2, cy2, xe, ye);
      return (float) divider.length() + (intersects(divider) ? INTERSECTION_SCORE : 0f);
    }

    private OptimizedCurve compute() {
      var random = new SplittableRandom(seed);
      var divider = CurveDividers.curveDivider(5);
      var organisms = initialize(random, divider);
      for (int i = 0; i < ITERATIONS; i++) {
        for (int j = 1, l = ORGANISMS >>> 2; j < l; j++) {
          organisms[j] = mutate(random, divider, organisms[j]);
        }
        for (int j = 1; j < ORGANISMS; j++) {
          var maleIndex = random.nextInt(ORGANISMS);
          var femaleIndex = random.nextInt(ORGANISMS);
          if (maleIndex != femaleIndex) {
            organisms[j] = crossover(divider, organisms[maleIndex], organisms[femaleIndex]);
          } else {
            organisms[i] = randomOrganism(random, divider);
          }
        }
        Arrays.sort(organisms);
      }
      var best = organisms[0];
      return new OptimizedCurve(xs, ys, best.cx1, best.cy1, best.cx2, best.cy2, xe, ye, best.score);
    }

    private Organism crossover(CurveDivider divider, Organism male, Organism female) {
      var cx1 = (male.cx1 + female.cx1) / 2f;
      var cy1 = (male.cy1 + female.cy1) / 2f;
      var cx2 = (male.cx2 + female.cx2) / 2f;
      var cy2 = (male.cy2 + female.cy2) / 2f;
      return new Organism(cx1, cy1, cx2, cy2, score(cx1, cy1, cx2, cy2, divider));
    }

    private Organism mutate(SplittableRandom random, CurveDivider divider, Organism organism) {
      var cx1 = organism.cx1 + random.nextFloat(-300f, 300f);
      var cy1 = organism.cy1 + random.nextFloat(-300f, 300f);
      var cx2 = organism.cx2 + random.nextFloat(-300f, 300f);
      var cy2 = organism.cy2 + random.nextFloat(-300f, 300f);
      return new Organism(cx1, cy1, cx2, cy2, score(cx1, cy1, cx2, cy2, divider));
    }

    private Organism randomOrganism(SplittableRandom random, CurveDivider divider) {
      var cx1 = random.nextFloat(minX, maxX);
      var cy1 = random.nextFloat(minY, maxY);
      var cx2 = random.nextFloat(minX, maxX);
      var cy2 = random.nextFloat(minY, maxY);
      return new Organism(cx1, cy1, cx2, cy2, score(cx1, cy1, cx2, cy2, divider));
    }

    private Organism[] initialize(SplittableRandom random, CurveDivider divider) {
      var organisms = new Organism[ORGANISMS];
      for (int i = 0; i < ORGANISMS; i++) {
        organisms[i] = randomOrganism(random, divider);
      }
      return organisms;
    }
  }

  private record Organism(float cx1, float cy1, float cx2, float cy2, float score) implements Comparable<Organism> {
    @Override
    public int compareTo(@NotNull CurveOptimizer.Organism o) {
      return Float.compare(score, o.score);
    }
  }
}

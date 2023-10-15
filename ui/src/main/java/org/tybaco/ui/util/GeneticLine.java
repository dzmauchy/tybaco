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

import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class GeneticLine {

  private static final int ITERATIONS = 256;
  private static final int ORGANISMS = 16;
  private static final float MUTATION_PROBABILITY = 0.05f;
  private static final double M = 20d;
  private static final double INTERSECTION_M = M * M;
  public static final double SAFE_DIST = 3d;

  private final Bounds[] bounds;
  private final double xs;
  private final double ys;
  private final double xe;
  private final double ye;

  private final Organism[] organisms = new Organism[ORGANISMS * 2];

  public GeneticLine(Supplier<Stream<Bounds>> bounds, Bounds outBounds, Bounds inBounds) {
    this.bounds = bounds.get().toArray(Bounds[]::new);
    this.xs = outBounds.getMaxX() + SAFE_DIST;
    this.ys = outBounds.getCenterY();
    this.xe = inBounds.getMinX() - SAFE_DIST;
    this.ye = inBounds.getCenterY();
  }

  public LinkedList<CubicCurve2D.Double> produce() {
    var r = new Random(0L);
    initialize(r);
    for (int i = 0; i < ITERATIONS; i++) {
      for (int j = 0; j < ORGANISMS; j++) {
        var o = new Organism(r, organisms[r.nextInt(ORGANISMS)], organisms[r.nextInt(ORGANISMS)]);
        organisms[j + ORGANISMS] = o;
        o.value = value(o);
      }
      Arrays.sort(organisms);
    }
    return curves(organisms[0]);
  }

  private void initialize(Random r) {
    for (int i = 0; i < ORGANISMS; i++) {
      var o = new Organism(r);
      o.value = value(o);
      organisms[i] = o;
    }
    Arrays.sort(organisms, 0, ORGANISMS);
  }

  LinkedList<CubicCurve2D.Double> curves(Organism organism) {
    var list = new LinkedList<CubicCurve2D.Double>();
    var d = organism.data;
    var ax = xs + (d[2] * M);
    var ay = ys + (d[3] * M);
    var px = xe - (d[4] * M);
    var py = ye - (d[5] * M);
    list.addLast(new CubicCurve2D.Double(
      xs + SAFE_DIST, ys,
      xs + (d[0] * M), ys + (d[1] * M),
      ax, ay,
      (ax + px) / 2d, (ay + py) / 2d
    ));
    list.addLast(new CubicCurve2D.Double(
      (ax + px) / 2d, (ay + py) / 2d,
      px, py,
      xe - (d[6] * M), ye - (d[7] * M),
      xe - SAFE_DIST, ye
    ));
    return list;
  }

  double value(Organism organism) {
    var curves = curves(organism);
    CurveDivider.divide(6, curves);
    return curves.parallelStream()
      .mapToDouble(c -> {
        var l = new Line2D.Double(c.x1, c.y1, c.x2, c.y2);
        var normalizedLength = Math.hypot(l.x1 - l.x2, l.y1 - l.y2) / M;
        int intersections = 0;
        for (var b : bounds) {
          if (l.intersects(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight())) {
            intersections++;
          }
        }
        return normalizedLength + intersections * INTERSECTION_M + c.getFlatnessSq();
      })
      .sum();
  }

  static final class Organism implements Comparable<Organism> {

    private final byte[] data;
    private double value;

    private Organism(Random r, byte[] data) {
      this.data = data;
      mutate(r);
    }

    Organism(Random r) {
      data = new byte[8];
      r.nextBytes(data);
    }

    Organism(Random r, Organism o1, Organism o2) {
      o1 = new Organism(r, o1.data.clone());
      o2 = new Organism(r, o2.data.clone());
      var data = new byte[8];
      for (int i = 0; i < 8; i += 2) {
        data[i] = avg(o1.data[i], o2.data[i]);
        data[i + 1] = avg(o1.data[i + 1], o2.data[i + 1]);
      }
      this.data = data;
    }

    private byte avg(byte b1, byte b2) {
      return (byte) (((int) b1 + (int) b2) / 2);
    }

    void mutate(Random r) {
      if (r.nextFloat() < MUTATION_PROBABILITY) {
        r.nextBytes(data);
      }
    }

    @Override
    public int compareTo(@NotNull GeneticLine.Organism o) {
      return Double.compare(value, o.value);
    }
  }
}

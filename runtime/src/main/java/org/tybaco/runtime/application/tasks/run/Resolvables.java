package org.tybaco.runtime.application.tasks.run;

/*-
 * #%L
 * runtime
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

import org.tybaco.runtime.application.ResolvableObject;

import java.util.*;

public final class Resolvables {

  private static final int BUCKET_SIZE = 64;

  private final ResolvableObject[][] buckets;

  @SafeVarargs
  public Resolvables(List<? extends ResolvableObject>... lists) {
    var maxId = Arrays.stream(lists)
      .flatMap(List::stream)
      .mapToInt(ResolvableObject::id)
      .max()
      .orElse(-1);
    buckets = new ResolvableObject[Math.ceilDiv(maxId + 1, BUCKET_SIZE)][];
    for (var list : lists) {
      list.forEach(this::put);
    }
  }

  private void put(ResolvableObject value) {
    var key = value.id();
    var bucketIndex = key / BUCKET_SIZE;
    var bucket = buckets[bucketIndex];
    if (bucket == null) buckets[bucketIndex] = bucket = new ResolvableObject[BUCKET_SIZE];
    bucket[key % BUCKET_SIZE] = value;
  }

  public ResolvableObject get(int key) {
    var bucketIndex = key / BUCKET_SIZE;
    if (bucketIndex >= buckets.length) return null;
    var bucket = buckets[bucketIndex];
    return bucket == null ? null : bucket[key % BUCKET_SIZE];
  }

  @Override
  public String toString() {
    var map = new TreeMap<Integer, ResolvableObject>();
    int i = 0;
    for (var bucket : buckets) {
      if (bucket == null) {
        i += BUCKET_SIZE;
      } else {
        for (var v : bucket) {
          if (v != null) map.put(i++, v);
          else i++;
        }
      }
    }
    return map.toString();
  }
}

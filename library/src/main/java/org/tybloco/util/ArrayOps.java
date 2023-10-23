package org.tybloco.util;

/*-
 * #%L
 * library
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

import java.util.function.BiPredicate;
import java.util.function.Predicate;

public interface ArrayOps {

  static <E> boolean all(E[] array, Predicate<? super E> predicate) {
    for (var e : array) {
      if (!predicate.test(e)) {
        return false;
      }
    }
    return true;
  }

  static <E> boolean none(E[] array, Predicate<? super E> predicate) {
    for (var e : array) {
      if (predicate.test(e)) {
        return false;
      }
    }
    return true;
  }

  static <E> boolean any(E[] array, Predicate<? super E> predicate) {
    for (var e : array) {
      if (predicate.test(e)) {
        return true;
      }
    }
    return false;
  }

  static <E> boolean all(E[] a1, E[] a2, BiPredicate<E, E> predicate) {
    int l1 = a1.length, l2 = a2.length;
    if (l1 == l2) {
      for (int i = 0; i < l1; i++) {
        if (!predicate.test(a1[i], a2[i])) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  static <E> boolean none(E[] a1, E[] a2, BiPredicate<E, E> predicate) {
    int l1 = a1.length, l2 = a2.length;
    if (l1 == l2) {
      for (int i = 0; i < l1; i++) {
        if (predicate.test(a1[i], a2[i])) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  static <E> boolean any(E[] a1, E[] a2, BiPredicate<E, E> predicate) {
    int l1 = a1.length, l2 = a2.length;
    if (l1 == l2) {
      for (int i = 0; i < l1; i++) {
        if (predicate.test(a1[i], a2[i])) {
          return true;
        }
      }
    }
    return false;
  }
}

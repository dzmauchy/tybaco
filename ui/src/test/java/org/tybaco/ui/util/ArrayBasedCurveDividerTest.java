package org.tybaco.ui.util;

import org.junit.jupiter.api.Test;

import java.awt.geom.CubicCurve2D;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArrayBasedCurveDividerTest {

  @Test
  void divide2() {
    var divider = new ArrayBasedCurveDivider(1);
    assertEquals(16, divider.array.length);
    var c = new CubicCurve2D.Double(0, 0, 10, 10, 20, 30, 10, 2);
    divider.divide(c.x1, c.y1, c.ctrlx1, c.ctrly1, c.ctrlx2, c.ctrly2, c.x2, c.y2);
    var c1 = new CubicCurve2D.Double();
    var c2 = new CubicCurve2D.Double();
    c.subdivide(c1, c2);
    assertEquals(c1.x1, divider.array[0]);
    assertEquals(c1.y1, divider.array[1]);
    assertEquals(c1.ctrlx1, divider.array[2]);
    assertEquals(c1.ctrly1, divider.array[3]);
    assertEquals(c1.ctrlx2, divider.array[4]);
    assertEquals(c1.ctrly2, divider.array[5]);
    assertEquals(c1.x2, divider.array[6]);
    assertEquals(c1.y2, divider.array[7]);
    assertEquals(c2.x1, divider.array[8]);
    assertEquals(c2.y1, divider.array[9]);
    assertEquals(c2.ctrlx1, divider.array[10]);
    assertEquals(c2.ctrly1, divider.array[11]);
    assertEquals(c2.ctrlx2, divider.array[12]);
    assertEquals(c2.ctrly2, divider.array[13]);
    assertEquals(c2.x2, divider.array[14]);
    assertEquals(c2.y2, divider.array[15]);
  }
}

package org.tybaco.types.calc;

import java.lang.reflect.Type;
import java.util.Set;

public interface UnionType extends Type {
  Type[] getTypes();
  Set<Type> types();
}

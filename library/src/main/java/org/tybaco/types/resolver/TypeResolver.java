package org.tybaco.types.resolver;

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

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import static java.lang.String.join;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.lineSeparator;
import static java.util.Map.entry;
import static java.util.stream.Collectors.joining;
import static org.eclipse.jdt.core.JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS;
import static org.eclipse.jdt.core.JavaCore.COMPILER_PB_RAW_TYPE_REFERENCE;
import static org.eclipse.jdt.core.JavaCore.COMPILER_PB_UNUSED_IMPORT;
import static org.eclipse.jdt.core.JavaCore.COMPILER_PB_UNUSED_LOCAL;
import static org.eclipse.jdt.core.JavaCore.COMPILER_PB_UNUSED_PARAMETER;
import static org.eclipse.jdt.core.JavaCore.COMPILER_RELEASE;
import static org.eclipse.jdt.core.JavaCore.COMPILER_SOURCE;

public final class TypeResolver {

    private final String name;
    private final ASTParser parser;

    public TypeResolver(String projectName, String[] libraries, String[] sources) {
        name = projectName;
        parser = ASTParser.newParser(AST.JLS20);
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);
        parser.setStatementsRecovery(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setCompilerOptions(options());
        parser.setUnitName(name + ".java");
        parser.setEnvironment(libraries, sources, encodings(sources), true);
    }

    public TypeResolverResults resolve(Map<String, String> expressions) {
        var lines = new LinkedList<String>();
        var lineMap = new TreeMap<Integer, String>();
        lines.add("public class " + name + " {");
        lines.add("public void method() {");
        expressions.forEach((name, expr) -> {
            lineMap.put(lines.size(), name);
            lines.add("var " + name + " = ");
            expr.lines().forEach(lines::add);
            lines.add(";");
        });
        lines.add("}}");
        parser.setSource(join(lineSeparator(), lines).toCharArray());
        var results = new TypeResolverResults();
        if (parser.createAST(null) instanceof CompilationUnit cu) {
            cu.accept(new ASTVisitor() {
                @Override
                public void endVisit(VariableDeclarationFragment node) {
                    var var = node.getName().getIdentifier();
                    var type = node.resolveBinding().getType();
                    results.types.put(var, type);
                }
            });
            processProblems(results, cu, n -> {
                var entry = lineMap.floorEntry(n);
                return entry == null ? null : entry.getValue();
            });
        }
        return results;
    }

    public TypeResolverResults resolveTypes(Collection<String> types) {
        var lines = new ArrayList<String>(types.size() + 3);
        lines.add("public class " + name + " {");
        lines.add("public java.util.List<Class<?>> method() { return java.util.List.of(");
        lines.add(types.stream().map(t -> t + ".class").collect(joining("," + lineSeparator())));
        lines.add(");}}");
        parser.setSource(join(lineSeparator(), lines).toCharArray());
        var results = new TypeResolverResults();
        if (parser.createAST(null) instanceof CompilationUnit cu) {
            cu.accept(new ASTVisitor() {
                @Override
                public void endVisit(TypeLiteral node) {
                    if (node.getType() instanceof SimpleType st) {
                        var name = st.getName().getFullyQualifiedName();
                        var type = st.resolveBinding().getTypeDeclaration();
                        results.types.put(name, type);
                    } else if (node.getType() instanceof PrimitiveType pt) {
                        var name = pt.getPrimitiveTypeCode().toString();
                        var type = pt.resolveBinding().getTypeDeclaration();
                        results.types.put(name, type);
                    } else {
                        var logger = System.getLogger(TypeResolver.class.getName());
                        logger.log(ERROR, () -> "Unknown type literal: " + node);
                    }
                }
            });
            processProblems(results, cu, n -> {
                var line = lines.get(n - 1);
                var idx = line.indexOf(".class");
                return idx > 0 ? line.substring(0, idx) : null;
            });
        }
        return results;
    }

    private static Map<String, String> options() {
        return Map.ofEntries(
                entry(COMPILER_RELEASE, "enabled"),
                entry(COMPILER_SOURCE, "20"),
                entry(COMPILER_ANNOTATION_NULL_ANALYSIS, "enabled"),
                entry(COMPILER_PB_UNUSED_LOCAL, "ignore"),
                entry(COMPILER_PB_UNUSED_PARAMETER, "ignore"),
                entry(COMPILER_PB_UNUSED_IMPORT, "ignore"),
                entry(COMPILER_PB_RAW_TYPE_REFERENCE, "ignore")
        );
    }

    public static String format(IProblem problem) {
        var args = problem.getArguments();
        if (args == null || args.length == 0) {
            return problem.getMessage();
        }
        return MessageFormat.format(problem.getMessage(), (Object[]) args);
    }

    private static String[] encodings(String[] sources) {
        var encodings = new String[sources.length];
        Arrays.fill(encodings, "UTF-8");
        return encodings;
    }

    private static List<String> add(List<String> l, String e) {
        if (l == null) {
            return List.of(e);
        }
        return switch (l.size()) {
            case 1 -> List.of(l.get(0), e);
            case 2 -> List.of(l.get(0), l.get(1), e);
            case 3 -> List.of(l.get(0), l.get(1), l.get(2), e);
            case 4 -> List.of(l.get(0), l.get(1), l.get(2), l.get(3), e);
            default -> Stream.concat(l.stream(), Stream.of(e)).toList();
        };
    }

    private static void processProblems(TypeResolverResults results, CompilationUnit cu, IntFunction<String> nameFunc) {
        for (var problem : cu.getProblems()) {
            var name = nameFunc.apply(problem.getSourceLineNumber());
            if (name == null) {
                var logger = System.getLogger(TypeResolver.class.getName());
                logger.log(ERROR, () -> "Problem " + problem);
            } else {
                if (problem.isError()) {
                    results.errors.compute(name, (v, l) -> add(l, format(problem)));
                } else if (problem.isWarning()) {
                    results.warns.compute(name, (v, l) -> add(l, format(problem)));
                } else {
                    results.infos.compute(name, (v, l) -> add(l, format(problem)));
                }
            }
        }
    }
}

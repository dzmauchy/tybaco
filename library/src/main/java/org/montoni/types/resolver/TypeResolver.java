package org.montoni.types.resolver;

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
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import static java.lang.System.Logger.Level.ERROR;
import static org.eclipse.jdt.core.JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS;
import static org.eclipse.jdt.core.JavaCore.COMPILER_PB_UNUSED_LOCAL;
import static org.eclipse.jdt.core.JavaCore.COMPILER_RELEASE;
import static org.eclipse.jdt.core.JavaCore.COMPILER_SOURCE;

public final class TypeResolver {

    private final String name;
    private final ASTParser parser;

    public TypeResolver(String name, String[] libraries, String[] sources) {
        this.name = name;
        parser = ASTParser.newParser(AST.JLS20);
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);
        parser.setStatementsRecovery(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setCompilerOptions(Map.of(
                COMPILER_RELEASE, "enabled",
                COMPILER_SOURCE, "20",
                COMPILER_ANNOTATION_NULL_ANALYSIS, "enabled",
                COMPILER_PB_UNUSED_LOCAL, "ignore"
        ));
        parser.setUnitName(name + ".java");
        parser.setEnvironment(libraries, sources, encodings(sources), true);
    }

    public TypeResolverResults resolve(Map<String, String> expressions) {
        var lines = new ArrayList<String>(expressions.size() + 3);
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
        parser.setSource(String.join("\n", lines).toCharArray());
        var results = new TypeResolverResults();
        if (parser.createAST(null) instanceof CompilationUnit cu) {
            cu.accept(new ASTVisitor() {
                @Override
                public boolean visit(VariableDeclarationFragment node) {
                    var var = node.getName().getIdentifier();
                    var type = node.resolveBinding().getType();
                    results.types.put(var, Types.from(type));
                    return true;
                }
            });
            for (var problem : cu.getProblems()) {
                var lineNo = problem.getSourceLineNumber();
                var entry = lineMap.floorEntry(lineNo);
                if (entry == null) {
                    var logger = System.getLogger(TypeResolver.class.getName());
                    logger.log(ERROR, () -> "Problem " + problem);
                } else {
                    var var = entry.getValue();
                    var msg = format(problem);
                    if (problem.isError()) {
                        results.errors.compute(var, (v, l) -> add(l, msg));
                    } else if (problem.isWarning()) {
                        results.warns.compute(var, (v, l) -> add(l, msg));
                    } else {
                        results.infos.compute(var, (v, l) -> add(l, msg));
                    }
                }
            }
        }
        return results;
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
}

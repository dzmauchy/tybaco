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

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.join;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;
import static java.lang.System.lineSeparator;
import static org.tybaco.types.resolver.TypeResolverResults.add;

public final class TypeResolver {

    private final EcjHelper helper = new EcjHelper();
    private final String name;
    private final Compiler compiler;

    public TypeResolver(String projectName, String[] libraries) {
        name = projectName;
        compiler = helper.compiler(libraries);
    }

    public TypeResolverResults resolve(Map<String, String> expressions, String... additionalLines) {
        var lines = new ArrayList<String>();
        lines.add("public class " + name + " {");
        lines.add("public void method() {");
        expressions.forEach((name, expr) -> {
            helper.registerLine(lines.size(), name);
            lines.add("var " + name + " =");
            expr.lines().forEach(lines::add);
            lines.add(";");
        });
        lines.add("}}");
        lines.addAll(List.of(additionalLines));
        var source = join(lineSeparator(), lines);
        var u = new CompilationUnit(source.toCharArray(), name + ".java", "UTF-8");
        try {
            var r = compiler.resolve(u, true, true, false);
            var results = new TypeResolverResults(r.scope);
            var problems = r.compilationResult.getProblems();
            if (problems != null) {
                for (var problem : problems) {
                    var var = helper.name(problem.getSourceLineNumber());
                    if (var == null) {
                        var logger = System.getLogger(getClass().getName());
                        if (problem.isError()) logger.log(ERROR, "{0}", formatProblem(problem, lines));
                        else if (problem.isWarning()) logger.log(WARNING, "{0}", formatProblem(problem, lines));
                        else logger.log(INFO, "{0}", formatProblem(problem, lines));
                    } else {
                        var p = formatProblem(problem, lines);
                        if (problem.isError()) {
                            results.errors.compute(var, (k, o) -> add(o, p));
                        } else if (problem.isWarning()) {
                            results.warns.compute(var, (k, o) -> add(o, p));
                        } else {
                            results.infos.compute(var, (k, o) -> add(o, p));
                        }
                    }
                }
            }
            for (var ct : r.types) {
                if (!name.equals(new String(ct.name))) {
                    continue;
                }
                ct.traverse(new ASTVisitor() {
                    @Override
                    public void endVisit(LocalDeclaration localDeclaration, BlockScope scope) {
                        var var = String.valueOf(localDeclaration.name);
                        results.types.put(var, localDeclaration.binding.type);
                    }
                }, r.scope);
            }
            return results;
        } finally {
            helper.reset();
        }
    }

    private String formatProblem(CategorizedProblem problem, List<String> lines) {
        var line = lines.get(problem.getSourceLineNumber() - 1);
        var builder = new StringBuffer()
                .append('[')
                .append(problem.getSourceLineNumber())
                .append(']')
                .append(' ');
        var args = problem.getArguments();
        if (args == null || args.length == 0) {
            builder.append(problem.getMessage());
        } else {
            var fmt = new MessageFormat(problem.getMessage());
            fmt.format(problem.getArguments(), builder, null);
        }
        builder.append(" {").append(line).append('}');
        return builder.toString();
    }
}

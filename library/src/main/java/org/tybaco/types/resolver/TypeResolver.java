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

import lombok.extern.java.Log;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

import java.io.CharArrayWriter;
import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.util.logging.Level.WARNING;
import static org.tybaco.types.resolver.ResolvedTypes.add;

@Log
public final class TypeResolver implements AutoCloseable {

    private final String name;
    private final Compiler compiler;

    public TypeResolver(String projectName, String[] libraries) {
        name = projectName;
        compiler = new EcjHelper().compiler(libraries);
    }

    public ResolvedTypes resolve(Map<String, String> expressions, String... additionalLines) {
        if (compiler.unitsToProcess != null) compiler.reset();
        var decls = new ConcurrentLinkedQueue<LocalDeclaration>();
        var u = new CompilationUnit(code(expressions, additionalLines), name + ".java", "UTF-8", null, true, null);
        var r = compiler.resolve(u, true, true, false);
        var results = new ResolvedTypes(r);
        for (var ct : r.types) {
            ct.traverse(new ASTVisitor() {
                @Override
                public void endVisit(LocalDeclaration localDeclaration, BlockScope scope) {
                    var var = String.valueOf(localDeclaration.name);
                    if (expressions.containsKey(var)) {
                        results.types.put(var, localDeclaration.binding.type);
                        decls.add(localDeclaration);
                    }
                }
            }, r.scope);
        }
        var problems = r.compilationResult.getProblems();
        if (problems == null) return results;

        PROBLEMS:
        for (var p : problems) {
            for (var decl : decls) {
                if (p.getSourceStart() >= decl.sourceStart && p.getSourceEnd() <= decl.sourceEnd) {
                    var var = String.valueOf(decl.name);
                    var map = p.isError() ? results.errors : p.isWarning() ? results.warns : results.infos;
                    map.compute(var, (k, o) -> add(o, formatProblem(p)));
                    continue PROBLEMS;
                }
            }
            log.log(WARNING, "{0}", formatProblem(p));
        }
        return results;
    }

    private char[] code(Map<String, String> expressions, String... additionalLines) {
        var charStream = new CharArrayWriter(512)
                .append("public class ")
                .append(name)
                .append(" {").append(System.lineSeparator())
                .append("public void method() {").append(System.lineSeparator());
        expressions.forEach((name, expr) -> charStream
                .append("var ")
                .append(name)
                .append(" = ")
                .append(expr)
                .append(';').append(System.lineSeparator())
        );
        charStream.append("}}").append(System.lineSeparator());
        for (var line : additionalLines) charStream.append(line).append(System.lineSeparator());
        return charStream.toCharArray();
    }

    private String formatProblem(CategorizedProblem problem) {
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
        return builder.toString();
    }

    @Override
    public void close() {
        compiler.reset();
    }
}

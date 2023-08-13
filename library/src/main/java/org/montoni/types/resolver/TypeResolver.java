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

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.montoni.types.model.Type;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.eclipse.jdt.core.JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS;
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
                COMPILER_ANNOTATION_NULL_ANALYSIS, "enabled"
        ));
        parser.setUnitName(name + ".java");
        parser.setEnvironment(libraries, sources, encodings(sources), true);
    }

    public Map<String, Type> resolve(List<Entry<String, String>> expressions) {
        var builder = new StringBuilder("public class ")
                .append(name)
                .append("{\n")
                .append("public void method() {\n");
        expressions.forEach(e ->
                builder.append("var ").append(e.getKey()).append(" = ").append(e.getValue()).append(";\n")
        );
        builder.append("}}");
        parser.setSource(builder.toString().toCharArray());
        if (parser.createAST(null) instanceof CompilationUnit cu) {
            var result = new HashMap<String, Type>();
            cu.accept(new ASTVisitor() {
                @Override
                public boolean visit(VariableDeclarationFragment node) {
                    var var = node.getName().getIdentifier();
                    var type = node.resolveBinding().getType();
                    result.put(var, Types.from(type));
                    return true;
                }
            });
            return result;
        } else {
            return Map.of();
        }
    }

    private static String[] encodings(String[] sources) {
        var encodings = new String[sources.length];
        Arrays.fill(encodings, "UTF-8");
        return encodings;
    }
}

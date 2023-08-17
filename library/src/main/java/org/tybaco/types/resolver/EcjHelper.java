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

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.Locale.US;
import static org.eclipse.jdt.internal.compiler.batch.FileSystem.getClasspath;
import static org.eclipse.jdt.internal.compiler.batch.FileSystem.getJrtClasspath;
import static org.eclipse.jdt.internal.compiler.impl.CompilerOptions.releaseToJDKLevel;

final class EcjHelper implements IErrorHandlingPolicy, ICompilerRequestor {

    private static final String JAVA_VERSION = "20";

    private final StringWriter writer = new StringWriter();
    private final PrintWriter printWriter = new PrintWriter(writer, true);
    private final TreeMap<Integer, String> lines = new TreeMap<>();
    private final ConcurrentLinkedQueue<CompilationResult> results = new ConcurrentLinkedQueue<>();

    @Override
    public boolean proceedOnErrors() {
        return true;
    }

    @Override
    public boolean stopOnFirstError() {
        return false;
    }

    @Override
    public boolean ignoreAllErrors() {
        return false;
    }

    @Override
    public void acceptResult(CompilationResult result) {
        results.add(result);
    }

    public void reset() {
        results.clear();
        writer.getBuffer().setLength(0);
        writer.getBuffer().trimToSize();
        lines.clear();
    }

    public Stream<String> lines() {
        return Pattern.compile("\\n")
                .splitAsStream(writer.getBuffer())
                .map(String::trim)
                .filter(s -> !s.isEmpty());
    }

    public Stream<CompilationResult> results() {
        return results.stream();
    }

    public Compiler compiler(String[] libraries) {
        return new Compiler(fileSystem(libraries), this, options(), this, new DefaultProblemFactory(US), printWriter, null);
    }

    void registerLine(int lineIndex, String name) {
        lines.put(lineIndex + 1, name);
    }

    String name(int line) {
        var entry = lines.floorEntry(line);
        return entry == null ? null : entry.getValue();
    }

    private FileSystem fileSystem(String[] libraries) {
        var cps = Stream.concat(
                Stream.of(libraries).map(lib -> getClasspath(lib, "UTF-8", null, null, JAVA_VERSION)),
                Stream.of(getJrtClasspath(System.getProperty("java.home"), "UTF-8", null, null))
        ).toArray(FileSystem.Classpath[]::new);
        return new FileSystem(cps, null, true) {
        };
    }

    private CompilerOptions options() {
        var opts = new CompilerOptions();
        opts.generateClassFiles = false;
        opts.sourceLevel = releaseToJDKLevel(JAVA_VERSION);
        opts.complianceLevel = opts.sourceLevel;
        opts.targetJDK = opts.sourceLevel;
        opts.originalComplianceLevel = opts.sourceLevel;
        opts.originalSourceLevel = opts.sourceLevel;
        opts.generateGenericSignatureForLambdaExpressions = true;
        opts.performMethodsFullRecovery = true;
        opts.performStatementsRecovery = true;
        opts.preserveAllLocalVariables = true;
        opts.processAnnotations = false;
        opts.maxProblemsPerUnit = Integer.MAX_VALUE;
        opts.analyseResourceLeaks = false;
        opts.inheritNullAnnotations = true;
        opts.verbose = false;
        opts.defaultEncoding = "UTF-8";
        return opts;
    }
}

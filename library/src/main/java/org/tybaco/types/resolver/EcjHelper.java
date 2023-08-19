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
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import static java.util.Locale.US;
import static org.eclipse.jdt.internal.compiler.batch.FileSystem.getClasspath;
import static org.eclipse.jdt.internal.compiler.batch.FileSystem.getJrtClasspath;
import static org.eclipse.jdt.internal.compiler.impl.CompilerOptions.releaseToJDKLevel;

@Log
final class EcjHelper implements IErrorHandlingPolicy, ICompilerRequestor {

    private static final String JAVA_VERSION = "20";

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
    }

    public Stream<CompilationResult> results() {
        return results.stream();
    }

    public Compiler compiler(String[] libraries) {
        var problemFactory = new DefaultProblemFactory(US);
        var fs = fileSystem(libraries);
        var compiler = new Compiler(fs, this, options(), this, problemFactory, nullPrintWriter(), null) {
            @Override
            public void reset() {
                super.reset();
                fs.cleanup();
            }
        };
        compiler.useSingleThread = true;
        return compiler;
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
        opts.inheritNullAnnotations = false;
        opts.verbose = false;
        opts.defaultEncoding = "UTF-8";
        return opts;
    }

    private PrintWriter nullPrintWriter() {
        return new PrintWriter(new Writer() {
            @Override public void write(char[] cbuf, int off, int len) {}
            @Override public void flush() {}
            @Override public void close() {}
        }, false) {
            @Override public void write(int c) {}
            @Override public void write(String s) {}
            @Override public void write(char[] buf) {}
            @Override public void write(String s, int off, int len) {}
            @Override public void write(char[] buf, int off, int len) {}
            @Override public PrintWriter append(char c) {return this;}
            @Override public PrintWriter append(CharSequence csq) {return this;}
            @Override public PrintWriter append(CharSequence csq, int start, int end) {return this;}
            @Override public void println(int x) {}
            @Override public void println(char x) {}
            @Override public void println(float x) {}
            @Override public void println(long x) {}
            @Override public void println(char[] x) {}
            @Override public void println(double x) {}
            @Override public void println(Object x) {}
            @Override public void println(String x) {}
            @Override public void println(boolean x) {}
            @Override public void print(boolean b) {}
            @Override public void print(char c) {}
            @Override public void print(int i) {}
            @Override public void print(long l) {}
            @Override public void print(float f) {}
            @Override public void print(double d) {}
            @Override public void print(char[] s) {}
            @Override public void print(String s) {}
            @Override public void print(Object obj) {}
            @Override public void println() {}
            @Override public PrintWriter printf(String format, Object... args) {return this;}
            @Override public PrintWriter printf(Locale l, String format, Object... args) {return this;}
            @Override public PrintWriter format(String format, Object... args) {return this;}
            @Override public PrintWriter format(Locale l, String format, Object... args) {return this;}
            @Override public boolean checkError() {return false;}
            @Override public void flush() {}
            @Override public void close() {}
            @Override public String toString() {return "NULL";}
        };
    }
}

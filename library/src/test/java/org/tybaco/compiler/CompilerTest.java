package org.tybaco.compiler;

import com.sun.source.util.JavacTask;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.*;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.US;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("slow")
class CompilerTest {

  @TempDir private Path source;
  @TempDir private Path classes;
  @TempDir private Path srcOut;

  @Test
  void hugeMethod() throws IOException {
    var compiler = ToolProvider.getSystemJavaCompiler();
    var diag = new DiagnosticCollector<JavaFileObject>();
    var fileManager = compiler.getStandardFileManager(diag, US, UTF_8);
    fileManager.setLocationFromPaths(StandardLocation.SOURCE_PATH, List.of(source));
    fileManager.setLocationFromPaths(StandardLocation.CLASS_OUTPUT, List.of(classes));
    fileManager.setLocationFromPaths(StandardLocation.SOURCE_OUTPUT, List.of(srcOut));
    var code = """
      public class X {
        public void x() {
          %s
        }
      }
      """.formatted(IntStream.range(0, 10_000)
      .mapToObj("    var a%d = java.util.List.of(1, 1.0);"::formatted)
      .collect(Collectors.joining(System.lineSeparator()))
    );
    var sourceFile = source.resolve("X.java");
    Files.writeString(sourceFile, code, UTF_8);
    var writer = new StringWriter();
    var task = (JavacTask) compiler.getTask(
      writer,
      fileManager,
      diag,
      List.of("--release", "21"),
      List.of(),
      fileManager.getJavaFileObjects(sourceFile)
    );
    var units = StreamSupport.stream(task.parse().spliterator(), false).toList();
    var elements = StreamSupport.stream(task.analyze().spliterator(), false).toList();
    assertEquals(1, units.size());
    assertEquals(1, elements.size());
  }
}

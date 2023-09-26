package org.tybaco.annotations;

/*-
 * #%L
 * annotations
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

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import java.util.*;

import static javax.tools.StandardLocation.SOURCE_PATH;

public abstract class AbstractFileProcessor implements Processor {

  private Filer filer;
  private Messager messager;

  @Override
  public Set<String> getSupportedOptions() {
    return Collections.emptySet();
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.RELEASE_21;
  }

  protected abstract String annotation();

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Collections.singleton(annotation());
  }

  @Override
  public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
    return Collections.emptyList();
  }

  @Override
  public void init(ProcessingEnvironment processingEnv) {
    filer = processingEnv.getFiler();
    messager = processingEnv.getMessager();
  }

  protected abstract List<ProcessedFile> process(String code, String name);

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (var element : roundEnv.getElementsAnnotatedWithAny(annotations.toArray(TypeElement[]::new))) {
      if (element instanceof TypeElement te && te.getEnclosingElement() instanceof PackageElement pe) {
        try {
          var sourceFile = filer.getResource(SOURCE_PATH, pe.getQualifiedName(), te.getSimpleName() + ".java");
          var contents = sourceFile.getCharContent(false);
          var processed = process(contents.toString(), te.getSimpleName().toString());
          for (var f : processed) {
            var outFile = filer.createSourceFile(pe.getQualifiedName() + "." + f.name());
            try (var w = outFile.openWriter()) {
              w.write(f.contents());
            }
          }
        } catch (Throwable e) {
          e.printStackTrace(System.err);
          messager.printError(e.getMessage(), element);
          return false;
        }
      }
    }
    return true;
  }
}

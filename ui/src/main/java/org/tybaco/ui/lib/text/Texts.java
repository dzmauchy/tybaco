package org.tybaco.ui.lib.text;

/*-
 * #%L
 * ui
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

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Stream.concat;
import static javafx.beans.binding.Bindings.createStringBinding;
import static org.tybaco.logging.Log.info;
import static org.tybaco.logging.Log.warn;
import static org.tybaco.xml.Xml.*;

public final class Texts {

  private static final Preferences PREFERENCES = Preferences.userNodeForPackage(Texts.class);
  private static final SimpleObjectProperty<Locale> LOCALE = new SimpleObjectProperty<>(Texts.class, "locale", defaultLocale());
  private static final TreeMap<Locale, Map<String, String>> TEXTS = loadData("l10n/texts.xml");
  private static final TreeMap<Locale, Map<String, String>> MESSAGES = loadData("l10n/messages.xml");

  static {
    PREFERENCES.addPreferenceChangeListener(ev -> {
      if ("locale".equals(ev.getKey())) {
        var newLocale = ev.getNewValue() == null ? Locale.getDefault() : Locale.forLanguageTag(ev.getNewValue());
        Platform.runLater(() -> LOCALE.set(newLocale));
      }
    });
  }

  private Texts() {
  }

  private static Locale defaultLocale() {
    var localeFromPrefs = PREFERENCES.get("locale", "");
    if (!localeFromPrefs.isEmpty()) {
      return Locale.forLanguageTag(localeFromPrefs);
    }
    return Locale.getDefault();
  }

  private static String key(String key, TreeMap<Locale, Map<String, String>> bundles) {
    return key(LOCALE.get(), key, bundles);
  }

  private static String key(Locale locale, String key, TreeMap<Locale, Map<String, String>> bundles) {
    if (key == null) {
      return null;
    }
    try {
      var map = bundles.get(locale);
      if (map == null) {
        if (!locale.getVariant().isEmpty()) {
          return key(Locale.of(locale.getLanguage(), locale.getCountry()), key, bundles);
        } else if (!locale.getCountry().isEmpty()) {
          return key(Locale.of(locale.getLanguage()), key, bundles);
        } else {
          map = Map.of();
        }
      }
      var v = map.get(key);
      return v == null ? key : v;
    } catch (RuntimeException e) {
      warn(Texts.class, "Unable to get " + key, e);
      return key;
    }
  }

  private static String fmt(String key, Supplier<Object[]> args) {
    if (key == null) {
      return null;
    }
    try {
      return key(key, TEXTS).formatted(args.get());
    } catch (RuntimeException e) {
      warn(Texts.class, "Unable to format " + key, e);
      return key;
    }
  }

  private static String msg(String key, Supplier<Object[]> args) {
    if (key == null) {
      return null;
    }
    try {
      return MessageFormat.format(key(key, MESSAGES), args.get());
    } catch (RuntimeException e) {
      warn(Texts.class, "Unable to format " + key, e);
      return key;
    }
  }

  public static void setLocale(Locale locale) {
    if (locale == null) {
      PREFERENCES.remove("locale");
      return;
    }
    PREFERENCES.put("locale", locale.toLanguageTag());
  }

  public static Locale getLocale() {
    return Locale.forLanguageTag(PREFERENCES.get("locale", Locale.getDefault().toLanguageTag()));
  }

  public static StringBinding text(String text) {
    return createStringBinding(() -> key(text, TEXTS), LOCALE);
  }

  public static StringBinding text(String format, Object... args) {
    return createStringBinding(() -> fmt(format, () -> values(args)), observables(args));
  }

  public static StringBinding msg(String msg) {
    return createStringBinding(() -> key(msg, MESSAGES), LOCALE);
  }

  public static StringBinding msg(String msg, Object... args) {
    return createStringBinding(() -> msg(msg, () -> values(args)), observables(args));
  }

  private static Observable[] observables(Object... args) {
    return concat(Stream.of(LOCALE), stream(args).filter(Observable.class::isInstance).map(Observable.class::cast)).toArray(Observable[]::new);
  }

  private static Object[] values(Object... args) {
    return stream(args).map(a -> a instanceof ObservableValue<?> o ? o.getValue() : a).toArray();
  }

  private static TreeMap<Locale, Map<String, String>> loadData(String file) {
    var url = Thread.currentThread().getContextClassLoader().getResource(file);
    if (url == null) {
      throw new NoSuchElementException(file);
    }
    var crudeMap = loadFrom(url, schema("l10n/l10n.xsd"), root -> elementsByTag(root, "key")
      .flatMap(key -> {
        var k = key.getAttribute("value");
        return elementsByTag(key, "val")
          .map(v -> {
            var lang = v.getAttribute("lang");
            var val = v.getTextContent();
            return new String[] {lang, k, val};
          });
      })
      .collect(Collectors.groupingBy(a -> a[0], Collectors.toMap(a -> a[1], a -> a[2], (v1, v2) -> v2)))
    );
    var result = crudeMap.entrySet().stream().collect(Collectors.toMap(
      e -> Locale.forLanguageTag(e.getKey()),
      e -> Map.copyOf(e.getValue()),
      (m1, m2) -> m2,
      () -> new TreeMap<>(Comparator
        .comparing(Locale::getLanguage)
        .thenComparing(Locale::getCountry)
        .thenComparing(Locale::getVariant)
      )
    ));
    info(Texts.class, "Loaded {0}: locales = {1}", file, result.keySet());
    return result;
  }
}

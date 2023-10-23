package org.tybloco.editors.text;

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
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Supplier;
import java.util.logging.*;
import java.util.prefs.Preferences;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Stream.concat;
import static javafx.beans.binding.Bindings.createStringBinding;
import static org.tybloco.logging.Log.warn;
import static org.tybloco.xml.Xml.*;

public final class Texts {

  private static final Preferences PREFERENCES = Preferences.userNodeForPackage(Texts.class);
  private static final SimpleObjectProperty<Locale> LOCALE = new SimpleObjectProperty<>(Texts.class, "locale", defaultLocale());
  private static final WeakHashMap<ClassLoader, ConcurrentSkipListMap<Locale, Map<String, String>>> TEXTS = new WeakHashMap<>();
  private static final WeakHashMap<ClassLoader, ConcurrentSkipListMap<Locale, Map<String, String>>> MESSAGES = new WeakHashMap<>();
  private static final Supplier<Object[]> EMPTY_ARGS = () -> null;

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

  private static String key(Locale locale, String key, ConcurrentSkipListMap<Locale, Map<String, String>> bundles) {
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

  private static String fmt(ClassLoader classLoader, String key, Supplier<Object[]> args) {
    if (key == null) {
      return null;
    }
    final ConcurrentSkipListMap<Locale, Map<String, String>> texts;
    synchronized (TEXTS) {
      texts = TEXTS.computeIfAbsent(classLoader, Texts::texts);
    }
    var arguments = args.get();
    if (arguments == null || arguments.length == 0) return key(LOCALE.get(), key, texts);
    try {
      return key(LOCALE.get(), key, texts).formatted(arguments);
    } catch (RuntimeException e) {
      warn(Texts.class, "Unable to format " + key, e);
      return key;
    }
  }

  private static String msg(ClassLoader classLoader, String key, Supplier<Object[]> args) {
    if (key == null) {
      return null;
    }
    final ConcurrentSkipListMap<Locale, Map<String, String>> messages;
    synchronized (MESSAGES) {
      messages = MESSAGES.computeIfAbsent(classLoader, Texts::messages);
    }
    var arguments = args.get();
    if (arguments == null || arguments.length == 0) return key(LOCALE.get(), key, messages);
    try {
      return MessageFormat.format(key(LOCALE.get(), key, messages), arguments);
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

  public static StringBinding text(ClassLoader classLoader, String text) {
    return createStringBinding(() -> fmt(classLoader, text, EMPTY_ARGS), LOCALE);
  }

  public static StringBinding text(ClassLoader classLoader, String format, Object... args) {
    return createStringBinding(() -> fmt(classLoader, format, () -> values(args)), observables(args));
  }

  public static StringBinding text(String text) {
    return text(Thread.currentThread().getContextClassLoader(), text);
  }

  public static StringBinding text(String format, Object... args) {
    return text(Thread.currentThread().getContextClassLoader(), format, args);
  }

  public static StringBinding msg(ClassLoader classLoader, String msg) {
    return createStringBinding(() -> msg(classLoader, msg, EMPTY_ARGS), LOCALE);
  }

  public static StringBinding msg(ClassLoader classLoader, String msg, Object... args) {
    return createStringBinding(() -> msg(classLoader, msg, () -> values(args)), observables(args));
  }

  public static StringBinding msg(String msg) {
    return msg(Thread.currentThread().getContextClassLoader(), msg);
  }

  public static StringBinding msg(String msg, Object... args) {
    return msg(Thread.currentThread().getContextClassLoader(), msg, args);
  }

  private static Observable[] observables(Object... args) {
    return concat(Stream.of(LOCALE), stream(args).filter(Observable.class::isInstance).map(Observable.class::cast)).toArray(Observable[]::new);
  }

  private static Object[] values(Object... args) {
    return stream(args).map(a -> a instanceof ObservableValue<?> o ? o.getValue() : a).toArray();
  }

  private static ConcurrentSkipListMap<Locale, Map<String, String>> texts(ClassLoader classLoader) {
    var map = new ConcurrentSkipListMap<Locale, Map<String, String>>(localeComparator());
    load(classLoader, "l10n/texts.xml", map);
    return map;
  }

  private static ConcurrentSkipListMap<Locale, Map<String, String>> messages(ClassLoader classLoader) {
    var map = new ConcurrentSkipListMap<Locale, Map<String, String>>(localeComparator());
    load(classLoader, "l10n/messages.xml", map);
    return map;
  }

  public static void load(ClassLoader classLoader, String resource, ConcurrentSkipListMap<Locale, Map<String, String>> map) {
    classLoader.resources(resource).parallel().forEach(url ->
      loadFrom(url, schema("l10n/l10n.xsd"), root -> elementsByTag(root, "key")).forEach(key -> {
        var k = key.getAttribute("value");
        elementsByTag(key, "val").forEach(v -> {
          var lang = v.getAttribute("lang");
          var val = v.getTextContent();
          try {
            var locale = Locale.forLanguageTag(lang);
            map.computeIfAbsent(locale, kv -> new ConcurrentSkipListMap<>()).put(k, val);
          } catch (RuntimeException e) {
            var logger = Logger.getLogger("l10n");
            var record = new LogRecord(Level.WARNING, "Unable to get {0}.{1}");
            record.setThrown(e);
            record.setParameters(new Object[]{lang, k});
            logger.log(record);
          }
        });
      })
    );
  }

  private static Comparator<Locale> localeComparator() {
    return Comparator
      .comparing(Locale::getLanguage)
      .thenComparing(Locale::getCountry)
      .thenComparing(Locale::getVariant);
  }
}

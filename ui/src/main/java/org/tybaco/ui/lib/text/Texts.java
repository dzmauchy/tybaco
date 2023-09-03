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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.logging.Level.WARNING;
import static java.util.stream.Stream.concat;
import static javafx.beans.binding.Bindings.createStringBinding;

public class Texts {

  private static final Logger LOGGER = Logger.getLogger("Texts");
  private static final Preferences PREFERENCES = Preferences.userNodeForPackage(Texts.class);
  private static final SimpleObjectProperty<Locale> LOCALE = new SimpleObjectProperty<>(Texts.class, "locale", defaultLocale());
  private static final ConcurrentHashMap<Locale, Locale> NORMALIZED_LOCALES = new ConcurrentHashMap<>(16, 0.5f);
  private static final ConcurrentHashMap<Locale, Properties> TEXTS_BUNDLES = new ConcurrentHashMap<>(8, 0.5f);
  private static final ConcurrentHashMap<Locale, Properties> MESSAGES_BUNDLES = new ConcurrentHashMap<>(8, 0.5f);

  static {
    PREFERENCES.addPreferenceChangeListener(ev -> {
      if ("locale".equals(ev.getKey())) {
        var newLocale = ev.getNewValue() == null ? Locale.getDefault() : Locale.forLanguageTag(ev.getNewValue());
        Platform.runLater(() -> LOCALE.set(newLocale));
      }
    });
  }

  private static Locale defaultLocale() {
    var localeFromPrefs = PREFERENCES.get("locale", "");
    if (!localeFromPrefs.isEmpty()) {
      return Locale.forLanguageTag(localeFromPrefs);
    }
    return Locale.getDefault();
  }

  private Texts() {
  }

  private static String key(String key, ConcurrentHashMap<Locale, Properties> bundles) {
    if (key == null) {
      return null;
    }
    try {
      var normalizedLocale = NORMALIZED_LOCALES.computeIfAbsent(LOCALE.get(), l -> Locale.of(l.getLanguage()));
      var ps = bundles.computeIfAbsent(normalizedLocale, l -> {
        var classLoader = Thread.currentThread().getContextClassLoader();
        var resource = bundles == TEXTS_BUNDLES ? "l10n/texts" : "l10n/messages";
        try (var is = classLoader.getResourceAsStream(resource + "_" + l.getLanguage() + ".properties")) {
          var props = new Properties();
          if (is == null) {
            return props;
          } else {
            props.load(is);
          }
          return props;
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });
      return ps.getProperty(key, key);
    } catch (RuntimeException e) {
      var r = new LogRecord(WARNING, "Unable to get {0}");
      r.setThrown(e);
      r.setParameters(new Object[]{key});
      LOGGER.log(r);
      return key;
    }
  }

  private static String fmt(String key, Supplier<Object[]> args) {
    if (key == null) {
      return null;
    }
    try {
      return key(key, TEXTS_BUNDLES).formatted(args.get());
    } catch (RuntimeException e) {
      var r = new LogRecord(WARNING, "Unable to format {0}");
      r.setThrown(e);
      r.setParameters(new Object[]{key});
      LOGGER.log(r);
      return key;
    }
  }

  private static String msg(String key, Supplier<Object[]> args) {
    if (key == null) {
      return null;
    }
    try {
      return MessageFormat.format(key(key, MESSAGES_BUNDLES), args.get());
    } catch (RuntimeException e) {
      var r = new LogRecord(WARNING, "Unable to format a message {0}");
      r.setThrown(e);
      r.setParameters(new Object[]{key});
      LOGGER.log(r);
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
    return createStringBinding(() -> key(text, TEXTS_BUNDLES), LOCALE);
  }

  public static StringBinding text(String format, Object... args) {
    return createStringBinding(() -> fmt(format, () -> values(args)), observables(args));
  }

  public static StringBinding msg(String msg) {
    return createStringBinding(() -> key(msg, MESSAGES_BUNDLES), LOCALE);
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
}

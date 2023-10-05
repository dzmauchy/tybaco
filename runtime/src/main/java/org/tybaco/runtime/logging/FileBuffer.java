package org.tybaco.runtime.logging;

/*-
 * #%L
 * runtime
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

import java.io.*;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.EnumSet;
import java.util.stream.IntStream;

import static java.nio.channels.FileChannel.MapMode.READ_WRITE;
import static java.nio.file.StandardOpenOption.*;

final class FileBuffer implements Closeable {

  private final char[] buf = new char[4];
  private final byte[] tempBuf = new byte[8192];
  private final StringBuilder builder = new StringBuilder(64);
  private final FileChannel cch;
  private final FileChannel bch;
  private final MappedByteBuffer byteBuffer;
  private final CharBuffer charBuffer;
  private final CharsetEncoder encoder;

  public FileBuffer(int maxFileSize) {
    var opts = EnumSet.of(CREATE_NEW, SPARSE, DELETE_ON_CLOSE, WRITE, READ);
    try {
      var bFile = Files.createTempFile("blog", ".log");
      Files.deleteIfExists(bFile);
      bch = FileChannel.open(bFile, opts);
      var cFile = Files.createTempFile("clog", ".log");
      Files.deleteIfExists(cFile);
      cch = FileChannel.open(cFile, opts);
      byteBuffer = bch.map(READ_WRITE, 0L, maxFileSize);
      charBuffer = cch.map(READ_WRITE, 0L, maxFileSize * 2L).asCharBuffer();
      encoder = StandardCharsets.UTF_8.newEncoder();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  void write(char c) {
    charBuffer.put(c);
  }

  void writeQuotedString(String v) {
    charBuffer.put('"');
    v.codePoints().flatMap(FileBuffer::escape).forEach(this::append);
    charBuffer.put('"');
  }

  void writeKey(String v) {
    charBuffer.put('"');
    charBuffer.append(v);
    charBuffer.put('"');
  }

  void writeInt(int v) {
    builder.setLength(0);
    builder.append(v);
    builder.codePoints().forEach(this::append);
  }

  void writeMarker(String v) {
    charBuffer.put('"');
    v.codePoints().map(FileBuffer::markerMap).filter(e -> e > 0).forEach(this::append);
    charBuffer.put('"');
  }

  private void append(int cp) {
    var buf = this.buf;
    var n = Character.toChars(cp, buf, 0);
    charBuffer.put(buf, 0, n);
  }

  void rewind(OutputStream stream) throws IOException {
    charBuffer.flip();
    encoder.reset();
    var result = encoder.encode(charBuffer, byteBuffer, false);
    if (!result.isUnderflow()) result.throwException();
    byteBuffer.flip();
    var buf = tempBuf;
    while (true) {
      var l = Math.min(byteBuffer.remaining(), buf.length);
      if (l == 0) break;
      byteBuffer.get(buf, 0, l);
      stream.write(buf, 0, l);
    }
    byteBuffer.clear();
    charBuffer.clear();
  }

  @Override
  public void close() {
    try (cch; bch) {
      builder.setLength(0);
      builder.trimToSize();
    } catch (Throwable e) {
      e.printStackTrace(System.err);
    }
  }

  private static IntStream escape(int cp) {
    return switch (cp) {
      case '"' -> IntStream.of('\\', '"');
      case '\b' -> IntStream.of('\\', 'b');
      case '\f' -> IntStream.of('\\', 'f');
      case '\n' -> IntStream.of('\\', 'n');
      case '\r' -> IntStream.of('\\', 'r');
      case '\t' -> IntStream.of('\\', 't');
      case '\\' -> IntStream.of('\\', '\\');
      default -> IntStream.of(cp);
    };
  }

  private static int markerMap(int cp) {
    if (Character.isLetterOrDigit(cp)) {
      return cp;
    }
    return switch (cp) {
      case '.', '_', '-' -> cp;
      default -> 0;
    };
  }
}

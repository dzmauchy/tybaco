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
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Files;
import java.util.EnumSet;

import static java.nio.channels.FileChannel.MapMode.READ_WRITE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.*;

final class FileBuffer implements Closeable {

  private final char[] buf = new char[2];
  private final byte[] tempBuf = new byte[16384];
  private final StringBuilder builder = new StringBuilder(64);
  private final FileChannel bch;
  private final MappedByteBuffer byteBuffer;
  private final CharsetEncoder encoder;

  public FileBuffer(int maxFileSize) {
    var opts = EnumSet.of(CREATE_NEW, SPARSE, DELETE_ON_CLOSE, WRITE, READ);
    try {
      var bFile = Files.createTempFile("ty", ".log");
      Files.deleteIfExists(bFile);
      bch = FileChannel.open(bFile, opts);
      byteBuffer = bch.map(READ_WRITE, 0L, maxFileSize);
      encoder = UTF_8.newEncoder();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  void write(CharBuffer buffer) {
    var result = encoder.encode(buffer, byteBuffer, true);
    if (!result.isUnderflow()) {
      try {
        result.throwException();
      } catch (CharacterCodingException e) {
        throw new UncheckedIOException(e);
      }
    }
  }

  void write(char c) {
    buf[0] = c;
    write(CharBuffer.wrap(buf, 0, 1));
  }

  void writeQuotedString(String v) {
    write('"');
    int o = 0, l = v.length();
    for (int i = 0; i < l; i++) {
      var c = v.charAt(i);
      if (escape(c)) {
        if (o < i) write(CharBuffer.wrap(v, o, i));
        write(CharBuffer.wrap(buf));
        o = i + 1;
      }
    }
    if (o < l) write(CharBuffer.wrap(v, o, l));
    write('"');
  }

  void writeSafeQuotedString(String v) {
    write('"');
    write(CharBuffer.wrap(v));
    write('"');
  }

  void writeKey(String v) {
    write('"');
    write(CharBuffer.wrap(v));
    write('"');
  }

  void writePair(String k, String v) {
    writeKey(k);
    write(':');
    writeQuotedString(v);
  }

  void writePair(String k, int v) {
    writeKey(k);
    write(':');
    writeInt(v);
  }

  void writePair(String k, long v) {
    writeKey(k);
    write(':');
    writeLong(v);
  }

  void writeSafePair(String k, String v) {
    writeKey(k);
    write(':');
    writeSafeQuotedString(v);
  }

  void writeInt(int v) {
    builder.setLength(0);
    builder.append(v);
    write(CharBuffer.wrap(builder));
  }

  void writeLong(long v) {
    builder.setLength(0);
    builder.append(v);
    write(CharBuffer.wrap(builder));
  }

  void writeMarker(String v) {
    write('"');
    int o = 0, l = v.length();
    for (int i = 0; i < l; i++) {
      char c = v.charAt(i);
      if (Character.isLetterOrDigit(c)) continue;
      switch (c) {
        case '_', '-', '.' -> {}
        default -> {
          if (o < i) write(CharBuffer.wrap(v, o, i));
          o = i + 1;
        }
      }
    }
    if (o < l) write(CharBuffer.wrap(v, o, l));
    write('"');
  }

  void rewind(OutputStream stream) throws IOException {
    byteBuffer.flip();
    var buf = tempBuf;
    while (true) {
      var l = Math.min(byteBuffer.remaining(), buf.length);
      if (l == 0) break;
      byteBuffer.get(buf, 0, l);
      stream.write(buf, 0, l);
    }
  }

  void reset() {
    byteBuffer.clear();
  }

  @Override
  public void close() {
    try (bch) {
      builder.setLength(0);
      builder.trimToSize();
    } catch (Throwable e) {
      e.printStackTrace(System.err);
    }
  }

  private boolean escape(char c) {
    return switch (c) {
      case '"' -> escapeBuf('"');
      case '\b' -> escapeBuf('b');
      case '\f' -> escapeBuf('f');
      case '\n' -> escapeBuf('n');
      case '\r' -> escapeBuf('r');
      case '\t' -> escapeBuf('t');
      case '\\' -> escapeBuf('\\');
      default -> false;
    };
  }

  private boolean escapeBuf(char c) {
    buf[0] = '\\';
    buf[1] = c;
    return true;
  }
}

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

import org.tybaco.types.model.Array;
import org.tybaco.types.model.Atomic;
import org.tybaco.types.model.Parameterized;
import org.tybaco.types.model.Type;
import org.tybaco.types.model.Primitive;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.IDN;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.MulticastChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.AtomicStampedReference;
import java.util.concurrent.atomic.DoubleAccumulator;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleToIntFunction;
import java.util.function.DoubleToLongFunction;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.LongConsumer;
import java.util.function.LongPredicate;
import java.util.function.LongSupplier;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongToIntFunction;
import java.util.function.LongUnaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongBiFunction;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class CommonTypes {

    private CommonTypes() {
        throw new UnsupportedOperationException();
    }

    public static final Atomic BOXED_INT = new Atomic(Integer.class.getName());
    public static final Atomic BOXED_LONG = new Atomic(Long.class.getName());
    public static final Atomic BOXED_BOOLEAN = new Atomic(Boolean.class.getName());
    public static final Atomic BOXED_FLOAT = new Atomic(Float.class.getName());
    public static final Atomic BOXED_DOUBLE = new Atomic(Double.class.getName());
    public static final Atomic BOXED_CHAR = new Atomic(Character.class.getName());
    public static final Atomic BOXED_BYTE = new Atomic(Byte.class.getName());
    public static final Atomic BOXED_SHORT = new Atomic(Byte.class.getName());
    public static final Atomic BOXED_VOID = new Atomic(Void.class.getName());

    public static final Array INT_ARRAY = new Array(Primitive.INT);
    public static final Array LONG_ARRAY = new Array(Primitive.LONG);
    public static final Array BYTE_ARRAY = new Array(Primitive.BYTE);
    public static final Array SHORT_ARRAY = new Array(Primitive.SHORT);
    public static final Array CHAR_ARRAY = new Array(Primitive.CHAR);
    public static final Array BOOLEAN_ARRAY = new Array(Primitive.BOOLEAN);
    public static final Array FLOAT_ARRAY = new Array(Primitive.FLOAT);
    public static final Array DOUBLE_ARRAY = new Array(Primitive.DOUBLE);

    public static final Atomic STRING = new Atomic(String.class.getName());
    public static final Atomic CHAR_SEQUENCE = new Atomic(CharSequence.class.getName());
    public static final Atomic STRING_BUFFER = new Atomic(StringBuffer.class.getName());
    public static final Atomic STRING_BUILDER = new Atomic(StringBuilder.class.getName());
    public static final Atomic NUMBER = new Atomic(Number.class.getName());
    public static final Atomic BIG_INTEGER = new Atomic(BigInteger.class.getName());
    public static final Atomic BIG_DECIMAL = new Atomic(BigDecimal.class.getName());
    public static final Atomic MATH_CONTEXT = new Atomic(MathContext.class.getName());
    public static final Atomic APPENDABLE = new Atomic(Appendable.class.getName());

    public static final Atomic INT_STREAM = new Atomic(IntStream.class.getName());
    public static final Atomic LONG_STREAM = new Atomic(LongStream.class.getName());
    public static final Atomic DOUBLE_STREAM = new Atomic(DoubleStream.class.getName());

    public static final Atomic WRITER = new Atomic(Writer.class.getName());
    public static final Atomic READER = new Atomic(Reader.class.getName());
    public static final Atomic BUFFERED_WRITER = new Atomic(BufferedWriter.class.getName());
    public static final Atomic BUFFERED_READER = new Atomic(BufferedReader.class.getName());
    public static final Atomic INPUT_STREAM = new Atomic(InputStream.class.getName());
    public static final Atomic OUTPUT_STREAM = new Atomic(OutputStream.class.getName());
    public static final Atomic FILE_INPUT_STREAM = new Atomic(FileInputStream.class.getName());
    public static final Atomic FILE_OUTPUT_STREAM = new Atomic(FileOutputStream.class.getName());
    public static final Atomic RANDOM_ACCESS_FILE = new Atomic(RandomAccessFile.class.getName());
    public static final Atomic BUFFERED_INPUT_STREAM = new Atomic(BufferedInputStream.class.getName());
    public static final Atomic BUFFERED_OUTPUT_STREAM = new Atomic(BufferedOutputStream.class.getName());
    public static final Atomic DATA_INPUT_STREAM = new Atomic(DataInputStream.class.getName());
    public static final Atomic DATA_OUTPUT_STREAM = new Atomic(DataOutputStream.class.getName());
    public static final Atomic DATA_INPUT = new Atomic(DataInput.class.getName());
    public static final Atomic DATA_OUTPUT = new Atomic(DataOutput.class.getName());

    public static final Atomic SOCKET = new Atomic(Socket.class.getName());
    public static final Atomic DATAGRAM_SOCKET = new Atomic(DatagramSocket.class.getName());
    public static final Atomic MULTICAST_SOCKET = new Atomic(MulticastSocket.class.getName());
    public static final Atomic NET_URL = new Atomic(URL.class.getName());
    public static final Atomic NET_URI = new Atomic(URI.class.getName());
    public static final Atomic NETWORK_INTERFACE = new Atomic(NetworkInterface.class.getName());
    public static final Atomic INET_ADDRESS = new Atomic(InetAddress.class.getName());
    public static final Atomic INET4_ADDRESS = new Atomic(Inet4Address.class.getName());
    public static final Atomic INET6_ADDRESS = new Atomic(Inet6Address.class.getName());
    public static final Atomic NET_IDN = new Atomic(IDN.class.getName());
    public static final Atomic DATAGRAM_PACKET = new Atomic(DatagramPacket.class.getName());
    public static final Atomic PROXY = new Atomic(Proxy.class.getName());
    public static final Atomic SOCKET_ADDRESS = new Atomic(SocketAddress.class.getName());
    public static final Atomic INET_SOCKET_ADDRESS = new Atomic(InetSocketAddress.class.getName());
    public static final Atomic UNIX_DOMAIN_SOCKET_ADDRESS = new Atomic(UnixDomainSocketAddress.class.getName());
    public static final Atomic HTTP_URL_CONNECTION = new Atomic(HttpURLConnection.class.getName());

    public static final Atomic BYTE_CHANNEL = new Atomic(ByteChannel.class.getName());
    public static final Atomic READABLE_BYTE_CHANNEL = new Atomic(ReadableByteChannel.class.getName());
    public static final Atomic WRITABLE_BYTE_CHANNEL = new Atomic(WritableByteChannel.class.getName());
    public static final Atomic GATHERING_BYTE_CHANNEL = new Atomic(GatheringByteChannel.class.getName());
    public static final Atomic FILE_CHANNEL = new Atomic(FileChannel.class.getName());
    public static final Atomic DATAGRAM_CHANNEL = new Atomic(DatagramChannel.class.getName());
    public static final Atomic SOCKET_CHANNEL = new Atomic(SocketChannel.class.getName());
    public static final Atomic MULTICAST_CHANNEL = new Atomic(MulticastChannel.class.getName());

    public static final Atomic BYTE_BUFFER = new Atomic(ByteBuffer.class.getName());
    public static final Atomic CHAR_BUFFER = new Atomic(CharBuffer.class.getName());
    public static final Atomic INT_BUFFER = new Atomic(IntBuffer.class.getName());
    public static final Atomic LONG_BUFFER = new Atomic(LongBuffer.class.getName());
    public static final Atomic FLOAT_BUFFER = new Atomic(FloatBuffer.class.getName());
    public static final Atomic DOUBLE_BUFFER = new Atomic(DoubleBuffer.class.getName());

    public static final Atomic INT_SUPPLIER = new Atomic(IntSupplier.class.getName());
    public static final Atomic LONG_SUPPLIER = new Atomic(LongSupplier.class.getName());
    public static final Atomic DOUBLE_SUPPLIER = new Atomic(DoubleSupplier.class.getName());
    public static final Atomic BOOLEAN_SUPPLIER = new Atomic(BooleanSupplier.class.getName());
    public static final Atomic DOUBLE_BINARY_OPERATOR = new Atomic(DoubleBinaryOperator.class.getName());
    public static final Atomic DOUBLE_TO_INT_FUNCTION = new Atomic(DoubleToIntFunction.class.getName());
    public static final Atomic DOUBLE_TO_LONG_FUNCTION = new Atomic(DoubleToLongFunction.class.getName());
    public static final Atomic INT_BINARY_OPERATOR = new Atomic(IntBinaryOperator.class.getName());
    public static final Atomic INT_CONSUMER = new Atomic(IntConsumer.class.getName());
    public static final Atomic INT_PREDICATE = new Atomic(IntPredicate.class.getName());
    public static final Atomic INT_TO_DOUBLE_FUNCTION = new Atomic(IntToDoubleFunction.class.getName());
    public static final Atomic INT_TO_LONG_FUNCTION = new Atomic(IntToLongFunction.class.getName());
    public static final Atomic INT_UNARY_OPERATOR = new Atomic(IntUnaryOperator.class.getName());
    public static final Atomic LONG_BINARY_OPERATOR = new Atomic(LongBinaryOperator.class.getName());
    public static final Atomic LONG_CONSUMER = new Atomic(LongConsumer.class.getName());
    public static final Atomic LONG_PREDICATE = new Atomic(LongPredicate.class.getName());
    public static final Atomic LONG_TO_DOUBLE_FUNCTION = new Atomic(LongToDoubleFunction.class.getName());
    public static final Atomic LONG_TO_INT_FUNCTION = new Atomic(LongToIntFunction.class.getName());
    public static final Atomic LONG_UNARY_OPERATOR = new Atomic(LongUnaryOperator.class.getName());

    public static final Atomic ATOMIC_INT = new Atomic(AtomicInteger.class.getName());
    public static final Atomic ATOMIC_LONG = new Atomic(AtomicLong.class.getName());
    public static final Atomic ATOMIC_BOOLEAN = new Atomic(AtomicBoolean.class.getName());
    public static final Atomic ATOMIC_INT_ARRAY = new Atomic(AtomicIntegerArray.class.getName());
    public static final Atomic ATOMIC_LONG_ARRAY = new Atomic(AtomicLongArray.class.getName());
    public static final Atomic DOUBLE_ACCUMULATOR = new Atomic(DoubleAccumulator.class.getName());
    public static final Atomic DOUBLE_ADDER = new Atomic(DoubleAdder.class.getName());
    public static final Atomic LONG_ACCUMULATOR = new Atomic(LongAccumulator.class.getName());
    public static final Atomic LONG_ADDER = new Atomic(LongAdder.class.getName());

    public static final Atomic LOCK = new Atomic(Lock.class.getName());
    public static final Atomic READ_WRITE_LOCK = new Atomic(ReadWriteLock.class.getName());
    public static final Atomic REENTRANT_LOCK = new Atomic(ReentrantLock.class.getName());
    public static final Atomic STAMPED_LOCK = new Atomic(StampedLock.class.getName());
    public static final Atomic REENTRANT_RW_LOCK = new Atomic(ReentrantReadWriteLock.class.getName());

    public static Parameterized listOf(Type arg) {
        return new Parameterized(List.class.getName(), List.of(arg));
    }

    public static Parameterized mapOf(Type k, Type v) {
        return new Parameterized(Map.class.getName(), List.of(k, v));
    }

    public static Parameterized sortedMapOf(Type k, Type v) {
        return new Parameterized(SortedMap.class.getName(), List.of(k, v));
    }

    public static Parameterized navigableMapOf(Type k, Type v) {
        return new Parameterized(NavigableMap.class.getName(), List.of(k, v));
    }

    public static Parameterized concurrentMapOf(Type k, Type v) {
        return new Parameterized(ConcurrentMap.class.getName(), List.of(k, v));
    }

    public static Parameterized streamOf(Type arg) {
        return new Parameterized(Stream.class.getName(), List.of(arg));
    }

    public static Parameterized collectorOf(Type t, Type a, Type r) {
        return new Parameterized(Collector.class.getName(), List.of(t, a, r));
    }

    public static Parameterized setOf(Type arg) {
        return new Parameterized(Set.class.getName(), List.of(arg));
    }

    public static Parameterized collectionOf(Type arg) {
        return new Parameterized(Collection.class.getName(), List.of(arg));
    }

    public static Parameterized iterableOf(Type arg) {
        return new Parameterized(Iterable.class.getName(), List.of(arg));
    }

    public static Parameterized consumerOf(Type arg) {
        return new Parameterized(Consumer.class.getName(), List.of(arg));
    }

    public static Parameterized biConsumerOf(Type arg1, Type arg2) {
        return new Parameterized(BiConsumer.class.getName(), List.of(arg1, arg2));
    }

    public static Parameterized predicateOf(Type arg) {
        return new Parameterized(Predicate.class.getName(), List.of(arg));
    }

    public static Parameterized biPredicateOf(Type arg1, Type arg2) {
        return new Parameterized(BiPredicate.class.getName(), List.of(arg1, arg2));
    }

    public static Parameterized supplierOf(Type arg) {
        return new Parameterized(Supplier.class.getName(), List.of(arg));
    }

    public static Parameterized unaryOperatorOf(Type arg) {
        return new Parameterized(UnaryOperator.class.getName(), List.of(arg));
    }

    public static Parameterized binaryOperatorOf(Type arg) {
        return new Parameterized(BinaryOperator.class.getName(), List.of(arg));
    }

    public static Parameterized toIntFunctionOf(Type arg) {
        return new Parameterized(ToIntFunction.class.getName(), List.of(arg));
    }

    public static Parameterized toLongFunctionOf(Type arg) {
        return new Parameterized(ToLongFunction.class.getName(), List.of(arg));
    }

    public static Parameterized toDoubleFunctionOf(Type arg) {
        return new Parameterized(ToDoubleFunction.class.getName(), List.of(arg));
    }

    public static Parameterized toIntBiFunctionOf(Type arg1, Type arg2) {
        return new Parameterized(ToIntBiFunction.class.getName(), List.of(arg1, arg2));
    }

    public static Parameterized toLongBiFunctionOf(Type arg1, Type arg2) {
        return new Parameterized(ToLongBiFunction.class.getName(), List.of(arg1, arg2));
    }

    public static Parameterized toDoubleBiFunctionOf(Type arg1, Type arg2) {
        return new Parameterized(ToDoubleBiFunction.class.getName(), List.of(arg1, arg2));
    }

    public static Parameterized atomicReferenceOf(Type arg) {
        return new Parameterized(AtomicReference.class.getName(), List.of(arg));
    }

    public static Parameterized atomicStampedReferenceOf(Type arg) {
        return new Parameterized(AtomicStampedReference.class.getName(), List.of(arg));
    }

    public static Parameterized atomicMarkableReferenceOf(Type arg) {
        return new Parameterized(AtomicMarkableReference.class.getName(), List.of(arg));
    }

    public static Parameterized atomicReferenceArrayOf(Type arg) {
        return new Parameterized(AtomicReferenceArray.class.getName(), List.of(arg));
    }
}

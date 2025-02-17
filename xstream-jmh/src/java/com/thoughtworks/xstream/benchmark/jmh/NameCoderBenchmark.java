/*
 * Copyright (C) 2015 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 *
 * Created on 16. December 2015 by Joerg Schaible, renamed from XmlFriendlyBenchmark
 */
package com.thoughtworks.xstream.benchmark.jmh;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.BenchmarkParams;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.naming.NameCoder;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
import com.thoughtworks.xstream.io.xml.Xpp3Driver;


/**
 * Benchmark for different {@link NameCoder} implementations.
 * 
 * @author J&ouml;rg Schaible
 * @since upcoming
 */
@BenchmarkMode(Mode.AverageTime)
@Fork(value = 1)
@Measurement(iterations = 25)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Threads(4)
@Warmup(iterations = 5)
public class NameCoderBenchmark {

    private XStream xstream;
    private String xml;
    private _1._2._3._4._5.Unfriendly array[];

    /**
     * No encoding, will create invalid XML for inner class types.
     *
     * @since upcoming
     */
    public static final class NoNameCoder implements NameCoder {

        @Override
        public String encodeNode(final String name) {
            return name;
        }

        @Override
        public String encodeAttribute(final String name) {
            return name;
        }

        @Override
        public String decodeNode(final String nodeName) {
            return nodeName;
        }

        @Override
        public String decodeAttribute(final String attributeName) {
            return attributeName;
        }
    }

    /**
     * Dollar encoding, will create invalid XML for class types in the default package.
     *
     * @since upcoming
     */
    public static final class DollarNameCoder implements NameCoder {

        @Override
        public String encodeNode(final String name) {
            return name.replace('$', '\u00b7');
        }

        @Override
        public String encodeAttribute(final String name) {
            return name.replace('$', '\u00b7');
        }

        @Override
        public String decodeNode(final String nodeName) {
            return nodeName.replace('\u00b7', '$');
        }

        @Override
        public String decodeAttribute(final String attributeName) {
            return attributeName.replace('\u00b7', '$');
        }
    }

    /**
     * Dollar encoding with an escaped underscore, may create invalid XML for class types defined in other languages
     * running on the JVM.
     *
     * @since upcoming
     */
    public static class EscapedUnderscoreNameCoder implements NameCoder {

        @Override
        public String encodeNode(final String name) {
            final int length = name.length();
            final StringBuilder sb = new StringBuilder(length + 20);
            for (int i = 0; i < length; ++i) {
                final char ch = name.charAt(i);
                switch (ch) {
                case '$':
                    sb.append("_-");
                    break;
                case '_':
                    sb.append("__");
                    break;
                default:
                    sb.append(ch);
                }
            }
            return sb.toString();
        }

        @Override
        public String encodeAttribute(final String name) {
            return encodeNode(name);
        }

        @Override
        public String decodeNode(final String nodeName) {
            final int length = nodeName.length();
            final StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; ++i) {
                char ch = nodeName.charAt(i);
                if (ch == '_') {
                    if (++i == length) {
                        throw new IllegalStateException();
                    }
                    ch = nodeName.charAt(i);
                    switch (ch) {
                    case '_':
                        sb.append(ch);
                        break;
                    case '-':
                        sb.append('$');
                        break;
                    default:
                        throw new IllegalStateException();
                    }
                } else {
                    sb.append(ch);
                }
            }
            return sb.toString();
        }

        @Override
        public String decodeAttribute(final String attributeName) {
            return decodeNode(attributeName);
        }
    }

    /**
     * Cached dollar encoding with an escaped underscore, may create invalid XML for class types defined in other
     * languages running on the JVM.
     *
     * @since upcoming
     */
    public static class CachedEscapedUnderscoreNameCoder extends EscapedUnderscoreNameCoder {
        private final ConcurrentMap<String, String> encoderCache = new ConcurrentHashMap<String, String>();
        private final ConcurrentMap<String, String> decoderCache = new ConcurrentHashMap<String, String>();

        @Override
        public String encodeNode(final String name) {
            String encoded = encoderCache.get(name);
            if (encoded == null) {
                encoded = super.encodeNode(name);
                encoderCache.putIfAbsent(name, encoded);
                decoderCache.putIfAbsent(encoded, name);
            }
            return encoded;
        }

        @Override
        public String decodeNode(final String nodeName) {
            String decoded = decoderCache.get(nodeName);
            if (decoded == null) {
                decoded = super.decodeNode(nodeName);
                decoderCache.putIfAbsent(nodeName, decoded);
                encoderCache.putIfAbsent(decoded, nodeName);
            }
            return decoded;
        }
    }

    private static class _1 {
        private static class _2 {
            private static class _3 {
                private static class _4 {
                    private static class _5 {
                        private static class Unfriendly {
                            @SuppressWarnings("unused")
                            final int __i__i__;
                            @SuppressWarnings("unused")
                            final String x__x__;
                            @SuppressWarnings("unused")
                            final Unfriendly __;

                            public Unfriendly(final int i, final Unfriendly u) {
                                __i__i__ = i;
                                x__x__ = Integer.toHexString(i);
                                __ = u;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Initialize the XML string to deserialize.
     *
     * @since upcoming
     */
    @Setup
    public void init() {
        array = new _1._2._3._4._5.Unfriendly[250];
        for (int i = 0; i < array.length / 10; ++i) {
            final int idx = i * 10;
            for (int j = 0; j < 10; ++j) {
                array[idx] = new _1._2._3._4._5.Unfriendly(idx + 9 - j, array[idx]);
                if (j < 9) {
                    array[idx + j + 1] = array[idx];
                }
            }
        }
    }

    /**
     * Setup the data to deserialize.
     *
     * @param params the parameters of the benchmark
     * @since upcoming
     */
    @Setup(Level.Trial)
    public void setUp(final BenchmarkParams params) {
        final String benchmark = params.getBenchmark();
        final NameCoder nameCoder;
        switch (benchmark.substring(NameCoderBenchmark.class.getName().length() + 1)) {
        case "noCoding":
            nameCoder = new NoNameCoder();
            break;
        case "dollarCoding":
            nameCoder = new DollarNameCoder();
            break;
        case "escapedUnderscoreCoding":
            nameCoder = new EscapedUnderscoreNameCoder();
            break;
        case "cahcedEscapedUnderscoreCoding":
            nameCoder = new CachedEscapedUnderscoreNameCoder();
            break;
        case "xmlFriendlyCoding":
            nameCoder = new XmlFriendlyNameCoder();
            break;
        default:
            throw new IllegalStateException("Unsupported benchmark type: " + benchmark);
        }
        xstream = new XStream(new Xpp3Driver(nameCoder));
        xstream.allowTypes(_1._2._3._4._5.Unfriendly.class);
        if (nameCoder.getClass() == NoNameCoder.class) {
            xstream.alias(_1._2._3._4._5.Unfriendly.class.getName().replace('$', '\u00b7'),
                _1._2._3._4._5.Unfriendly.class);
        }
        xml = xstream.toXML(array);
        //System.out.println(xstream.toXML(array[0]));
    }

    /**
     * No encoding, will produce invalid XML for inner class types.
     *
     * @since upcoming
     */
    @Benchmark
    public void noCoding() {
        run();
    }

    /**
     * Dollar encoding, will produce invalid XML for class types in the default package.
     *
     * @since upcoming
     */
    @Benchmark
    public void dollarCoding() {
        run();
    }

    /**
     * Escaped underscore encoding, can encode any Java identifier.
     *
     * @since upcoming
     */
    @Benchmark
    public void escapedUnderscoreCoding() {
        run();
    }

    /**
     * Escaped underscore encoding with caching, can encode any Java identifier.
     *
     * @since upcoming
     */
    @Benchmark
    public void cachedEscapedUnderscoreCoding() {
        run();
    }

    /**
     * XML friendly encoding used by XStream as default, can encode any invalid XML character.
     *
     * @since upcoming
     */
    @Benchmark
    public void xmlFriendlyCoding() {
        run();
    }

    private void run() {
        final String x = xstream.toXML(xstream.fromXML(xml));
        assert x.equals(xml) : "XML differs";
    }
}

package org.apache.cassandra.io.compress;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.scijava.nativelib.NativeLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.meteogroup.jbrotli.Brotli;
import com.meteogroup.jbrotli.Brotli.Mode;
import com.meteogroup.jbrotli.BrotliDeCompressor;


public class BrotliCompressor implements ICompressor {

    private static final Logger LOG = LoggerFactory.getLogger(BrotliCompressor.class);

    static {
        // NativeLoader#loadLibrary first extracts the JNI shared library to a temporary location before
        // loading it. This location is first taken from the `java.library.tmpdir' system property, or
        // failing that, `tmplib' in the current directory. The former is inconvenient, the latter
        // unacceptable, so assign `java.library.tmpdir' to the JVM-standard `java.io.tmpdir' if unset.
        if (System.getProperty("java.library.tmpdir") == null) {
            LOG.warn("Native library temp directory `java.library.tmpdir' unset, using `java.io.tmpdir' instead");
            System.setProperty("java.library.tmpdir", System.getProperty("java.io.tmpdir"));
        }

        try {
            NativeLoader.loadLibrary("brotli");
        }
        catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static BrotliCompressor create(Map<String, String> options) {
        return new BrotliCompressor(options);
    }

    private final com.meteogroup.jbrotli.BrotliCompressor compressor = new com.meteogroup.jbrotli.BrotliCompressor();
    private final BrotliDeCompressor decompressor = new BrotliDeCompressor();
    private final Brotli.Parameter brotliParam;

    private BrotliCompressor(Map<String, String> options) {
        brotliParam = getBrotliParameter(options);
        LOG.info("Initialized new compressor instance (options={})", options);
    }

    @Override
    public int compress(byte[] input, int inputOffset, int inputLength, WrappedArray output, int outputOffset)
            throws IOException {
        return compressor.compress(this.brotliParam, input, inputOffset, inputLength, output.buffer, outputOffset, output.buffer.length);
    }

    @Override
    public int uncompress(byte[] input, int inputOffset, int inputLength, byte[] output, int outputOffset)
            throws IOException {
        return decompressor.deCompress(input, inputOffset, inputLength, output, outputOffset, output.length);
    }

    @Override
    public int initialCompressedBufferLength(int chunkLength) {
        return chunkLength;
    }

    @Override
    public Set<String> supportedOptions() {
        return Sets.newHashSet("lgwin", "lgblock", "quality", "mode");
    }

    private static Brotli.Parameter getBrotliParameter(Map<String, String> options) {
        Brotli.Parameter parms = getDefaultBrotliParameter();

        if (options.containsKey("mode")) {
            parms.setMode(Brotli.Mode.valueOf(options.get("mode").toUpperCase()));
        }

        if (options.containsKey("quality")) {
            try {
                int quality = Integer.parseInt(options.get("quality"));
                checkArgument(quality >= 0 && quality <= 11, "quality must be between 0 and 11");
                parms.setQuality(quality);
            }
            catch (NumberFormatException e) {
                throw new IllegalArgumentException("quality requires an integer value");
            }
        }

        if (options.containsKey("lgwin")) {
            try {
                int lgwin = Integer.parseInt(options.get("lgwin"));
                checkArgument(lgwin >= 10 && lgwin <= 24, "lgwin must be between 10 and 24");
                parms.setLgwin(lgwin);
            }
            catch (NumberFormatException e) {
                throw new IllegalArgumentException("lgwin requires an integer value");
            }
        }

        if (options.containsKey("lgblock")) {
            try {
                int lgblock = Integer.parseInt(options.get("lgblock"));
                checkArgument(lgblock >= 16 && lgblock <= 24, "lgblock must be between 16 and 24");
                parms.setLgwin(lgblock);
            }
            catch (NumberFormatException e) {
                throw new IllegalArgumentException("lgblock requires an integer value");
            }
        }

        return parms;
    }

    private static Brotli.Parameter getDefaultBrotliParameter() {
        return new Brotli.Parameter(Mode.GENERIC, 0, Brotli.DEFAULT_LGWIN, Brotli.DEFAULT_LGBLOCK);
    }

}

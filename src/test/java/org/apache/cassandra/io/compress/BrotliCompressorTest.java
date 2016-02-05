package org.apache.cassandra.io.compress;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;

import org.apache.cassandra.io.compress.ICompressor.WrappedArray;
import org.junit.Test;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

public class BrotliCompressorTest {

    @Test
    public void testLarge() throws IOException {
        File f = new File(getClass().getResource("/obama.html").getPath());
        byte[] input = Files.toByteArray(f);
        byte[] compressed = new byte[input.length];
        byte[] decompressed = new byte[input.length];

        BrotliCompressor compressor = BrotliCompressor.create(Collections.singletonMap("quality", "5"));

        // Compress
        int compressedLen = compressor.compress(input, 0, input.length, new WrappedArray(compressed), 0);

        // De-compress
        compressor.uncompress(compressed, 0, compressedLen, decompressed, 0);

        HashFunction hf = Hashing.md5();
        HashCode inputHash = hf.hashBytes(input);
        HashCode decompressedHash = hf.hashBytes(decompressed);

        // Compare hash of input data to result of a round-trip through the compressor.
        assertThat(decompressedHash, is(equalTo(inputHash)));
    }

    @Test
    public void testSimple() throws IOException {
        byte[] input = "Hello, world!".getBytes(Charset.forName("UTF-8"));
        byte[] compressed = new byte[2048];

        BrotliCompressor compressor = BrotliCompressor.create(Collections.emptyMap());
        int compressedLen = compressor.compress(input, 0, input.length, new WrappedArray(compressed), 0);

        byte[] output = new byte[2048];
        int decompressedLen = compressor.uncompress(compressed, 0, compressedLen, output, 0);

        assertThat(new String(Arrays.copyOf(output, decompressedLen), "UTF-8"), equalTo(new String(input, "UTF-8")));
    }

}

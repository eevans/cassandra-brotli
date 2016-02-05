package org.apache.cassandra.io.compress;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;

import org.apache.cassandra.io.compress.ICompressor.WrappedArray;
import org.junit.Test;

public class BrotliCompressorSimpleTest {

    @Test
    public void test() throws IOException {
        byte[] input = "Hello, world!".getBytes(Charset.forName("UTF-8"));
        byte[] compressed = new byte[2048];

        BrotliCompressor compressor = BrotliCompressor.create(Collections.emptyMap());
        int compressedLen = compressor.compress(input, 0, input.length, new WrappedArray(compressed), 0);

        byte[] output = new byte[2048];
        int decompressedLen = compressor.uncompress(compressed, 0, compressedLen, output, 0);

        assertThat(new String(Arrays.copyOf(output, decompressedLen), "UTF-8"), equalTo(new String(input, "UTF-8")));
    }

}

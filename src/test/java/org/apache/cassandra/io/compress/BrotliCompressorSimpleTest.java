package org.apache.cassandra.io.compress;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Collections;

import org.junit.Test;

public class BrotliCompressorSimpleTest {

    @Test
    public void test() throws IOException {
        byte[] test = "Hello, world!".getBytes(Charset.forName("UTF-8"));
        ByteBuffer input = ByteBuffer.allocateDirect(2048);
        ByteBuffer compressed = ByteBuffer.allocateDirect(2048);
        ByteBuffer decompressed = ByteBuffer.allocateDirect(2048);

        input.put(test).flip();

        BrotliCompressor compressor = BrotliCompressor.create(Collections.emptyMap());

        compressor.compress(input, compressed);
        compressor.uncompress(compressed, decompressed);

        byte[] decompressedBytes = new byte[decompressed.limit()];
        decompressed.get(decompressedBytes);

        assertThat(new String(decompressedBytes, "UTF-8"), equalTo(new String(test, "UTF-8")));

    }

}

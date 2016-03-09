package org.apache.cassandra.io.compress;

import static com.google.common.base.Preconditions.checkArgument;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

@RunWith(Parameterized.class)
public class BrotliCompressorTest {

    private static Map<String, String> mapFor(String... args) {
        checkArgument((args.length >= 2) && ((args.length % 2) == 0));
        Map<String, String> map = Maps.newHashMap();
        for (int i = 0; i < args.length - 1; i++) {
            String k = args[i];
            String v = args[i + 1];
            map.put(k, v);
        }
        return map;
    }

    @Parameters
    public static Collection<Object[]> data() {
        List<Object[]> parms = Lists.newArrayList();

        for (String f : new String[]{ "obama.html", "foobar.html", "san_antonio.html" }) {
            parms.add(new Object[]{ f, mapFor("quality",  "0") });
            parms.add(new Object[]{ f, mapFor("quality",  "1") });
            parms.add(new Object[]{ f, mapFor("quality",  "2") });
            parms.add(new Object[]{ f, mapFor("quality",  "3") });
            parms.add(new Object[]{ f, mapFor("quality",  "4") });
            parms.add(new Object[]{ f, mapFor("quality",  "5") });
            parms.add(new Object[]{ f, mapFor("quality",  "6") });
            parms.add(new Object[]{ f, mapFor("quality",  "7") });
            parms.add(new Object[]{ f, mapFor("quality",  "8") });
            parms.add(new Object[]{ f, mapFor("quality",  "9") });
            parms.add(new Object[]{ f, mapFor("quality", "10") });
            parms.add(new Object[]{ f, mapFor("quality", "11") });
            parms.add(new Object[]{ f, mapFor(   "mode", "generic") });
            parms.add(new Object[]{ f, mapFor(   "mode", "text") });
            parms.add(new Object[]{ f, mapFor(   "mode", "font") });
            parms.add(new Object[]{ f, mapFor(  "lgwin", "10") });
            parms.add(new Object[]{ f, mapFor(  "lgwin", "17") });
            parms.add(new Object[]{ f, mapFor(  "lgwin", "24") });
            parms.add(new Object[]{ f, mapFor("lgblock", "16") });
            parms.add(new Object[]{ f, mapFor("lgblock", "20") });
            parms.add(new Object[]{ f, mapFor("lgblock", "24") });
            parms.add(new Object[]{ f, mapFor("quality",  "1", "mode", "generic", "lgwin", "10", "lgblock", "16") });
            parms.add(new Object[]{ f, mapFor("quality",  "5", "mode", "generic", "lgwin", "12", "lgblock", "18") });
            parms.add(new Object[]{ f, mapFor("quality",  "7", "mode", "generic", "lgwin", "14", "lgblock", "20") });
            parms.add(new Object[]{ f, mapFor("quality",  "9", "mode", "generic", "lgwin", "16", "lgblock", "18") });
            parms.add(new Object[]{ f, mapFor("quality",  "5", "mode", "text",    "lgwin", "18", "lgblock", "20") });
            parms.add(new Object[]{ f, mapFor("quality",  "7", "mode", "font",    "lgwin", "20", "lgblock", "22") });
        }
     
        return parms;
    }

    private final String file;
    private final Map<String, String> options;

    public BrotliCompressorTest(String file, Map<String, String> options) {
        this.file = file;
        this.options = options;
    }

    @Test
    public void testLarge() throws IOException {
        File f = new File(getClass().getResource("/" + this.file).getPath());
        byte[] test = Files.toByteArray(f);
        ByteBuffer input = ByteBuffer.allocateDirect(test.length);
        ByteBuffer compressed = ByteBuffer.allocateDirect(test.length);
        ByteBuffer decompressed = ByteBuffer.allocateDirect(test.length);

        // Write test data to buffer and flip to make ready for read.
        input.put(test).flip();

        BrotliCompressor compressor = BrotliCompressor.create(this.options);

        // Compress
        compressor.compress(input, compressed);

        // De-compress
        compressor.uncompress(compressed, decompressed);

        HashFunction hf = Hashing.md5();
        HashCode inputHash = hf.hashBytes(test);
        byte[] decompressedBytes = new byte[decompressed.limit()];
        decompressed.get(decompressedBytes);
        HashCode decompressedHash = hf.hashBytes(decompressedBytes);

        // Compare hash of input data to result of a round-trip through the compressor.
        assertThat(decompressedHash, is(equalTo(inputHash)));

    }

}

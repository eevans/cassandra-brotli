package org.apache.cassandra.io.compress;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class InvalidOptionsTest {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { Collections.singletonMap("lgwin", "eleven") },    // Not a number
            { Collections.singletonMap("lgwin", "30") },        // Too big
            { Collections.singletonMap("lgwin", "5") },         // Too small
            { Collections.singletonMap("lgblock", "eleven") },  // Not a number
            { Collections.singletonMap("lgblock", "30") },      // Too big
            { Collections.singletonMap("lgblock", "5") },       // Too small
            { Collections.singletonMap("quality", "eleven") },  // Not a number
            { Collections.singletonMap("quality", "30") },      // Too big
            { Collections.singletonMap("quality", "-5") },      // Too small
            { Collections.singletonMap("mode", "foo") },        // Not a mode
        });
    }

    private final Map<String, String> options;

    public InvalidOptionsTest(Map<String, String> options) {
        this.options = options;
    }

    @Test(expected = IllegalArgumentException.class)
    public void test() {
        BrotliCompressor.create(this.options);
    }

}

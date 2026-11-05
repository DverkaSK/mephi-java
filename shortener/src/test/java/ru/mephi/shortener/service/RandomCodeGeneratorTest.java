package ru.mephi.shortener.service;

import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomCodeGeneratorTest {

    @Test
    void generatesCodesOfConfiguredLengthFromBase62Alphabet() {
        RandomCodeGenerator generator = new RandomCodeGenerator(7, new Random(42));

        IntStream.range(0, 1000).mapToObj(i -> generator.nextCode()).forEach(code -> {
            assertEquals(7, code.length());
            assertTrue(code.chars().allMatch(c -> RandomCodeGenerator.ALPHABET.indexOf(c) >= 0), code);
        });
    }

    @Test
    void producesVariedCodes() {
        RandomCodeGenerator generator = new RandomCodeGenerator(6);

        long distinct = IntStream.range(0, 1000).mapToObj(i -> generator.nextCode()).distinct().count();

        assertTrue(distinct > 990, "distinct=" + distinct);
    }

    @Test
    void rejectsNonPositiveLength() {
        assertThrows(IllegalArgumentException.class, () -> new RandomCodeGenerator(0));
    }
}

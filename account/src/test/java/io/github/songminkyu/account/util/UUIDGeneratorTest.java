package io.github.songminkyu.account.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.Test;

public class UUIDGeneratorTest {

    @Test
    void testUUIDFromString() {
        String[] inputs = {"hello", "world", "test", "test"};
        String[] expectedUUIDs = {
            "5d41402a-bc4b-3a76-b971-9d911017c592",
            "7d793037-a076-3186-974b-0282f2f435e7",
            "098f6bcd-4621-3373-8ade-4e832627b4f6",
            "098f6bcd-4621-3373-8ade-4e832627b4f6"
        };

        for (int i = 0; i < inputs.length; i++) {
            UUID uuid = UUIDGenerator.generateUUIDFromString(inputs[i]);
            assertEquals(expectedUUIDs[i], uuid.toString());
        }
    }
}
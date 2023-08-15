package bgu.spl.mics;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class FutureTest {

    private Future<String>  future;

    @BeforeEach
    public void setUp() {
        future = new Future<String>();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void get() {
        assertFalse(future.isDone());
        String s = "hello";
        future.resolve(s);
        assertEquals(future.get(),s);
    }

    @Test
    void resolve() {
        String s = "test";
        future.resolve(s);
        assertTrue(future.isDone());
        assertTrue(s.equals(future.get()));
    }

    @Test
    void isDone() {
        String s = "hello";
        assertFalse(future.isDone());
        future.resolve(s);
        assertTrue(future.isDone());
    }

    @Test
    void testGet() {
        assertFalse(future.isDone());
        String s = "hello";
        future.resolve(s);
        assertEquals(future.get(100, TimeUnit.MILLISECONDS),s);
    }
}
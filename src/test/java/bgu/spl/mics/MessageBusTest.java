package bgu.spl.mics;

import static org.junit.jupiter.api.Assertions.*;

//package bgu.spl.mics;

import bgu.spl.mics.application.services.CPUService;
import bgu.spl.mics.example.messages.ExampleBroadcast;
import bgu.spl.mics.example.messages.ExampleEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageBusTest {

    private MessageBus mb;

    @BeforeEach
    void setUp() {
        mb = MessageBusImpl.getInstance();
    }

    @AfterEach
    void tearDown() {}

    @Test
    void subscribeEvent() {
        CPUService serExample = new CPUService("T");
        ExampleEvent e = new ExampleEvent("something");
        mb.register(serExample);
        mb.subscribeEvent(e.getClass(), serExample);
        assertTrue(mb.checkEventSub(e,serExample));
    }

    @Test
    void subscribeBroadcast() {
        CPUService serExample = new CPUService("T");
        ExampleBroadcast b = new ExampleBroadcast("something");
        mb.register(serExample);
        mb.subscribeBroadcast(b.getClass(), serExample);
        assertTrue(mb.checkBroadSub(b,serExample));
    }

    @Test
    void complete() {
        ExampleEvent e = new ExampleEvent("some");
        CPUService serExample1 = new CPUService("T");
        CPUService serExample2 = new CPUService("G");
        mb.register(serExample1);
        mb.register(serExample2);
        mb.subscribeEvent(e.getClass(),serExample2);
        Future<String> f =serExample1.sendEvent(e);
        mb.complete(e,"");
        assertTrue(f.isDone());
        mb.unregister(serExample1);
        mb.unregister(serExample2);
    }

    @Test
    void sendBroadcast() {
        ExampleBroadcast b = new ExampleBroadcast("some");
        CPUService serExample1 = new CPUService("T");
        CPUService serExample2 = new CPUService("G");
        mb.register(serExample1);
        mb.register(serExample2);
        mb.subscribeBroadcast(ExampleBroadcast.class,serExample2);
        serExample1.sendBroadcast(b);
        try {
            Message d2 = mb.awaitMessage(serExample2);
            assertTrue(b.equals(d2));
        } catch (InterruptedException e){}
        mb.unregister(serExample1);
        mb.unregister(serExample2);
    }

    @Test
    void sendEvent() {
        ExampleEvent e = new ExampleEvent("some");
        CPUService serExample1 = new CPUService("T");
        CPUService serExample2 = new CPUService("G");
        mb.register(serExample1);
        mb.register(serExample2);
        mb.subscribeEvent(e.getClass(),serExample2);
        serExample1.sendEvent(e);
        try{
            Message e2 = mb.awaitMessage(serExample2);
            assertEquals(((ExampleEvent)e2).getSenderName(),e.getSenderName());}
        catch (InterruptedException k){};
        mb.unregister(serExample1);
        mb.unregister(serExample2);
    }

    @Test
    void register() {
        CPUService serExample = new CPUService("T");
        assertFalse(mb.isRegistered(serExample));
        mb.register(serExample);
        assertTrue(mb.isRegistered(serExample));
    }

    @Test
    void unregister() {
        CPUService serExample = new CPUService("T");
        mb.register(serExample);
        assertTrue(mb.isRegistered(serExample));
        mb.unregister(serExample);
        assertFalse(mb.isRegistered(serExample));
    }

    @Test
    void awaitMessage() {
        CPUService serExample = new CPUService("T");
        mb.register(serExample);
        ExampleEvent b = new ExampleEvent("something");
        mb.subscribeEvent(b.getClass(),serExample);
        try{
            Message message = mb.awaitMessage(serExample);
            assertEquals(mb.awaitMessage(serExample),message);
        }
        catch (InterruptedException e){};
    }
}

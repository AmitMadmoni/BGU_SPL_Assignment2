package bgu.spl.mics.application.objects;

import static org.junit.jupiter.api.Assertions.*;

//package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.services.CPUService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CPUTest {

    private CPU cpu;

    @BeforeEach
    void setUp() { cpu = new CPU();
    }

    @AfterEach
    void tearDown() {}

    @Test
    void getCores() {
        assertTrue(cpu.getCores()==0);
        cpu.setCores(10);
        assertTrue(cpu.getCores()==10);
    }

    @Test
    void setCores() {
        assertTrue(cpu.getCores()==0);
        cpu.setCores(10);
        assertTrue(cpu.getCores()==10);
    }

    @Test
    void GetService(){
        assertTrue(cpu.getService()==null);
        CPUService tempSer = new CPUService("cpuService");
        cpu.setService(tempSer);
        assertTrue(cpu.getService().equals(tempSer));
    }

    @Test
    void setService(){
        assertTrue(cpu.getService()==null);
        CPUService tempSer = new CPUService("cpuService");
        cpu.setService(tempSer);
        assertTrue(cpu.getService().equals(tempSer));
    }

}
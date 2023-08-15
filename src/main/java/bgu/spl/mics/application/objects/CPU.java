package bgu.spl.mics.application.objects;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Event;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.services.CPUService;

import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.util.LinkedList;

/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class CPU {
    private int cores;
    private LinkedList<DataBatch> dataBatchContainer;
    private Cluster cluster;
    private CPUService service;

    public CPU() {
        cores=0;
        dataBatchContainer=new LinkedList<DataBatch>();
        cluster=null;
        service=null;
        service.setCpu(this);
    }


    public CPU(int cores) {
        this.cores = cores;
        this.dataBatchContainer=new LinkedList<DataBatch>();
        this.cluster=Cluster.getInstance();
    }

    public Cluster getCluster() {return cluster;}

    /**
     * @return int number of cores
     */
    public int getCores() {return cores;}

    /**
     * @param cores int - to set as the new num of cores
     * @pre none
     * @post this.cores==cores
     */
    public void setCores(int cores) {this.cores = cores;}

    /**
     * @return return service
     */
    public CPUService getService() { return service; }

    /**
     * @param service CPUService - to set as the new list
     * @pre none
     * @post this.service==service
     */
    public void setService(CPUService service) { this.service = service; }
}

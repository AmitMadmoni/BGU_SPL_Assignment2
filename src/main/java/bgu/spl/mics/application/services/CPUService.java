package bgu.spl.mics.application.services;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.FinishWork;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.DataBatch;

/**
 * CPU service is responsible for handling the {@link bgu.spl.mics.application.messages.TrainModelEvent,bgu.spl.mics.application.messages.TestModelEvent}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class CPUService extends MicroService {
    private CPU cpu;
    private MessageBusImpl mb;
    private DataBatch current;     //current batch being processed

    private int time=0;              //total runtime on the cpu
    private int finishTime=0;      //notify that the batch has done being processed
    private int count = 0;         //time for processing the batch (for statistics)
    private boolean busy=false;

    public CPUService(String name) {super(name);}

    @Override
    protected void initialize() {
        subscribeBroadcast(FinishWork.class,(FinishWork)->{
            terminate();
        });
    }

    public void beforeStart(){
        subscribeBroadcast(TickBroadcast.class,(TickBroadcast b) -> {
            synchronized (cpu.getCluster().getProcessed()){
                for(DataBatch db : cpu.getCluster().getProcessed()){
                    if(db.getData().getModel().getGpu().canInsert()) {
                        cpu.getCluster().BackToGPU(db);
                        cpu.getCluster().getProcessed().remove(db);
                    }
                }
                cpu.getCluster().getProcessed().notifyAll();
            }
            time+=1;
            if(current==null)
                current = cpu.getCluster().getNext();
            if(current!=null && !busy){
                busy= true;
                switch (current.getData().getType()) {
                    case Tabular: {
                        finishTime = time + 32 / getCpu().getCores();
                        count = 32 / getCpu().getCores();
                    }
                    case Text: {
                        finishTime = time + 64 / getCpu().getCores();
                        count = 64 / getCpu().getCores();
                    }
                    case Images: {
                        finishTime = time + 128 / getCpu().getCores();
                        count = 128 / getCpu().getCores();
                    }
                }
            }
            if(finishTime==time && current!=null){
                busy = false;
                cpu.getCluster().addCPUTime(count);
                cpu.getCluster().addBatchesCount(1);
                count =0;
                synchronized (current.getData().getModel().getGpu().getPd()) {
                    if (current.getData().getModel().getGpu().canInsert())
                        cpu.getCluster().BackToGPU(current);
                    else {
                        synchronized (cpu.getCluster().getProcessed()) {
                            cpu.getCluster().getProcessed().add(current);
                            cpu.getCluster().getProcessed().notifyAll();
                        }
                    }
                    current.getData().getModel().getGpu().getPd().notifyAll();
                }
                current=null;
            }
        });

    }

    public CPU getCpu() {
        return cpu;
    }

    public void setCpu(CPU cpu) {
        this.cpu = cpu;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void setFinishTime(int finishTime) {
        this.finishTime = finishTime + time;
    }

    public void setCurrent(DataBatch current) {
        this.current = current;
    }
}

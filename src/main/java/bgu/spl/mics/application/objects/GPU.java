package bgu.spl.mics.application.objects;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.services.GPUService;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU {
    /**
     * Enum representing the type of the GPU.
     */
    public enum Type {RTX3090, RTX2080, GTX1080}

    private Type type;
    private Model model;
    private Cluster cluster;
    private int time;
    private ConcurrentLinkedQueue<DataBatch> pd,upd;
    private GPUService service;
    private MessageBusImpl mb;
    private ConcurrentLinkedQueue<Data> datas = new ConcurrentLinkedQueue<>();

    private boolean busy = false;
    private int ticks;
    private int finishTime=0;


    public GPU() {
        model=null;
        cluster=null;
        time=0;
        pd=new ConcurrentLinkedQueue<>();
        upd=new ConcurrentLinkedQueue<>();
        type = null;
        service=null;
        mb=MessageBusImpl.getInstance();
        switch (getType()){
            case GTX1080: ticks = 4;
            case RTX2080: ticks = 2;
            case RTX3090: ticks = 1;
        }
    }

    public GPU(Type type) {
        this.type = type;
        this.cluster = Cluster.getInstance();
        this.model = null;
        pd = new ConcurrentLinkedQueue<>();
        upd = new ConcurrentLinkedQueue<>();
        time = 0;
        mb = MessageBusImpl.getInstance();
        switch (getType()){
            case GTX1080: ticks = 4;
            case RTX2080: ticks = 2;
            case RTX3090: ticks = 1;
        }
    }

    /**
     * @param service
     * @post thie.service = service
     */
    public void setService(GPUService service) {this.service = service;}

    /**
     * @post time+=1
     */
    public void doEveryTick(){
        time+=1;
        if(model!=null && upd.isEmpty() && !datas.isEmpty()){
            Data data = datas.remove();
            data.getModel().setGpu(this);
            setModel(data.getModel());
            for(int i=0;i< data.getSize()/1000;i++){
                DataBatch t = new DataBatch(data, i*1000, this);
                upd.add(t);
            }
        }
        while(!upd.isEmpty() && canInsert())  //send all the data batchs to the cluster
            UPD(upd.peek());

        if(!pd.isEmpty()) {
            if (pd.peek().getData().getModel().getStatus() == Model.Status.PreTrained) {
                model = pd.peek().getData().getModel();
                pd.peek().getData().getModel().setStatus(Model.Status.Training);
                finishTime = time + ticks;
            }
            else if(pd.peek().getData().getModel().getStatus()== Model.Status.Training){
                if(finishTime==time){
                    action(ticks);
                    finishTime = time + ticks;
                }
            }
        }
        if(!pd.isEmpty() && finishTime<time)
            finishTime = time + ticks;
        if(time==finishTime && !pd.isEmpty()){
            action(ticks);
        }
    }

    /**
     * @return ticks
     */
    public int getTicks() {return ticks;}

    /**
     * @return time
     */
    public int getTime() {return time;}

    /**
     * @return finishTime
     */
    public int getFinishTime() {return finishTime;}

    /**
     *
     * @return busy
     */
    public boolean isBusy(){return busy;}

    /**
     * @param model
     * @pre model.getStatus()==PreTrained
     * @post model.getStatus()==Training
     *      upd.size()+=ata.getSize()/1000
     */
    public void trainModel(Model model){     //starts training the model
        busy = true;
        this.model = model;
        Data data = model.getData();
        synchronized (upd) {
            int size = data.getSize()/1000;
            for (int i = 0; i < size; i++) {    //divides the data to batches and insert to upd
                DataBatch t = new DataBatch(data, i * 1000, this);
                upd.add(t);
            }
            upd.notifyAll();
        }
    }

    /**
     *
     * @param model
     * @pre model.getStatus()==Trained
     * @post model.getStatus()==Tested
     *      model.getResult()==Good/Bad
     */
    public void testModel(Model model){
        busy = true;
        this.model = model;
        boolean passed = false;
        if (model.getStudent().getStatus() == Student.Degree.MSc) {
            int tmp = new Random().nextInt(10);
            if (tmp <= 5)
                passed = true;
        } else {   //phd
            int tmp = new Random().nextInt(10);
            if (tmp <= 7)
                passed = true;
        }
        if (passed) {
            model.setResult(Model.Results.Good);
            mb.sendEvent(new PublishResultsEvent(model.getStudent(),"PublishResultsEvent",model.getData(),model));
        }
        else
            model.setResult(Model.Results.Bad);
        model.setStatus(Model.Status.Tested);
        busy = false;
        service.complete(model);

        mb.sendEvent(model.getStudent().getNext());

    }

    /**
     * @post eventMap.size()--
     */
    public void stopWork(){
        service.stop(model);
    }

    /**
     * @return return type
     */
    public Type getType() {return type;}

    /**
     * @return return model
     */
    public Model getModel() {return model;}

    /**
     * @param model Model - new model to set
     * @pre no condition
     * @post this.model==model
     */
    public void setModel(Model model) {this.model = model;}

    /**
     * @return return pd
     */
    public ConcurrentLinkedQueue<DataBatch> getPd() { return pd; }

    /**
     * @return answer - T/F if there's enoguh room
     */
    public boolean canInsert(){
        switch (getType()){
            case GTX1080: if(pd.size()>8) {return false;}
            case RTX2080: if(pd.size()>16) {return false;}
            case RTX3090: if(pd.size()>32) {return false;}
        }
        return true;
    }

    /**
     * @param db DataBatch - send this unprocessed dataBatch to the cluster
     * @pre no condition
     * @inv sends db to be converted if and only if it's inside the epd list to begin with
     * @post upd.size--, cluster.upd++ - sends the UPD from gpu to cluster
     */
    public void UPD(DataBatch db) {
        if (upd.contains(db) && canInsert()) {
            upd.remove(db);
            synchronized (cluster.getUnprocessed()){
                cluster.getUnprocessed().add(db);
                cluster.getUnprocessed().notifyAll();
            }
        }
    }

    /**
     * @return upd
     */
    public ConcurrentLinkedQueue<DataBatch> getUpd() {return upd;}

    /**
     *
     * @param t - data batch that sent from cluster
     * @pre data batch needs to be processed
     * @post pd.size()++
     */
    public void receievePD(DataBatch t){
        synchronized (pd) {
            pd.add(t);
            pd.notifyAll();
        }
    }

    /**
     *
     * @param count - gpu time used per process
     * @pre !service.getSentEvent().isEmpty()
     * @post if the event is 'TrainModel':
     *          cluster.GPUTime = cluster.GPUTime + count
     *          data.processed.size() = data.processed.size() + 1000
     *          model.status = Training
     *          if done training:
     *              cluster.nameslist.size()++
     *              model.status = Trained
     *       if the event is 'TestModel':
     *          model.results = GOOD/BAD
     *          model.status = Tested
     */
    public void action(int count) {
        cluster.addGPUTime(count);
        synchronized (pd) {
            model.getData().addProcessed(1000);
            model.setStatus(Model.Status.Training);
            pd.remove();
            pd.notifyAll();
        }
        if (model.getData().getProcessed() == model.getData().getSize()) {      //done training
            cluster.addName(model.getName());
            model.setStatus(Model.Status.Trained);
            model.getData().getModel().setStatus(Model.Status.Trained);
            TestModelEvent tmp = new TestModelEvent(model.getStudent(), model.getName(), model.getData(), model);
            busy = false;
            service.complete(model);
            mb.sendEvent(tmp);
        }
    }

    /**
     * @return service
     */
    public GPUService getService() {return service;}
}

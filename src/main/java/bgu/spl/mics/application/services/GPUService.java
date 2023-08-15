package bgu.spl.mics.application.services;

import bgu.spl.mics.Event;
import bgu.spl.mics.MicroService;
//import bgu.spl.mics.application.IObjcets.IModel;
import bgu.spl.mics.application.messages.FinishWork;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;
//import com.sun.org.apache.xpath.internal.operations.Mod;

//import javax.jws.WebParam;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * GPU service is responsible for handling the
 * {@link TrainModelEvent} and {@link TestModelEvent}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {

    private final GPU gpu;
    private final Queue<Model> remainingModels;
    private final Map<Model, Event> eventMap;

    public GPUService(String name, GPU gpu) {
        super(name);
        this.gpu = gpu;
        remainingModels = new LinkedList<>();
        eventMap = new HashMap<>();
    }

    @Override
    public void initialize() {
        subscribeBroadcast(FinishWork.class, (b) -> {
            gpu.stopWork();
            terminateWork();
            terminate();
        } );
    }

    public void beforeStart(){
        subscribeBroadcast(TickBroadcast.class, (b) -> {
            gpu.doEveryTick();
        } );
        subscribeEvent(TestModelEvent.class, (e) -> {
            e.getModel().setGpu(gpu);
            remainingModels.add(e.getModel());
            eventMap.put(e.getModel(), e);
            tryToStartEvent();
        });
        subscribeEvent(TrainModelEvent.class, (e) -> {
            e.getModel().setGpu(gpu);
            remainingModels.add(e.getModel());
            eventMap.put(e.getModel(), e);
            tryToStartEvent();
        });

    }


    public void complete(Model model){
        complete(eventMap.get(model), model);
        eventMap.remove(model);
        tryToStartEvent();
    }

    public void stop(Model model){
        complete(eventMap.get(model), null);
        eventMap.remove(model);
    }

    public void terminateWork(){
        while (!remainingModels.isEmpty()) {
            Model m = remainingModels.remove();
            complete(eventMap.get(m), null);
            eventMap.remove(m);
        }
    }

    public void tryToStartEvent() {
        if (!remainingModels.isEmpty() && !gpu.isBusy()){
            Model m = remainingModels.remove();
            if (m.getStatus() == Model.Status.PreTrained)
                gpu.trainModel(m);
            else if (m.getStatus() == Model.Status.Trained)
                gpu.testModel(m);
        }
    }
}

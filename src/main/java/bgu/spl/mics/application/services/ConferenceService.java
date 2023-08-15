package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.FinishWork;
import bgu.spl.mics.application.messages.PublishConfrenceBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.Model;

/**
 * Conference service is in charge of
 * aggregating good results and publishing them via the {@link PublishConfrenceBroadcast},
 * after publishing results the conference will unregister from the system.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ConferenceService extends MicroService {

    private int ticks;          //'date'
    private int time=0;
    private int finishTime;
    private int holdTime;
    private ConfrenceInformation conference=null;

    public ConferenceService(String name,int ticks) {
        super(name);
        this.ticks = ticks;
        this.finishTime = ticks;
        this.holdTime=finishTime+100;
    }

    public ConfrenceInformation getConference() {return conference;}

    public void setConference(ConfrenceInformation conference) {this.conference = conference;}

    @Override
    protected void initialize(){
        subscribeBroadcast(FinishWork.class,(FinishWork)->{
            terminate();
        });
    }

    public void beforeStart(){
        subscribeBroadcast(TickBroadcast.class,(TickBroadcast b)->{
            time=time+1;
            if(time==finishTime) {
                PublishConfrenceBroadcast c = new PublishConfrenceBroadcast(conference.getPublishedModels());
                sendBroadcast(c);
                finishTime = time + ticks;
                conference.Archive();
            }
            if(time==holdTime){
                conference.getPublishedModels().clear();
                holdTime = finishTime + 100;
            }
        });
        subscribeEvent(PublishResultsEvent.class,(PublishResultsEvent e)->{
            this.conference.addPublishedModel(e.getModel());
        });

    }

}

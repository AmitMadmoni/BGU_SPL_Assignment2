package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.FinishWork;
import bgu.spl.mics.application.messages.TickBroadcast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService{

	private int duration;
	private int speed;

	public TimeService() {
		super("Timer");
	}

	@Override
	protected void initialize() {
		while(duration>0){
			TickBroadcast t = new TickBroadcast();
			sendBroadcast(t);
			duration-=1;
			try{
				synchronized (this) {
					wait(speed);
				}
			}catch (InterruptedException e){}
		}
		sendBroadcast(new FinishWork());
		terminate();
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}
}

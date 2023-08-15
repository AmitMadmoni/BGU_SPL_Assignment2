package bgu.spl.mics;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	private final static AtomicReference<MessageBusImpl> messageBus = new AtomicReference<>(null);

	private final ConcurrentHashMap<Class<? extends Broadcast>, LinkedBlockingQueue<MicroService>> broadSubsMap;
	private final ConcurrentHashMap<Class<? extends Event>, LinkedBlockingQueue<MicroService>> eventSubsMap;
	private final ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> MSMsgMap;
	private final ConcurrentHashMap<MicroService, Set<Class<? extends Message>>> MessageTypeSubs;
	private final ConcurrentHashMap<Event, Future> ftMap;
	private final Object register = new Object();

	private MessageBusImpl() {
		broadSubsMap = new ConcurrentHashMap<>();
		eventSubsMap = new ConcurrentHashMap<>();
		MSMsgMap = new ConcurrentHashMap<>();
		MessageTypeSubs = new ConcurrentHashMap<>();
		ftMap = new ConcurrentHashMap<>();
	}

	public static MessageBusImpl getInstance() {
		messageBus.compareAndSet(null, new MessageBusImpl());
		return messageBus.get();
	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		if (!isRegistered(m))
			return;
		if (!eventSubsMap.containsKey(type))
			synchronized (eventSubsMap) {
				if (!eventSubsMap.containsKey(type))
					eventSubsMap.put(type, new LinkedBlockingQueue<>());
				eventSubsMap.notifyAll();
			}
		synchronized (eventSubsMap.get(type)) {
			if (!eventSubsMap.get(type).contains(m))
				eventSubsMap.get(type).add(m);
			eventSubsMap.get(type).notifyAll();
		}
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		if (!isRegistered(m))
			return;
		if (!broadSubsMap.containsKey(type))
			synchronized (broadSubsMap) {
				if (!broadSubsMap.containsKey(type))
					broadSubsMap.put(type, new LinkedBlockingQueue<>());
				broadSubsMap.notifyAll();
			}
		synchronized (broadSubsMap.get(type)) {
			broadSubsMap.get(type).add(m);
			broadSubsMap.get(type).notifyAll();
		}
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		synchronized (ftMap) {
			if (e==null || !ftMap.containsKey(e))
				return;
			ftMap.get(e).resolve(result);
			ftMap.remove(e);
			ftMap.notifyAll();
		}
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		Class<? extends Broadcast> bType = b.getClass();
		if (!broadSubsMap.containsKey(bType))
			synchronized (broadSubsMap) {
				if (!broadSubsMap.containsKey(bType))
					broadSubsMap.put(bType, new LinkedBlockingQueue<>());
				broadSubsMap.notifyAll();
			}
		synchronized (broadSubsMap.get(bType)) {
			for (MicroService ms : broadSubsMap.get(bType)) {
				synchronized (MSMsgMap.get(ms)) {
					MSMsgMap.get(ms).add(b);
					MSMsgMap.get(ms).notifyAll();
				}
			}
			broadSubsMap.get(bType).notifyAll();
		}
	}

	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		if(e==null)
			return null;
		Class<? extends Event> type = e.getClass();
		if (!eventSubsMap.containsKey(type))
			synchronized (eventSubsMap) {
				if (!eventSubsMap.containsKey(type))
					eventSubsMap.put(type, new LinkedBlockingQueue<>());
				eventSubsMap.notifyAll();
			}
		synchronized (eventSubsMap.get(type)) {
			while (eventSubsMap.get(type)==null || eventSubsMap.get(type).isEmpty()) {
				if (ftMap.containsKey(e))
					complete(e, null);
				return null;
			}
			Future<T> ft = new Future<>();
			synchronized (ftMap) {
				if (ftMap.containsKey(e))
					ft = ftMap.get(e);
				else
					ftMap.put(e, ft);
				ftMap.notifyAll();
			}
			MicroService m = eventSubsMap.get(type).remove();
			synchronized (MSMsgMap.get(m)) {
				MSMsgMap.get(m).add(e);
				MSMsgMap.get(m).notifyAll();
			}
			eventSubsMap.get(type).add(m);
			eventSubsMap.get(type).notifyAll();
			return ft;
		}

	}

	@Override
	public void register(MicroService m) {
		synchronized (register) {
			if (!isRegistered(m)) {
				MessageTypeSubs.put(m, new HashSet<>());
				MSMsgMap.put(m, new LinkedBlockingQueue<>());
			}
			register.notifyAll();
		}
	}

	@Override
	public void unregister(MicroService m) {
		synchronized (register) {
			if (isRegistered(m)) {
				Message msg;
				for (Class<? extends Message> type : MessageTypeSubs.get(m)) {
					try {
						msg = type.newInstance();
						if (msg instanceof Broadcast)
							synchronized (broadSubsMap.get(type)) {
								broadSubsMap.get(type).remove(m);
								broadSubsMap.get(type).notifyAll();
							}
						else
							synchronized (eventSubsMap.get(type)) {
								eventSubsMap.get(type).remove(m);
								eventSubsMap.get(type).notifyAll();
							}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				MessageTypeSubs.remove(m);
				for (Message message : MSMsgMap.get(m)) {
					if (message instanceof Event)
						sendEvent((Event) message);
				}
			}
			register.notifyAll();
		}
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		return MSMsgMap.get(m).take();
	}

	public <T> boolean checkEventSub(Event<T> e, MicroService m){
		synchronized (eventSubsMap){
			return eventSubsMap.get(e).contains(m);
		}
	}

	public boolean checkBroadSub(Broadcast b, MicroService m){
		synchronized (broadSubsMap){
			return eventSubsMap.get(b).contains(m);
		}
	}

	public boolean isRegistered(MicroService m) {
		synchronized (register) {
			return MSMsgMap.containsKey(m) && MessageTypeSubs.containsKey(m);
		}

	}
}
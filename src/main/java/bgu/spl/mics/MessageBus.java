package bgu.spl.mics;

/**
 * The message-bus is a shared object used for communication between
 * micro-services.
 * It should be implemented as a thread-safe singleton.
 * The message-bus implementation must be thread-safe as
 * it is shared between all the micro-services in the system.
 * You must not alter any of the given methods of this interface. 
 * You cannot add methods to this interface.
 */
public interface MessageBus {

    /**
     * Subscribes {@code m} to receive {@link Event}s of type {@code type}.
     * <p>
     * @param <T>  The type of the result expected by the completed event.
     * @param type The type to subscribe to,
     * @param m    The subscribing micro-service.
     */


    /**
     * @param type Class - the type of EVENT we want to sub
     * @param m Microservice - the microservice we want to preform the action on
     * @pre no condition
     * @post mb.messageMap.get(m).size() ++ for every microService subscribed
     */
    <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m);

    /**
     * Subscribes {@code m} to receive {@link Broadcast}s of type {@code type}.
     * <p>
     * @param type 	The type to subscribe to.
     * @param m    	The subscribing micro-service.
     */


    /**
     * @param type Class - the type of BROADCAST we want to sub
     * @param m Microservice - the microservice we want to preform the action on
     * @pre no condition
     * @post mb.messageMap.get(m).size() ++ for every microService subscribed
     */
    void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m);

    /**
     * Notifies the MessageBus that the event {@code e} is completed and its
     * result was {@code result}.
     * When this method is called, the message-bus will resolve the {@link Future}
     * object associated with {@link Event} {@code e}.
     * <p>
     * @param <T>    The type of the result expected by the completed event.
     * @param e      The completed event.
     * @param result The resolved result of the completed event.
     */



    /**
     * @param e Event - the event that we want to resolve
     * @param result T - the result we want to assign to the event
     * @pre a 'sendEvent' method was called
     * @post future.isDone==true, ftMap.size()--, future.get()==result
     */
    <T> void complete(Event<T> e, T result);

    /**
     * Adds the {@link Broadcast} {@code b} to the message queues of all the
     * micro-services subscribed to {@code b.getClass()}.
     * <p>
     * @param b 	The message to added to the queues.
     */


    /**
     * @param b Broadcast - the broadcast that we want to send
     * @pre atleast 1 microservice is subscribed to the broadcast
     * @post microMap.get(m).size() ++ (m - microService) for each microService subscribed
     */
    void sendBroadcast(Broadcast b);

    /**
     * Adds the {@link Event} {@code e} to the message queue of one of the
     * micro-services subscribed to {@code e.getClass()} in a round-robin
     * fashion. This method should be non-blocking.
     * <p>
     * @param <T>    	The type of the result expected by the event and its corresponding future object.
     * @param e     	The event to add to the queue.
     * @return {@link Future<T>} object to be resolved once the processing is complete,
     * 	       null in case no micro-service has subscribed to {@code e.getClass()}.
     */



    /**
     * @param e event - the event that we want to send
     * @pre atleast 1 microservice is subscribed to the event
     * @post microMap.get(m).size() ++ , ftMap.size() ++
     */
    <T> Future<T> sendEvent(Event<T> e);

    /**
     * Allocates a message-queue for the {@link MicroService} {@code m}.
     * <p>
     * @param m the micro-service to create a queue for.
     */



    /**
     * @param m Microservice - the microservice that we want to register to the bus
     * @pre no condition
     * @post microMap.size() ++
     */
    void register(MicroService m);

    /**
     * Removes the message queue allocated to {@code m} via the call to
     * {@link #register(bgu.spl.mics.MicroService)} and cleans all references
     * related to {@code m} in this message-bus. If {@code m} was not
     * registered, nothing should happen.
     * <p>
     * @param m the micro-service to unregister.
     */




    /**
     * @param m Microservice - the microservice that we want to unregister to the bus
     * @pre mb.isRegistered(m)==true
     * @post microMap.size() --
     */
    void unregister(MicroService m);

    /**
     * Using this method, a <b>registered</b> micro-service can take message
     * from its allocated queue.
     * This method is blocking meaning that if no messages
     * are available in the micro-service queue it
     * should wait until a message becomes available.
     * The method should throw the {@link IllegalStateException} in the case
     * where {@code m} was never registered.
     * <p>
     * @param m The micro-service requesting to take a message from its message
     *          queue.
     * @return The next message in the {@code m}'s queue (blocking).
     * @throws InterruptedException if interrupted while waiting for a message
     *                              to became available.
     */
    //@PRE: mb.isRegistered(m)==true
    //@POST: return Message & add to queue if possible


    /**
     * @param m Microservice - the microservice that waits for the message
     * @pre mb.isRegistered(m)==true
     * @post return Message & add to queue if possible
     */
    Message awaitMessage(MicroService m) throws InterruptedException;


    /**
     * @param e Event - an event that  we want to check if the microservice is subed to
     * @param m Microservice - the microservice we want to check
     * @return return T/F based on the subscription of m to e
     */
    <T> boolean checkEventSub(Event<T> e, MicroService m);

    /**
     * @param b Broadcast - a broadcast that  we want to check if the microservice is subed to
     * @param m Microservice - the microservice we want to check
     * @return return T/F based on the subscription of m to b
     */
    boolean checkBroadSub(Broadcast b, MicroService m);


    /**
     * @param m Microservice - a microservice, we want to check if it's registered
     * @return return T/F based on the registration of m
     */
    boolean isRegistered(MicroService m);







    
}

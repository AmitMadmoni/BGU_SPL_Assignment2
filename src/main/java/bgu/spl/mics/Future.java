package bgu.spl.mics;

import java.util.concurrent.TimeUnit;


/**
 * A Future object represents a promised result - an object that will
 * eventually be resolved to hold a result of some operation. The class allows
 * Retrieving the result once it is available.
 * 
 * Only private methods may be added to this class.
 * No public constructor is allowed except for the empty constructor.
 */
public class Future<T> {

	private boolean isDone;
	private T result;
	/**
	 * This should be the the only public constructor in this class.
	 */
	public Future() {
		isDone=false;
		result=null;
	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved.
     * This is a blocking method! It waits for the computation in case it has
     * not been completed.
     * <p>
     * @return return the result of type T if it is available, if not wait until it is available.
     * 	       
     */


	/**
	 * @return return this.result
	 */
	public T get() {
		synchronized (this) {
			try {
				while(!isDone) this.wait();
			} catch (InterruptedException e) { }
		}
		return result;
	}
	
	/**
     * Resolves the result of this Future object.
     */

	/**
	 * @pre no condition
	 * @post this.result==result & future.isDone()==True
	 */
	public void resolve (T result) {
		synchronized (this){
			isDone = true;
			this.result=result;
			//notifying that this future is resolved so that those
			//who are waiting will know it's time to "wake up"
			this.notifyAll();
		}
	}
	
	/**
     * @return true if this object has been resolved, false otherwise
     */


	/**
	 * @return return this.isDone
	 */
	public boolean isDone() {
		synchronized (this) {
			return isDone;
		}
	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved,
     * This method is non-blocking, it has a limited amount of time determined
     * by {@code timeout}
     * <p>
     * @param timeout 	the maximal amount of time units to wait for the result.
     * @param unit		the {@link TimeUnit} time units to wait.
     * @return return the result of type T if it is available, if not, 
     * 	       wait for {@code timeout} TimeUnits {@code unit}. If time has
     *         elapsed, return null.
     */



	/**
	 * @param timeout long - the time to be timed out
	 * @param unit TimeUnit - the unit of times we use
	 * @return return this.result
	 */
	public T get(long timeout, TimeUnit unit) {
		synchronized (this) {
			if (isDone)
				return result;
			try {
				this.wait(unit.toMillis(timeout));
				if (isDone)
					return result;
			} catch (InterruptedException exception) {
			}
		}
		return null;
	}
}

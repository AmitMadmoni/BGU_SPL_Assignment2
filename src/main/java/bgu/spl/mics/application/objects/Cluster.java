package bgu.spl.mics.application.objects;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.services.CPUService;

import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */

public class Cluster {

	private LinkedList<GPU> GPUCollection;
	private LinkedList<CPU> CPUCollection;
	private ConcurrentLinkedQueue<DataBatch> unprocessed;
	private ConcurrentLinkedQueue<CPUService> freeCPU;
	private LinkedBlockingQueue<DataBatch> processed=new LinkedBlockingQueue<DataBatch>();

	//for statistics
	private LinkedList<String> nameList;     //name of the models   //is taken care from the gpu
	private int batchesCount;            //is taken care from the cpu
	private int CPUTime;             //is taken care from the cpu
	private int GPUTime;             //is taken care from the gpu

	public Cluster() {
		GPUCollection = new LinkedList<GPU>();
		CPUCollection = new LinkedList<CPU>();
		//processed = new LinkedList<Object>();
		unprocessed = new ConcurrentLinkedQueue<DataBatch>();
		freeCPU = new ConcurrentLinkedQueue<CPUService>();
		nameList = new LinkedList<String>();
		batchesCount = 0;
		CPUTime = 0;
		GPUTime = 0;
	}

	private static class ClusterHolder{
		private static Cluster instance=new Cluster();
	}

	public Cluster(LinkedList<GPU> GPUCollection, LinkedList<CPU> CPUCollection) {
		this.GPUCollection = GPUCollection;
		this.CPUCollection = CPUCollection;
		this.freeCPU = new ConcurrentLinkedQueue<CPUService>();
		this.unprocessed = new ConcurrentLinkedQueue<DataBatch>();
		this.nameList = new LinkedList<String>();
		this.batchesCount = 0;
		this.CPUTime = 0;
		this.GPUTime = 0;
	}

	public synchronized DataBatch getNext(){
		if (!this.unprocessed.isEmpty())
			return unprocessed.remove();
		return null;
	}

	public LinkedBlockingQueue<DataBatch> getProcessed() {
		return processed;
	}

	public void setGPUCollection(LinkedList<GPU> GPUCollection) {this.GPUCollection = GPUCollection;}

	public void setCPUCollection(LinkedList<CPU> CPUCollection) {this.CPUCollection = CPUCollection;}

	public LinkedList<String> getNameList() {return nameList;}

	public void addGPUTime(int add){this.GPUTime+=add;}

	public void addCPUTime(int add){this.CPUTime+=add;}

	public void addBatchesCount(int add){this.batchesCount+=add;}

	public void addName(String add){this.nameList.add(add);}

	public void addGPUCollection(GPU add){this.GPUCollection.add(add);}

	public void addCPUCollection(CPU add){this.CPUCollection.add(add);}

	public ConcurrentLinkedQueue<DataBatch> getUnprocessed() { return unprocessed; }

	public int getBatchesCount() {return batchesCount;}

	public int getCPUTime() {return CPUTime;}

	public int getGPUTime() {return GPUTime;}

	/**
     * Retrieves the single instance of this class.
     */
	public static Cluster getInstance() {
		if(Cluster.ClusterHolder.instance==null)
			Cluster.ClusterHolder.instance = new Cluster();
		return Cluster.ClusterHolder.instance;
	}

	//add a cpuservice to the list of free cpus
	public void addService(CPUService service){
		freeCPU.add(service);
	}

	public void BackToGPU(DataBatch data){
		synchronized (data.getMyGPU().getPd()) {
			data.getMyGPU().receievePD(data);
			data.getMyGPU().getPd().notifyAll();
		}
	}
}



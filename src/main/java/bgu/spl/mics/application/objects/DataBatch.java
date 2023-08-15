package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */

public class DataBatch {
    private Data data;
    private int index;
    private GPU myGPU;

    public DataBatch(){
        data=new Data();
        index=0;
    }

    public DataBatch(Data data, int index, GPU gpu){
        this.data=data;
        this.index=index;
        this.myGPU=gpu;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public GPU getMyGPU() {
        return myGPU;
    }

    public void setMyGPU(GPU myGPU) {
        this.myGPU = myGPU;
    }
}

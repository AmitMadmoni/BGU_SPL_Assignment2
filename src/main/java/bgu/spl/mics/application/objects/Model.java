package bgu.spl.mics.application.objects;

/**
 * Passive object representing a Deep Learning model.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Model {

    public enum Status{
        PreTrained, Training, Trained, Tested
    }
    public enum Results{
        None, Good, Bad
    }

    private String name;
    private Data data;
    private Student student;
    private Status status;
    private Results result;
    private GPU gpu=null;

    public Model() {}

    public Model(String name, Data data, Student student, Status status, Results result) {
        this.name = name;
        this.data = data;
        this.student = student;
        this.status = status;
        this.result = result;
    }

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public Data getData() {return data;}

    public void setData(Data data) {this.data = data;}

    public Student getStudent() {return student;}

    public Status getStatus() {return status;}

    public void setStatus(Status status) {this.status = status;}

    public Results getResult() {return result;}

    public void setResult(Results result) {this.result = result;}

    public void setGpu(GPU gpu) {this.gpu = gpu;}

    public GPU getGpu() {
        return gpu;
    }
}

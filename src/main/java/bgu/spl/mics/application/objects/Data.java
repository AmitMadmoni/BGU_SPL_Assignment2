package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Data {
    /**
     * Enum representing the Data type.
     */
    public enum Type {
        Images, Text, Tabular
    }

    private Type type;
    private int processed;
    private int size;
    private Model model;

    public Data() {
        size=0;
        processed=0;
    }

    public Data(Type type, int processed, int size) {
        this.type = type;
        this.processed = processed;
        this.size = size;
        this.model=null;
    }

    public Type getType() {return type;}

    public void setType(Type type) {this.type = type;}

    public int getProcessed() {return processed;}

    public int getSize() {return size;}

    public void addProcessed(int add) {this.processed += add;}

    public Model getModel() {return model;}

    public void setModel(Model model) {
        this.model = model;
    }
}

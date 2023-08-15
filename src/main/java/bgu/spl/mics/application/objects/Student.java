package bgu.spl.mics.application.objects;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.services.StudentService;

import java.util.LinkedList;

/**
 * Passive object representing single student.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Student {
    /**
     * Enum representing the Degree the student is studying for.
     */
    public enum Degree {
        MSc, PhD
    }

    private String name;
    private String department;
    private Degree status;
    private int publications;
    private int papersRead;
    private LinkedList<Model> models;
    private StudentService service;
    private LinkedList<Event> toBeSent;


    public Student(String name, String department, Degree status,LinkedList<Model> models) {
        this.name = name;
        this.department = department;
        this.status = status;
        this.publications = 0;
        this.papersRead = 0;
        this.models = models;
        service=new StudentService("studentService");
        service.setStudent(this);
        toBeSent = new LinkedList<Event>();

    }
    public void RefreshToBeSent(){
        for(Model model : models){      //for each model we add a trainmodelevent
            toBeSent.addLast(new TrainModelEvent(this, model.getName(),model.getData(),model));
        }
//        for(Event e : toBeSent){
//            System.out.println(((TrainModelEvent)e).getModel().getName());
//        }
    }

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public String getDepartment() {return department;}

    public void setDepartment(String department) {this.department = department;}

    public Degree getStatus() {return status;}

    public void setStatus(Degree status) {this.status = status;}

    public int getPublications() {return publications;}

    public void setPublications(int publications) {this.publications = publications;}

    public int getPapersRead() {return papersRead;}

    public void setPapersRead(int papersRead) {this.papersRead = papersRead;}

    public void addPapersRead(int add) {this.papersRead += add;}

    public void addPublications(int add) {this.publications += add;}

    public LinkedList<Model> getModels() {return models;}

    public void setModels(LinkedList<Model> models) {this.models = models;}

    public StudentService getService() {return service;}

    public String nameToString(){
        if(status==Degree.MSc)
            return "MSc";
        else
            return "PhD";
    }

    public void setService(StudentService service) {this.service = service;}

    public Event getNext(){
        if(!toBeSent.isEmpty()){
            return toBeSent.remove();
        }
        return null;
    }
}

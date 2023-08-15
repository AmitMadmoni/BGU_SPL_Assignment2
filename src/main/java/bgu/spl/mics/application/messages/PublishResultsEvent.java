package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

public class PublishResultsEvent implements Event {
    private Student student;
    private String name;      //name of the event
    private Data data;
    private Model model;

    public PublishResultsEvent(Student student, String name, Data data,Model model) {
        this.student = student;
        this.name = name;
        this.data = data;
        this.model = model;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Model getModel() {return model;}

    public void setModel(Model model) {this.model = model;}
}

package bgu.spl.mics.application.services;

import bgu.spl.mics.Event;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

import java.util.LinkedList;

/**
 * Student is responsible for sending the {@link TrainModelEvent},
 * {@link TestModelEvent} and {@link PublishResultsEvent}.
 * In addition, it must sign up for the conference publication broadcasts.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */


public class StudentService extends MicroService {

    private Student student;
    private LinkedList<Model> incoming = new LinkedList<Model>();

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public StudentService(String name) {
        super(name);
    }

    @Override
    protected void initialize() {
        this.sendEvent(getStudent().getNext());
        subscribeBroadcast(FinishWork.class,(FinishWork)->{terminate();});

        subscribeBroadcast(PublishConfrenceBroadcast.class, (PublishConfrenceBroadcast b)->{
            for(Model model : b.getModels()){
                if(student.getModels().contains(model)) {
                    getStudent().addPublications(1);
                }
                else {
                    getStudent().addPapersRead(1);
                }
            }
        });

    }

    public void beforeStart(){}

}

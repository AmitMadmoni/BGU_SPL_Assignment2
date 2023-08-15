package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.services.ConferenceService;

import java.util.LinkedList;

/**
 * Passive object representing information on a conference.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class ConfrenceInformation {

    private String name;
    private int date;
    private LinkedList<Model> publishedModels;
    private LinkedList<Model> archive;
    private ConferenceService service;

    public ConfrenceInformation(String name, int date) {
        this.name = name;
        this.date = date;
        publishedModels = new LinkedList<Model>();
        service = new ConferenceService(name,date);
        archive = new LinkedList<Model>();
        service.setConference(this);
    }

    public String getName() {return name;}

    public void setService(ConferenceService service) {this.service = service;}

    public void setName(String name) {this.name = name;}

    public LinkedList<Model> getPublishedModels() {return publishedModels;}

    public void addPublishedModel(Model model) {publishedModels.add(model);}

    public int getDate() {return date;}

    public void Archive() {
        for(Model model : publishedModels){
            archive.addLast(model);
        }
    }

    public LinkedList<Model> getArchive() {return archive;}

    public void setPublishedModels(LinkedList<Model> publishedModels) {
        this.publishedModels = publishedModels;
    }
}

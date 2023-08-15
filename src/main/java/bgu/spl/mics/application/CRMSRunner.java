package bgu.spl.mics.application;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.*;
import com.google.gson.internal.bind.util.ISO8601Utils;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;

/** This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {

    public static void main(String[] args) throws IOException, InterruptedException {
         LinkedList<Student> studentsHolder = new LinkedList<Student>();
         LinkedList<GPU> gpusHolder = new LinkedList<GPU>();
         LinkedList<CPU> cpusHolder = new LinkedList<CPU>();
         LinkedList<ConfrenceInformation> conferencesHolder = new LinkedList<ConfrenceInformation>();
         LinkedList<Thread> threads = new LinkedList<>();
         int tickTime;
         int duration;
         Cluster cluster = Cluster.getInstance();
         MessageBusImpl mb = MessageBusImpl.getInstance();


        Gson gson = new Gson();
        // InputStream inputStream = CRMSRunner.class.getClassLoader().getResourceAsStream("example_input.json");
        Reader reader = new FileReader("example_input.json");

        JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

        //first we take care of all the students
        JsonArray students = jsonObject.getAsJsonArray("Students");
        for (JsonElement obj : students){

            //student name
            JsonElement jsonName =((JsonObject)obj).get("name");
            String name = gson.fromJson(jsonName,String.class);

            //department
            JsonElement jsonDepartment = ((JsonObject)obj).get("department");
            String department = gson.fromJson(jsonDepartment,String.class);

            //status
            JsonElement jsonStatus = ((JsonObject)obj).get("status");
            Student.Degree status = gson.fromJson(jsonStatus,Student.Degree.class);

            //init student
            LinkedList<Model> tmpModels = new LinkedList<Model>(); //just for student constructor
            Student student = new Student(name,department,status,tmpModels);
            student.getService().beforeStart();

            //models
            LinkedList<Model> models = new LinkedList<Model>();
            JsonArray jasonModels = ((JsonObject)obj).getAsJsonArray("models");
            for (JsonElement mod : jasonModels) {

                //name of model
                JsonElement jsonModelName = ((JsonObject) mod).get("name");
                String modelName = gson.fromJson(jsonModelName, String.class);

                //type of data
                JsonElement jsonDataType = ((JsonObject) mod).get("type");
                Data.Type dataType = gson.fromJson(jsonDataType, Data.Type.class);

                //size of data
                JsonElement jsonDataSize = ((JsonObject) mod).get("size");
                int dataSize = gson.fromJson(jsonDataSize, int.class);

                //init data
                Data data = new Data(dataType, 0, dataSize);

                //init model
                Model model = new Model(modelName, data, student, Model.Status.PreTrained, Model.Results.None);
                data.setModel(model);
                models.add(model);


            }//end 'for' of the models
            student.setModels(models);
            student.RefreshToBeSent();
            studentsHolder.add(student);
            threads.add(new Thread(student.getService()));
        }//end 'for' of the students

        //taking care of the GPUs
        JsonArray GPUs = jsonObject.getAsJsonArray("GPUS");
        for(JsonElement gpu : GPUs){
            //type
            GPU.Type gpuType = gson.fromJson(gpu,GPU.Type.class);
            GPU tempGpu = new GPU(gpuType);
            gpusHolder.add(tempGpu);
            GPUService gpuService = new GPUService("GPUService",tempGpu);
            tempGpu.setService(gpuService);
            threads.add(new Thread(gpuService));
            mb.register(gpuService);
            tempGpu.getService().beforeStart();
        }

        //taking care of the CPUs
        JsonArray CPUs = jsonObject.getAsJsonArray("CPUS");
        for(JsonElement cpu : CPUs){
            //num of cores
            int cpuCores = cpu.getAsInt();
            CPU tempCpu = new CPU(cpuCores);      //in the cpu constructor we need to add the cpu to the cluster's list of cpu
            cpusHolder.add(tempCpu);
            CPUService cpuService = new CPUService("CPUService");
            tempCpu.setService(cpuService);
            cpuService.setCpu(tempCpu);
            cluster.addService(cpuService);
            threads.add(new Thread(cpuService));
            mb.register(cpuService);
            tempCpu.getService().beforeStart();
        }

        cluster.setCPUCollection(cpusHolder);
        cluster.setGPUCollection(gpusHolder);

        //taking care of the conferences
        JsonArray Conferences = jsonObject.getAsJsonArray("Conferences");
        for(JsonElement conference : Conferences){
            //name
            JsonElement jsonName = ((JsonObject)conference).get("name");
            String name  = gson.fromJson(jsonName,String.class);
            //date
            JsonElement jsonDate = ((JsonObject)conference).get("date");
            int date  = gson.fromJson(jsonDate,int.class);

            ConfrenceInformation tempConference = new ConfrenceInformation(name, date);
            conferencesHolder.add(tempConference);
            ConferenceService conferenceService = new ConferenceService("conferenceService", date);
            conferenceService.setConference(tempConference);
            tempConference.setService(conferenceService);
            threads.add(new Thread(conferenceService));
            mb.register(conferenceService);
            conferenceService.beforeStart();
        }

        //tick time
        JsonElement tempTickTime = jsonObject.get("TickTime");
        tickTime = gson.fromJson(tempTickTime,int.class);

        //duration
        JsonElement tempDuration = jsonObject.get("Duration");
        duration = gson.fromJson(tempDuration,int.class);

        TimeService timeService = new TimeService();
        timeService.setDuration(duration);
        timeService.setSpeed(tickTime);
        Thread t = new Thread(timeService);
        threads.add(t);

        for(Thread th : threads)
            th.start();

        for(Thread th : threads)
            th.join();


        //output
        File file = new File("output.json");
        FileWriter writer = new FileWriter(file);
        PrintWriter print = new PrintWriter(writer);
        print.println("{");
        print.println("    \"students\": [");
        //print.println("        {");
        int studCount = 0;
        for(Student student:studentsHolder){
            studCount+=1;
            print.println("        {");
            print.println("          \"name\": \"" + student.getName() + "\",");
            print.println("          \"department\": \"" + student.getDepartment() + "\",");
            print.println("          \"status\": \"" + student.nameToString() + "\",");
            print.println("          \"publications\": " + student.getPublications() + ",");
            print.println("          \"papersRead\": " + student.getPapersRead() + ",");
            print.println("          \"trainedModels\": [");
            int numofModel = 0;
            for(Model model: student.getModels()){
                if(model.getStatus()== Model.Status.Tested || model.getStatus()== Model.Status.Trained)
                    numofModel+=1;
            }
            int count = 0;
            for(Model model: student.getModels()){
                if(model.getStatus()== Model.Status.Tested || model.getStatus()== Model.Status.Trained) {
                    count+=1;
                    print.println("              {");
                    print.println("                  \"name\": \"" + model.getName() + "\",");
                    print.println("                  \"data\": {");
                    print.println("                        \"type\": \"" + model.getData().getType() + "\",");
                    print.println("                        \"size\": " + model.getData().getSize());
                    print.println("                  },");
                    print.println("                  \"status\": \"" + model.getStatus() + "\",");
                    print.println("                  \"results\": \"" + model.getResult() + "\"");
                    if(count==numofModel)
                        print.println("              }");
                    else
                        print.println("              },");
                }

            }
            print.println("            ]");
            if(studCount==studentsHolder.size())
                print.println("        }");
            else
                print.println("        },");
        }
        print.println("    ],");
        print.println("    \"conferences\": [");
        int confCount = 0;
        for(ConfrenceInformation conference:conferencesHolder){
            confCount+=1;
            print.println("        {");
            print.println("          \"name\": \"" + conference.getName() + "\",");
            print.println("          \"date\": " + conference.getDate() + ",");
            print.println("          \"publications\": [");
            int count = 0;
            for(Model model:conference.getArchive()){
                count+=1;
                print.println("              {");
                print.println("                  \"name\": \"" + model.getName() + "\",");
                print.println("                  \"data\": {");
                print.println("                        \"type\": \"" + model.getData().getType() + "\",");
                print.println("                        \"size\": " + model.getData().getSize());
                print.println("                  },");
                print.println("                  \"status\": \"" + model.getStatus() + "\",");
                print.println("                  \"results\": \"" + model.getResult() + "\"");
                if(count==conference.getArchive().size())
                    print.println("              }");
                else
                    print.println("              },");
            }
            print.println("          ]");
            if(confCount==conferencesHolder.size())
                print.println("        }");
            else
                print.println("        },");
        }
        print.println("    ],");
        print.println("    \"cpuTimeUsed\":" + cluster.getCPUTime() + ",");
        print.println("    \"gpuTimeUsed\":" + cluster.getGPUTime() + ",");
        print.println("    \"batchesProcessed\":" + cluster.getBatchesCount());
        print.println("}");
        print.close();
        writer.close();

    }
}

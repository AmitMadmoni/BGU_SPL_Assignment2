package bgu.spl.mics.application.objects;

import static org.junit.jupiter.api.Assertions.*;

import bgu.spl.mics.application.services.GPUService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

import static bgu.spl.mics.application.objects.GPU.Type.*;

class GPUTest {

    private GPU gpu;

    @BeforeEach
    void setUp() {gpu=new GPU(RTX2080);}

    @AfterEach
    void tearDown() {}

    @Test
    void getType() {
        assertTrue(gpu.getType()==RTX2080);
    }

    @Test
    void action(){
        Data data = new Data(Data.Type.Text,0,1000);
        Model tmpMod = new Model("moshe",new Data(Data.Type.Text,0,1000),new Student("amit","madmach", Student.Degree.PhD,new LinkedList<Model>()), Model.Status.PreTrained, Model.Results.None);
        gpu.setModel(tmpMod);
        gpu.action(4);
        assertTrue(data.getProcessed()==1000);
        assertTrue(tmpMod.getStatus()== Model.Status.Training);
    }

    @Test
    void trainModel(){
        Model tmpMod = new Model("moshe",new Data(Data.Type.Text,0,1000),new Student("amit","madmach", Student.Degree.PhD,new LinkedList<Model>()), Model.Status.PreTrained, Model.Results.None);
        gpu.trainModel(tmpMod);
        assertTrue(tmpMod.getStatus()== Model.Status.Trained);
    }

    @Test
    void testModel(){
        Model tmpMod = new Model("moshe",new Data(Data.Type.Text,0,1000),new Student("amit","madmach", Student.Degree.PhD,new LinkedList<Model>()), Model.Status.PreTrained, Model.Results.None);
        gpu.trainModel(tmpMod);
        assertTrue(tmpMod.getStatus()== Model.Status.Trained);
        gpu.testModel(tmpMod);
        assertTrue(tmpMod.getStatus()== Model.Status.Tested);
    }



    @Test
    void doEveryTick(){
        assertFalse(gpu.getFinishTime()==gpu.getTime());
        Model mod = new Model();
        mod.setStatus(Model.Status.PreTrained);
        gpu.setModel(mod);
        gpu.getPd().add(new DataBatch(new Data(),0,gpu));
        gpu.doEveryTick();
        assertTrue(gpu.getFinishTime()==gpu.getTime() + gpu.getTicks());
    }

    @Test
    void receievePD(){
        assertTrue(gpu.getPd().size()==0);
        gpu.receievePD(new DataBatch(new Data(Data.Type.Text,0,1000),0,gpu));
        assertTrue(gpu.getPd().size()==1);
    }

    @Test
    void getUpd(){
        assertTrue(gpu.getUpd().size()==0);
        gpu.getUpd().add(new DataBatch(new Data(Data.Type.Text,0,1000),0,gpu));
        assertTrue(gpu.getUpd().size()==1);
    }

    @Test
    void canInsert(){
        assertTrue(gpu.canInsert()==true);
    }

    @Test
    void getPd(){
        assertTrue(gpu.getPd().size()==0);
        gpu.getPd().add(new DataBatch(new Data(Data.Type.Text,0,1000),0,gpu));
        assertTrue(gpu.getPd().size()==1);
    }

    @Test
    void isBusy(){
        assertTrue(gpu.isBusy()==false);
    }

    @Test
    void getModel() {
        Model model = null;
        assertTrue(gpu.getModel().equals(model));
        model = new Model();
        gpu.setModel(model);
        assertTrue(gpu.getModel().equals(model));
    }

    @Test
    void setModel() {
        Model model = null;
        assertTrue(gpu.getModel().equals(model));
        model = new Model();
        gpu.setModel(model);
        assertTrue(gpu.getModel().equals(model));
    }

    @Test
    void UPD() {
        assertTrue(gpu.getPd().size()==0);
        Data data = new Data(Data.Type.Text,0,1000);
        DataBatch dt = new DataBatch(data,0,gpu);
        gpu.getUpd().add(dt);
        gpu.UPD(dt);
        assertTrue(gpu.getUpd().size()==0);
        assertTrue(Cluster.getInstance().getUnprocessed().size()==1);
    }



    @Test
    void getService(){
        GPUService tmpSer = new GPUService("gpuservice",gpu);
        gpu.setService(tmpSer);
        assertTrue(gpu.getService().equals(tmpSer));
    }

    @Test
    void setService(){
        GPUService tmpSer = new GPUService("gpuservice",gpu);
        gpu.setService(tmpSer);
        assertTrue(gpu.getService().equals(tmpSer));
    }
}


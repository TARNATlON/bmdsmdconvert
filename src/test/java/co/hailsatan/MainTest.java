package co.hailsatan;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

// These tests are kind of simple because this is my first time writing a unit test for anything,
// and I really want to get this project on GitHub soon because this has been basically done for a while now.
public class MainTest {
    private static final String modelSMD = "src/test/resources/monkey_test.smd";
    private static final String modelBMD = "src/test/resources/monkey_test.bmd";
    private static final String animationSMD = "src/test/resources/monkey_animation_test.smd";
    private static final String animationBMD = "src/test/resources/monkey_animation_test.bmd";

    @Test
    public void test() {
        System.out.println(System.getProperty("user.dir"));
        //First test, model SMD to BMD
        modelSMDBMD();

        //Second test, model BMD to SMD
        modelBMDSMD();

        //Third test, animation SMD to BMD
        animationSMDBMD();

        //Fourth test, animation BMD to SMD
        animationBMDSMD();
    }

    private void modelSMDBMD() {
        Model model = Main.SMDToModel(modelSMD);
        Main.ModelToBMD(model, modelBMD + ".test", false);

        byte[] expectedOutput = null;
        byte[] realOutput = null;
        try {
            expectedOutput = new DataInputStream(new FileInputStream(modelBMD)).readAllBytes();
            realOutput = new DataInputStream(new FileInputStream(modelBMD + ".test")).readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertArrayEquals(expectedOutput, realOutput);
    }

    private void modelBMDSMD() {
        Model model = Main.BMDToModel(modelBMD);
        Main.ModelToSMD(model, modelSMD + ".test");

        byte[] expectedOutput = null;
        byte[] realOutput = null;
        try {
            expectedOutput = new DataInputStream(new FileInputStream(modelSMD)).readAllBytes();
            realOutput = new DataInputStream(new FileInputStream(modelSMD + ".test")).readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertArrayEquals(expectedOutput, realOutput);
    }

    private void animationSMDBMD() {
        Model model = Main.SMDToModel(animationSMD);
        Main.ModelToBMD(model, animationBMD + ".test", false);

        byte[] expectedOutput = null;
        byte[] realOutput = null;
        try {
            expectedOutput = new DataInputStream(new FileInputStream(animationBMD)).readAllBytes();
            realOutput = new DataInputStream(new FileInputStream(animationBMD + ".test")).readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertArrayEquals(expectedOutput, realOutput);
    }

    private void animationBMDSMD() {
        Model model = Main.BMDToModel(animationBMD);
        Main.ModelToSMD(model, animationSMD + ".test");

        byte[] expectedOutput = null;
        byte[] realOutput = null;
        try {
            expectedOutput = new DataInputStream(new FileInputStream(animationSMD)).readAllBytes();
            realOutput = new DataInputStream(new FileInputStream(animationSMD + ".test")).readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertArrayEquals(expectedOutput, realOutput);
    }
}

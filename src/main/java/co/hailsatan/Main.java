package co.hailsatan;

import org.apache.commons.cli.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        Options options = new Options();

        Option input = new Option("i", "input", true, "Input file");
        input.setRequired(true);
        options.addOption(input);

        Option output = new Option("o", "output", true, "Output file");
        output.setRequired(true);
        options.addOption(output);

        Option format = new Option("f", "format", true, "The format to write the output file in. " +
                "Will assume input file is the opposite format. Valid options are \"SMD\", \"smd\", \"BMD\", and \"bmd\"");
        format.setRequired(true);
        options.addOption(format);

        Option thisIsHilarious = new Option("b00b", "b00b", false, "Writes hex B00B in the BMD format in some " +
                "places Pixelmon doesn't read from. :D");
        thisIsHilarious.setRequired(false);
        options.addOption(thisIsHilarious);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter help = new HelpFormatter();
        CommandLine arguments = null;

        try {
            arguments = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            help.printHelp("bmdsmdconvert -i input -o output -f format [-b00b]", options);
            System.exit(1);
        }

//        for (String argument : args) {
//            if (argument.equals("-h") || argument.equals("--h") || argument.equals("-help") || argument.equals("--help")) {
//                printHelp();
//            }
//        }
//
//        if (args.length != 3) {
//            printHelp();
//        }
//
//        Boolean toSMD = null;
//        if (args[0].contains("-s")) {
//            toSMD = true;
//        } else if (args[0].contains("-b")) {
//            toSMD = false;
//        } else {
//            printHelp();
//        }
//
//        String inputFilePath = args[1];
//        String outputFilePath = args[2];

        String inputFilePath = arguments.getOptionValue("input");
        String outputFilePath = arguments.getOptionValue("output");

        boolean noReallyImVeryFunny = arguments.hasOption("b00b");

        Model model;
        switch (arguments.getOptionValue("format")) {
            case "SMD":
            case "smd":
                System.out.println("Parsing input BMD file...");
                model = BMDToModel(inputFilePath);
                System.out.println("Writing output SMD file...");
                ModelToSMD(model, outputFilePath);
                break;
            case "BMD":
            case "bmd":
                System.out.println("Parsing input SMD file...");
                model = SMDToModel(inputFilePath);
                System.out.println("Writing output BMD file...");
                ModelToBMD(model, outputFilePath, noReallyImVeryFunny);
                break;
        }
        System.out.println("Done! :)");
    }

    protected static Model BMDToModel(String inputFilePath) {
        float[][][] skeletons = null;
        Bone[] bones = null;
        Face[] faces = null;
        boolean animation = false;

        try (DataInputStream in = new DataInputStream(new FileInputStream(inputFilePath))) {
            byte version = in.readByte();
            if (version != 1) {
                System.out.println("First byte in input file isn't a 0x01!\n" +
                        "This probably means the file isn't a proper BMD file. :(");
                System.exit(1);
            }

            int totalBones = in.readShort();
            bones = new Bone[totalBones];

            for (int boneNum = 0; boneNum < totalBones; boneNum++) {
                short boneID = in.readShort();
                short parentBone = in.readShort();
                String name = readNullTerm(in);

                Bone parent = (parentBone != -1) ? bones[parentBone] : null;
                bones[boneID] = new Bone(name, boneID, parent);
            }

            int totalSkeletons = in.readShort();
            if (totalSkeletons > 1) {
                System.out.println("Detected input as an animation file.");
                animation = true;
            } else {
                System.out.println("Detected input as a normal model file.");
            }

            Float[][][] skeletonsNulls = new Float[totalSkeletons][totalBones][6];
            for (int frameNum = 0; frameNum < totalSkeletons; frameNum++) {
                int bonesInSkeleton = in.readShort();

                if (bonesInSkeleton != totalBones && !animation) {
                    System.out.println("Somehow, the input file specified more or less bones in a skeleton\n" +
                            "than there are in the model, while not being an animation. This is invalid! >:(\n" +
                            "Number of bones in current skeleton: " + bonesInSkeleton + "\n" +
                            "Number of bones in model: " + totalBones);
                    System.exit(1);
                } else if (bonesInSkeleton > totalBones) {
                    System.out.println("Somehow, the input file specified more bones in a skeleton\n" +
                            "than there are in the model. This is invalid! >:(\n" +
                            "Number of bones in current skeleton: " + bonesInSkeleton + "\n" +
                            "Number of bones in model: " + totalBones);
                    System.exit(1);
                }

                Float[][] coordsAllBones = new Float[totalBones][6];
                int[] boneIDs = new int[bonesInSkeleton];
                for (int boneNum = 0; boneNum < bonesInSkeleton; boneNum++) {
                    short boneID = in.readShort();
                    float locX = in.readFloat();
                    float locY = in.readFloat();
                    float locZ = in.readFloat();
                    float rotX = in.readFloat();
                    float rotY = in.readFloat();
                    float rotZ = in.readFloat();
                    Float[] coords = new Float[]{locX, locY, locZ, rotX, rotY, rotZ};
                    coordsAllBones[boneID] = coords;
                    boneIDs[boneNum] = boneID;
                }

                skeletonsNulls[frameNum] = coordsAllBones;
            }

            skeletons = new float[totalSkeletons][totalBones][6];
            for (int skeletonNum = 0; skeletonNum < skeletonsNulls.length; skeletonNum++) {
                for (int boneNum = 0; boneNum < skeletonsNulls[skeletonNum].length; boneNum++) {
                    for (int coordNum = 0; coordNum < 6; coordNum++) {
                        if (skeletonsNulls[skeletonNum][boneNum][coordNum] != null) {
                            skeletons[skeletonNum][boneNum][coordNum] = skeletonsNulls[skeletonNum][boneNum][coordNum];
                        } else {
                            for (int i = 1; i < skeletons.length; i++) {
                                if (skeletonsNulls[skeletonNum - i][boneNum][coordNum] != null) {
                                    skeletons[skeletonNum][boneNum][coordNum] = skeletonsNulls[skeletonNum - i][boneNum][coordNum];
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if (!animation) {
                int totalMaterials = in.readShort();
                String[] materials = new String[totalMaterials];
                for (int matNum = 0; matNum < totalMaterials; matNum++) {
                    materials[matNum] = readNullTerm(in);
                }

                int totalFaces = in.readShort();
                faces = new Face[totalFaces];
                for (int faceNum = 0; faceNum < totalFaces; faceNum++) {
                    String mat = materials[in.readByte()];
                    Vertex[] vertices = new Vertex[3];

                    for (int vertexNum = 0; vertexNum < 3; vertexNum++) {
                        in.readShort();
                        float x = in.readFloat();
                        float y = in.readFloat();
                        float z = in.readFloat();
                        float xn = in.readFloat();
                        float yn = in.readFloat();
                        float zn = in.readFloat();
                        float u = in.readFloat();
                        float v = in.readFloat();

                        vertices[vertexNum] = new Vertex(x, y, z, xn, yn, zn, u, v);

                        byte links = in.readByte();
                        if (links > 0) {
                            vertices[vertexNum].linkedBoneIDs = new int[links];
                            vertices[vertexNum].linkedBoneWeights = new float[links];
                            for (int linkNum = 0; linkNum < links; linkNum++) {
                                int boneID = in.readShort();
                                float weight = in.readFloat();

                                vertices[vertexNum].linkedBoneIDs[linkNum] = boneID;
                                vertices[vertexNum].linkedBoneWeights[linkNum] = weight;
                            }
                        }
                    }
                    faces[faceNum] = new Face(vertices, mat);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Could not find file: " + inputFilePath + "\n" +
                    ":(");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Something's gone wrong, look at the stack trace above for details. :(");
            System.exit(1);
        }
        if (!animation) {
            return new Model(skeletons, bones, faces);
        } else {
            return new Model(skeletons, bones);
        }
    }

    protected static Model SMDToModel(String inputFilePath) {
        // These need to be ArrayLists and not arrays because the SMD format doesn't specify
        // how many, for example, bones there are in the model.
        ArrayList<float[][]> skeletons = new ArrayList<>();
        ArrayList<Bone> bones = new ArrayList<>();
        ArrayList<String> materials = new ArrayList<>();
        ArrayList<Face> faces = new ArrayList<>();
        boolean animation = false;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFilePath), StandardCharsets.US_ASCII))) {
            String line;

            if (!in.readLine().equals("version 1")) {
                System.out.println("First line in input file isn't \"version 1\"!\n" +
                        "This probably means the file isn't a proper SMD file. :(");
                System.exit(1);
            }

            if (!in.readLine().equals("nodes")) {
                System.out.println("Couldn't find bones list (nodes section in SMD format).\n" +
                        "This probably means the file isn't a proper SMD file. :(");
                System.exit(1);
            }

            while (!(line = in.readLine()).equals("end")) {
                String[] elements = line.split(" ");

                int boneID = Integer.parseInt(elements[0]);
                String name = elements[1].substring(1, elements[1].length() - 1); //Removes quotes
                int parentBoneID = Integer.parseInt(elements[2]);

                Bone parentBone = parentBoneID != -1 ? bones.get(parentBoneID) : null;
                bones.add(boneID, new Bone(name, boneID, parentBone));
            }

            if (!in.readLine().equals("skeleton")) {
                System.out.println("Couldn't find skeletons list (skeleton section in SMD format).\n" +
                        "This probably means the file isn't a proper SMD file. :(");
                System.exit(1);
            }

            int totalSkeletons = 0;
            while (!(line = in.readLine()).equals("end")) {
                totalSkeletons++;
                int currentSkeleton = Integer.parseInt(line.split(" ")[1]);

                float[][] coordsAllBones = new float[bones.size()][6];
                for (int boneNum = 0; boneNum < bones.size(); boneNum++) {
                    line = in.readLine();
                    String[] elements = line.split(" {2}");

                    int boneID = Integer.parseInt(elements[0]);
                    String[] locs = elements[1].split(" ");
                    String[] rots = elements[2].split(" ");
                    float[] coords = {Float.parseFloat(locs[0]), Float.parseFloat(locs[1]), Float.parseFloat(locs[2]),
                            Float.parseFloat(rots[0]), Float.parseFloat(rots[1]), Float.parseFloat(rots[2])};

                    coordsAllBones[boneID] = coords;
                }
                skeletons.add(currentSkeleton, coordsAllBones);
            }

            if (totalSkeletons > 1) {
                System.out.println("Detected input as an animation file.");
                animation = true;
            } else {
                System.out.println("Detected input as a normal model file.");
            }

            if (!animation) {
                if (!in.readLine().equals("triangles")) {
                    System.out.println("Couldn't find triangles list (triangles section in SMD format).\n" +
                            "This probably means the file isn't a proper SMD file. :(");
                    System.exit(1);
                }
                while (!(line = in.readLine()).equals("end")) {
                    if (!materials.contains(line)) {
                        materials.add(line);
                    }
                    String material = line;

                    Vertex[] vertices = new Vertex[3];
                    for (int vertexNum = 0; vertexNum < 3; vertexNum++) {
                        line = in.readLine();

                        String[] elements = line.split(" {2}");
                        String[] xyz = elements[1].split(" ");
                        String[] normXyz = elements[2].split(" ");
                        String[] uvAndLinks = elements[3].split(" ");

                        float x = Float.parseFloat(xyz[0]);
                        float y = Float.parseFloat(xyz[1]);
                        float z = Float.parseFloat(xyz[2]);
                        float xn = Float.parseFloat(normXyz[0]);
                        float yn = Float.parseFloat(normXyz[1]);
                        float zn = Float.parseFloat(normXyz[2]);
                        float u = Float.parseFloat(uvAndLinks[0]);
                        float v = Float.parseFloat(uvAndLinks[1]);

                        vertices[vertexNum] = new Vertex(x, y, z, xn, yn, zn, u, v);

                        int links = Integer.parseInt(uvAndLinks[2]);
                        if (links > 0) {
                            vertices[vertexNum].linkedBoneIDs = new int[links];
                            vertices[vertexNum].linkedBoneWeights = new float[links];
                            for (int linkNum = 0; (linkNum / 2) < links; linkNum += 2) { // Add two because SMD format does "boneID weight boneID weight"
                                int boneID = Integer.parseInt(uvAndLinks[3 + linkNum]);
                                float weight = Float.parseFloat(uvAndLinks[4 + linkNum]);

                                vertices[vertexNum].linkedBoneIDs[linkNum / 2] = boneID;
                                vertices[vertexNum].linkedBoneWeights[linkNum / 2] = weight;
                            }
                        }
                    }
                    faces.add(new Face(vertices, material));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Could not find file: " + inputFilePath + "\n" +
                    ":(");
            System.exit(1);
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            System.out.println("Something's gone wrong, look at the stack trace above for details. :(");
            System.exit(1);
        }

        float[][][] skeletonsArray = new float[skeletons.size()][bones.size()][6];
        Bone[] bonesArray = new Bone[bones.size()];
        Face[] facesArray = new Face[faces.size()];
        skeletonsArray = skeletons.toArray(skeletonsArray);
        bonesArray = bones.toArray(bonesArray);
        facesArray = faces.toArray(facesArray);

        if (!animation) {
            return new Model(skeletonsArray, bonesArray, facesArray);
        } else {
            return new Model(skeletonsArray, bonesArray);
        }
    }

    protected static void ModelToBMD(Model model, String outputFilePath, boolean CmonThisIsTotallyHilarious) {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(outputFilePath))) {
            out.writeByte(1);

            out.writeShort(model.bones.length);

            for (Bone bone : model.bones) {
                out.writeShort(bone.ID);
                out.writeShort(bone.getParentID());
                out.write(toNullTerm(bone.name));
            }

            out.writeShort(model.skeletons.length);

            for (int skeletonNum = 0; skeletonNum < model.skeletons.length; skeletonNum++) {
                int bonesInSkeleton = 0;
                float[][] skeleton = model.skeletons[skeletonNum];

                for (int boneNum = 0; boneNum < skeleton.length; boneNum++) {
                    float[] bone = skeleton[boneNum];

                    if (skeletonNum == 0 || !(Arrays.equals(bone, model.skeletons[skeletonNum - 1][boneNum]))) {
                        bonesInSkeleton++;
                    }
                }
                out.writeShort(bonesInSkeleton);

                for (int boneNum = 0; boneNum < skeleton.length; boneNum++) {
                    float[] bone = skeleton[boneNum];

                    if (skeletonNum == 0 || !(Arrays.equals(bone, model.skeletons[skeletonNum - 1][boneNum]))) {
                        out.writeShort(model.bones[boneNum].ID);
                        for (float value : bone) {
                            out.writeFloat(value);
                        }
                    }

                }
            }

            if (model.animation) {
                out.write(new byte[] {0x00, 0x00, 0x00, 0x00});
            }

            if (!model.animation) {
                ArrayList<String> materials = model.getMaterials();
                out.writeShort(materials.size());
                for (String mat : materials) {
                    out.write(toNullTerm(mat));
                }

                out.writeShort(model.faces.length);
                for (Face face : model.faces) {
                    out.writeByte(materials.indexOf(face.material));
                    for (Vertex vertex : face.vertices) {
                        // So this next short isn't read by the Pixelmon mod at all. I mean it is, but no variable is
                        // assigned its value, and it's not used for anything.
                        // The only reasonable thing to do in this case is write hex B00B in it. :D
                        if (CmonThisIsTotallyHilarious) {
                            byte[] hexBoob = new byte[2];
                            hexBoob[0] = (byte) 0xB0;
                            hexBoob[1] = (byte) 0x0B;
                            out.write(hexBoob);
                        } else {
                            out.writeShort(0);
                        }

                        out.writeFloat(vertex.x);
                        out.writeFloat(vertex.y);
                        out.writeFloat(vertex.z);
                        out.writeFloat(vertex.xn);
                        out.writeFloat(vertex.yn);
                        out.writeFloat(vertex.zn);
                        out.writeFloat(vertex.u);
                        out.writeFloat(vertex.v);

                        out.writeByte(vertex.linkedBoneIDs != null ? vertex.linkedBoneIDs.length : 0);
                        if (vertex.linkedBoneIDs != null && vertex.linkedBoneIDs.length > 0) {
                            for (int linkNum = 0; linkNum < vertex.linkedBoneIDs.length; linkNum++) {
                                out.writeShort(vertex.linkedBoneIDs[linkNum]);
                                out.writeFloat(vertex.linkedBoneWeights[linkNum]);
                            }
                        }
                    }
                }

            }


        } catch (FileNotFoundException e) {
            System.out.println("Could not find file: " + outputFilePath + "\n" +
                    ":(");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Something's gone wrong, look at the stack trace above for details. :(");
            System.exit(1);
        }
    }

    protected static void ModelToSMD(Model model, String outputFilePath) {
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFilePath), StandardCharsets.US_ASCII))) {
            out.write("version 1");
            out.newLine();

            out.write("nodes");
            out.newLine();

            for (Bone bone : model.bones) {
                String line = String.format("%d \"%s\" %d", bone.ID, bone.name, bone.getParentID());
                out.write(line);
                out.newLine();
            }
            out.write("end");
            out.newLine();

            out.write("skeleton");
            out.newLine();

            for (int frame = 0; frame < model.skeletons.length; frame++) {
                out.write("time " + frame);
                out.newLine();

                for (Bone bone : model.bones) {
                    String line;
                    float locX, locY, locZ, rotX, rotY, rotZ;

                    if (bone.ID == 0) {
                        line = "0  0 0 0  0 0 0";
                    } else {
                        float[] locsAndRots = model.skeletons[frame][bone.ID];
                        locX = locsAndRots[0];
                        locY = locsAndRots[1];
                        locZ = locsAndRots[2];
                        rotX = locsAndRots[3];
                        rotY = locsAndRots[4];
                        rotZ = locsAndRots[5];
                        line = String.format("%d  %.6f %.6f %.6f  %.6f %.6f %.6f", bone.ID, locX, locY, locZ, rotX, rotY, rotZ);
                    }
                    out.write(line);
                    out.newLine();
                }
            }

            out.write("end");
            out.newLine();

            if (!model.animation) {
                out.write("triangles");
                out.newLine();

                for (Face face : model.faces) {
                    out.write(face.material);
                    out.newLine();

                    for (int vertexNum = 0; vertexNum < 3; vertexNum++) {
                        Vertex vertex = face.vertices[vertexNum];
                        float x, y, z, xn, yn, zn, u, v;
                        x = vertex.x;
                        y = vertex.y;
                        z = vertex.z;
                        xn = vertex.xn;
                        yn = vertex.yn;
                        zn = vertex.zn;
                        u = vertex.u;
                        v = vertex.v;
                        int links = vertex.linkedBoneIDs != null ? vertex.linkedBoneIDs.length : 0;

                        String line = String.format("0  %.6f %.6f %.6f  %.6f %.6f %.6f  %.6f %.6f %d", x, y, z, xn, yn, zn, u, v, links);

                        if (vertex.linkedBoneIDs != null) {
                            for (int linkNum = 0; linkNum < links; linkNum++) {
                                line = String.format("%s %d %.6f", line, vertex.linkedBoneIDs[linkNum], vertex.linkedBoneWeights[linkNum]);
                            }
                        }

                        out.write(line);
                        out.newLine();
                    }
                }
                out.write("end");
                out.newLine();
            }
        } catch (FileNotFoundException e) {
            System.out.println("Could not find file: " + outputFilePath + "\n" +
                    ":(");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Something's gone wrong, look at the stack trace above for details. :(");
            System.exit(1);
        }
    }

    private static void printHelp() {
        System.out.println("Usage: bmdsmdconvert (-b or -s) [optionally -b00b] inputfile outputfile\n" +
                "-b is for SMD to BMD and -s for BMD to SMD, you must include one of them.\n" +
                "-b00b will write hex B00B in some spaces that Pixelmon doesn't use, for the BMD format\n" +
                ":)");
        System.exit(0);
    }

    private static String readNullTerm(DataInputStream in) throws IOException {
        StringBuilder string = new StringBuilder();
        char nullCharacter = Character.MIN_VALUE;
        while (true) {
            if (nullCharacter != '\000') {
                string.append(nullCharacter);
            }
            nullCharacter = in.readChar();
            if (nullCharacter == '\000') {
                return string.toString();
            }
        }
    }

    private static byte[] toNullTerm(String string) {
        byte[] stringBytes = string.getBytes(StandardCharsets.US_ASCII);
        byte[] nullTerm = new byte[(stringBytes.length * 2) + 2];

        for (int byteNum = 0; (byteNum / 2) < stringBytes.length; byteNum += 2) {
            nullTerm[byteNum] = 0;
            nullTerm[byteNum + 1] = stringBytes[byteNum / 2];
        }
        nullTerm[nullTerm.length - 2] = 0;
        nullTerm[nullTerm.length - 1] = 0;

        return nullTerm;
    }
}


//Trans rights
package co.hailsatan;

import java.util.ArrayList;

public class Model {
    public float[][][] skeletons;
    public Bone[] bones;
    public Face[] faces;
    public Boolean animation;

    public Model(float[][][] skeletons, Bone[] bones, Face[] faces) {
        this.skeletons = skeletons;
        this.bones = bones;
        this.faces = faces;
        this.animation = false;
    }

    public Model(float[][][] skeletons, Bone[] bones) {
        this.skeletons = skeletons;
        this.bones = bones;
        this.animation = true;
    }

    public ArrayList<String> getMaterials() {
        ArrayList<String> materials = new ArrayList<>();
        for (Face face : faces) {
            if (!materials.contains(face.material)) {
                materials.add(face.material);
            }
        }
        return materials;
    }
}

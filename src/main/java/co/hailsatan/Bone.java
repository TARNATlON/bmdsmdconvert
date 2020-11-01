package co.hailsatan;

public class Bone {
    public String name;
    public int ID;
    public Bone parent;

    public Bone(String name, int ID, Bone parent) {
        this.name = name;
        this.ID = ID;
        this.parent = parent;
    }

    public int getParentID() {
        return parent != null ? parent.ID : -1;
    }
}

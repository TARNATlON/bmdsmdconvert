package co.hailsatan;

public class Vertex {
    public float x;
    public float y;
    public float z;
    public float xn;
    public float yn;
    public float zn;
    public float u;
    public float v;

    public int[] linkedBoneIDs;
    public float[] linkedBoneWeights;

    public Vertex(float x, float y, float z, float xn, float yn, float zn, float u, float v) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.xn = xn;
        this.yn = yn;
        this.zn = zn;
        this.u = u;
        this.v = v;
    }
}

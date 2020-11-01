package co.hailsatan;

public class Face {
    public Vertex[] vertices;
    public String material;

    public Face(Vertex[] vertices, String material) {
        this.vertices = vertices;
        this.material = material;
    }
}

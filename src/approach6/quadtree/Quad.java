package approach6.quadtree;

public class Quad {

	public float x1;
	public float y1;
	public float w;
	public float h;
	public float area;
	public float x2;
	public float y2;
	public int brightness;

	Quad(float x1, float y1, float w, float h, int brightness) {
		this.x1 = x1;
		this.y1 = y1;
		this.w = w;
		this.h = h;
		this.brightness = brightness;
		area = w * h;
		x2 = x1 + w;
		y2 = y1 + h;
	}

	public boolean contains(float x, float y) {
		return x1 <= x && x2 >= x && y1 <= y && y2 >= y;
	}

}
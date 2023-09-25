package approach6.quadtree;

import processing.core.PApplet;
import processing.core.PImage;

import java.util.ArrayList;

import static java.lang.Math.*;

public class QuadTree {

	public int maxDepth;
	public int divisionTolerance;
	public PImage inputImage;
	private int[][] inputImageBrightness;
	public ArrayList<Quad> quads;

	public QuadTree(int maxDepth, int divisionTolerance) {
		this.maxDepth = maxDepth;
		this.divisionTolerance = divisionTolerance;
	}

	public void calculate(PImage inputImage) {

		// ------------------------------ calculate RGB values

		this.inputImage = inputImage;
		if (inputImageBrightness == null || inputImageBrightness.length != this.inputImage.width || inputImageBrightness[0].length != this.inputImage.height) {
			inputImageBrightness = new int[this.inputImage.width][this.inputImage.height];
		}
		for (int yPixel = 0; yPixel < this.inputImage.height; yPixel++) {
			for (int xPixel = 0; xPixel < this.inputImage.width; xPixel++) {
				int colour = this.inputImage.pixels[yPixel * this.inputImage.width + xPixel];
				int r = colour >> 16 & 255;
				int g = colour >> 8 & 255;
				int b = colour & 255;
				inputImageBrightness[xPixel][yPixel] = round(r * 0.299F + g * 0.587F + b * 0.114F);
			}
		}

		// ------------------------------ calculate quad tree

		quads = new ArrayList<>();
		calculateQuads(0, 0, 0, this.inputImage.width, this.inputImage.height);
	}

	private void calculateQuads(int divisionDepth, float x, float y, float w, float h) {
		int quadX = round(x);
		int quadY = round(y);
		int quadW = round(PApplet.min(w, inputImage.width - x - 1));
		int quadH = round(PApplet.min(h, inputImage.height - y - 1));
		int avgBrightness = getBrightnessAverage(quadX, quadY, quadW, quadH);
		divisionDepth++;
		if (divisionDepth <= maxDepth) {
			float variationBrightness = getBrightnessVariation(quadX, quadY, quadW, quadH, avgBrightness);
			if (variationBrightness > divisionTolerance) {
				w *= 0.5;
				h *= 0.5;
				calculateQuads(divisionDepth, x, y, w, h);
				calculateQuads(divisionDepth, x + w, y, w, h);
				calculateQuads(divisionDepth, x + w, y + h, w, h);
				calculateQuads(divisionDepth, x, y + h, w, h);
				return;
			}
		}
		quads.add(new Quad(x, y, w, h, avgBrightness));
	}

	private int getBrightnessAverage(int x, int y, int w, int h) {
		int avgBrightness = 0;
		for (int yPixel = y; yPixel < y + h; yPixel++) {
			for (int xPixel = x; xPixel < x + w; xPixel++) {
				avgBrightness += inputImageBrightness[xPixel][yPixel];
			}
		}
		avgBrightness = round(avgBrightness / (float) (w * h));
		return avgBrightness;
	}

	private float getBrightnessVariation(int x, int y, int w, int h, int avgBrightness) {
		float sd = 0;
		for (int yPixel = y; yPixel < y + h; yPixel++) {
			for (int xPixel = x; xPixel < x + w; xPixel++) {
				float dif = inputImageBrightness[xPixel][yPixel] - avgBrightness;
				sd += dif * dif;
			}
		}
		sd /= (float) (w * h);
		sd = (float) Math.sqrt(sd);
		return sd;
	}

}
package approach6;

import approach6.quadtree.Quad;
import approach6.quadtree.QuadTree;
import config.Portraits;
import processing.core.*;
import Utils.Utilities;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Portraitor {
	private static int INPUT_IMAGE_RESIZE = config.Portraits.INPUT_IMAGE_RESIZE;
	private static int THRESHOLD_BRIGHTNESS_MIN = Portraits.THRESHOLD_BRIGHTNESS_MIN;
	private static int THRESHOLD_BRIGHTNESS_MAX = config.Portraits.THRESHOLD_BRIGHTNESS_MAX;
	private static int QUAD_TREE_DEPTH_MAX = Portraits.QUADTREE_SETUP_DEPTH_MAX;
	private static int QUAD_TREE_BRIGHTNESS_DIVISION_TOLERANCE = Portraits.QUADTREE_SETUP_BRIGHTNESS_DIVISION_TOLERANCE;
	private static int QUAD_TREE_BRIGHTNESS_THRESHOLD_FILTER = Portraits.QUADTREE_SETUP_BRIGHTNESS_THRESHOLD_FILTER;
	private static int[] FONT_SIZES =  config.Portraits.FONT_SIZES;
	private static float ELEMENTS_ANGLE_MAX = Portraits.ELE_MENTS_ANGLE_MAX;
	private static int PLACEMENT_ATTEMPTS_BLACK = (int) Portraits.PLACEMENT_BLACK_ATTEMPTS;
	private static int PLACEMENT_ATTEMPTS_WHITE = (int) config.Portraits.PLACEMENT_WHITE_ATTEMPTS;
	private static float PLACEMENT_MIN_AREA_ON_INK_BLACK = Portraits.PLACEMENT_BLACK_MIN_AREA_ON_INK_BLACK;
	private static float PLACEMENT_MIN_AREA_ON_INK_WHITE = Portraits.PLACEMENT_WHITE_MIN_AREA_ON_INK_WHITE;
	private static float PLACEMENT_OUTLINE_BLACK = Portraits.PLACEMENT_BLACK_OUTLINE;
	private static float PLACEMENT_OUTLINE_WHITE =  Portraits.PLACEMENT_WHITE_OUTLINE;
	private final static String CHARACTERS = config.Portraits.CHARACTERS;
	public final static String FONT_PATH = config.Portraits.FONT_PATH;

	private PApplet p5;
	private Random random;
	private PShape[][] glyphs;
	private float[][][][] glyphsPoints;
	public PImage imageOriginal, imageProcessed;
	public PGraphics canvas;
	public QuadTree quadTree;
	public ArrayList<Quad> quads;
	private ArrayList<Element> elements;
	private volatile boolean calculating = false;
	private Logger logger;


	public Portraitor(PApplet p5, Logger logger) {
		this.p5 = p5;
		random = new Random();
		elements = new ArrayList<>();
		this.logger = logger;

		// create shapes of the glyphs
		glyphs = new PShape[FONT_SIZES.length][CHARACTERS.length()];
		for (int iSize = 0; iSize < FONT_SIZES.length; iSize++) {
			PFont font = p5.createFont(FONT_PATH, FONT_SIZES[iSize]);
			for (int iChar = 0; iChar < CHARACTERS.length(); iChar++) {
				PShape glyph = font.getShape(CHARACTERS.charAt(iChar), 0.01f);
				glyph.translate(-glyph.getWidth() * 0.5F, font.getSize() * 0.35f);
				glyph.disableStyle();
				glyphs[iSize][iChar] = glyph;
			}
		}

		// calculate points at the ink pixels of the glyphs
		glyphsPoints = new float[FONT_SIZES.length][CHARACTERS.length()][][];
		for (int iSize = 0; iSize < FONT_SIZES.length; iSize++) {
			for (int iChar = 0; iChar < CHARACTERS.length(); iChar++) {
				PGraphics pg = p5.createGraphics(Math.round(FONT_SIZES[iSize] * 1.1f), Math.round(FONT_SIZES[iSize] * 1.1f));
				pg.smooth(8);
				pg.beginDraw();
				pg.background(255);
				pg.noStroke();
				pg.fill(0);
				pg.shape(glyphs[iSize][iChar], pg.width * 0.5F, pg.height * 0.5F);
				pg.endDraw();
				ArrayList<float[]> inkPointsTemp = new ArrayList<>();
				for (int y = 0; y < pg.height; y++) {
					for (int x = 0; x < pg.width; x++) {
						float brightness = p5.brightness(pg.get(x, y));
						if (brightness < 255) {
							float pointX = x - pg.width * 0.5F;
							float pointY = y - pg.height * 0.5F;
							inkPointsTemp.add(new float[]{pointX, pointY, brightness});
						}
					}
				}
				glyphsPoints[iSize][iChar] = new float[inkPointsTemp.size()][3];
				for (int p = 0; p < inkPointsTemp.size(); p++) {
					System.arraycopy(inkPointsTemp.get(p), 0, glyphsPoints[iSize][iChar][p], 0, 3);
				}
			}
		}

		// create quad tree
		quadTree = new QuadTree(QUAD_TREE_DEPTH_MAX, QUAD_TREE_BRIGHTNESS_DIVISION_TOLERANCE);
	}


	public void portrait(PImage inputImage, boolean resizeImage) {
		waitToCalculate();

		// process input image
		imageOriginal = inputImage.get();
		if (resizeImage && INPUT_IMAGE_RESIZE > 0) {
			float[] resize = Utilities.resizeToFitInside(imageOriginal.width, imageOriginal.height, INPUT_IMAGE_RESIZE, INPUT_IMAGE_RESIZE);
			imageOriginal.resize((int) resize[0], (int) resize[1]);
		}
		imageProcessed = new PImage(imageOriginal.width, imageOriginal.height);
		for (int p = 0; p < imageOriginal.pixels.length; p++) {
			int c = imageOriginal.pixels[p];
			float brightnessIn = (c >> 16 & 255) * 0.299F + (c >> 8 & 255) * 0.587F + (c & 255) * 0.114F;
			float brightnessOut;
			if (brightnessIn <= THRESHOLD_BRIGHTNESS_MIN) {
				brightnessOut = 0;
			} else if (brightnessIn >= THRESHOLD_BRIGHTNESS_MAX) {
				brightnessOut = 255;
			} else {
				brightnessOut = PApplet.map(brightnessIn, THRESHOLD_BRIGHTNESS_MIN, THRESHOLD_BRIGHTNESS_MAX, 0, 255);
			}
			imageProcessed.pixels[p] = p5.color(brightnessOut);
		}
		imageProcessed.filter(PConstants.ERODE); // erosion makes objects in black bigger
		imageProcessed.filter(PConstants.DILATE); // dilatation makes objects in black smaller
		imageProcessed.filter(PConstants.ERODE);

		// calculate quad tree of the image processed
		quadTree.calculate(imageProcessed);

		// ignore quads with high average brightness
		quads = new ArrayList<>();
		for (Quad q : quadTree.quads) {
			if (q.brightness <= QUAD_TREE_BRIGHTNESS_THRESHOLD_FILTER) {
				quads.add(q);
			}
		}

		// sort quads by area in a descending order
		quads.sort((q1, q2) -> {
			if (q1.area == q2.area) return 0;
			return q1.area > q2.area ? -1 : 1;
		});

		// reset canvas and clear elements
		clear();
	}

	public void clear() {
		// reset canvas
		canvas = p5.createGraphics(imageProcessed.width, imageProcessed.height);
		canvas.smooth(8);
		canvas.beginDraw();
		canvas.set(0, 0, imageProcessed);
		canvas.noStroke();
		canvas.fill(255);
		canvas.strokeJoin(PConstants.ROUND);
		canvas.endDraw();

		// clear existing elements
		elements.clear();
	}

	public void placeElements() {
		if (calculating) return;
		new Thread(this::placeElementsImpl).start();
	}

	private void placeElementsImpl() {
		calculating = true;

//		debug
//		System.out.println("Adding elements");
//		int numElementsAdded = elements.size();
//		long timeInit = System.currentTimeMillis();

		for (Quad q : quads) {
			float brightnessRel = PApplet.map(q.brightness, 0, QUAD_TREE_BRIGHTNESS_THRESHOLD_FILTER, 0, 1);
			int numFontSizesUsable = (int) Math.ceil(PApplet.map(brightnessRel, 0, 1, FONT_SIZES.length, 0));
			float minPointsOnInk = PApplet.map(brightnessRel, 0, 1, PLACEMENT_MIN_AREA_ON_INK_BLACK, PLACEMENT_MIN_AREA_ON_INK_WHITE);
			float numMaxAttempts = PApplet.map(brightnessRel, 0, 1, PLACEMENT_ATTEMPTS_BLACK, PLACEMENT_ATTEMPTS_WHITE);

			for (int iSize = numFontSizesUsable - 1; iSize >= 0; iSize--) {
				int numConsecutiveFails = 0;
				trying:
				for (int iAttempt = 0; iAttempt < 10000; iAttempt++) {
					int indexCharacter = random.nextInt(CHARACTERS.length());
					float posX = q.x1 + (q.x2 - q.x1) * random.nextFloat();
					float posY = q.y1 + (q.y2 - q.y1) * random.nextFloat();
					float angle = ELEMENTS_ANGLE_MAX * 2 * random.nextFloat() - ELEMENTS_ANGLE_MAX;

					// translate and rotate ink points
					float cos = (float) Math.cos(angle);
					float sin = (float) Math.sin(angle);
					float[][] glyphPts = glyphsPoints[iSize][indexCharacter];
					float[][] glyphPtsTransf = new float[glyphPts.length][2];
					for (int p = 0; p < glyphPts.length; p++) {
						glyphPtsTransf[p][0] = posX + (glyphPts[p][0] * cos - glyphPts[p][1] * sin);
						if (glyphPtsTransf[p][0] < 0 || glyphPtsTransf[p][0] >= canvas.width) {
							continue trying;
						}
						glyphPtsTransf[p][1] = posY + (glyphPts[p][0] * sin + glyphPts[p][1] * cos);
						if (glyphPtsTransf[p][1] < 0 || glyphPtsTransf[p][1] >= canvas.height) {
							continue trying;
						}
					}

					int numPointsOnInk = 0;
					for (float[] p : glyphPtsTransf) {
						float brightness = p5.brightness(canvas.get(Math.round(p[0]), Math.round(p[1])));
						if (brightness < 240) {
							numPointsOnInk++;
						}
					}
					float percentagePointsOnInk = numPointsOnInk / (float) glyphPtsTransf.length;
					if (percentagePointsOnInk >= minPointsOnInk) {
						Element elemento = new Element(glyphs[iSize][indexCharacter], posX, posY, angle, FONT_SIZES[iSize], CHARACTERS.charAt(indexCharacter)); //UPDATED from animation screen
						elements.add(elemento);
						canvas.beginDraw();
						canvas.noStroke();
						canvas.fill(255);
						elemento.draw(canvas);
						canvas.stroke(255);
						canvas.strokeWeight(PApplet.map(brightnessRel, 0, 1, PLACEMENT_OUTLINE_BLACK, PLACEMENT_OUTLINE_WHITE));
						elemento.draw(canvas);
						canvas.endDraw();
						numConsecutiveFails = 0;
					} else {
						numConsecutiveFails++;
						if (numConsecutiveFails > numMaxAttempts) {
							break;
						}
					}
				}
			}
		}

		// debug
		// long timeDuration = System.currentTimeMillis() - timeInit;
		// numElementsAdded = elements.size() - numElementsAdded;
		// System.out.println(numElementsAdded + " elements added in " + timeDuration + " ms");
		calculating = false;
	}

	private void draw(PGraphics pg, float scaling) {
		pg.pushMatrix();
		pg.scale(scaling);
		pg.pushStyle();
		pg.noStroke();
		pg.fill(0);
		for (Element e : elements) {
			e.draw(pg);
		}
		pg.popStyle();
		pg.popMatrix();
	}

	public void draw(PGraphics pg, float x, float y, float boundingW, float boundingH, boolean centre) {
		waitToCalculate();
		if (elements.isEmpty()) return;
		float[] sizeRendering = Utilities.resizeToFitInside(canvas.width, canvas.height, boundingW, boundingH);
		pg.pushMatrix();
		pg.translate(x, y);
		if (centre) {
			pg.translate(-sizeRendering[0] * 0.5F, -sizeRendering[1] * 0.5F);
		}
		draw(pg, sizeRendering[0] / (float) canvas.width);
		pg.popMatrix();
	}

	public PImage getOutput(float boundingW, float boundingH, float marginRelative) {
		waitToCalculate();
		if (canvas == null) {
			return null;
		}
		float margin = PApplet.constrain(marginRelative, 0, 0.75f) * Math.max(boundingW, boundingH);
		float[] sizeRendering = Utilities.resizeToFitInside(canvas.width, canvas.height, boundingW - margin * 2, boundingH - margin * 2);
		PGraphics output = p5.createGraphics(Math.round(sizeRendering[0] + margin * 2), Math.round(sizeRendering[1] + margin * 2));
		output.smooth(8);
		output.beginDraw();
		output.background(255);
		output.translate(margin, margin);
		draw(output, sizeRendering[0] / (float) canvas.width);
		output.endDraw();
		return output;
	}

	// add for start screen aniatioon
	public ArrayList<Element> getElements() {
		return elements;
	}

	public float getAspectRatio() {
		return canvas.width / (float) canvas.height;
	}

	public void saveOutputPDF(boolean open) {
		saveOutput(Math.max(canvas.width, canvas.height), true, open);
	}

	public void saveOutputPNG(int boundingSize, boolean open) {
		saveOutput(boundingSize, false, open);
	}

	private void saveOutput(int boundingSize, boolean vector, boolean open) {
		waitToCalculate();

		// set path of output file
		Date currDate = new Date();
		String dateStamp = new SimpleDateFormat("yyyy-MM-dd").format(currDate);
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(currDate);
		Path pathOutputImage = Utilities.getCurrentDirectory().resolve("output").resolve(dateStamp).resolve(timeStamp + "." + (vector ? "pdf" : "png"));

		// create output file
		float marginRelative = 0.01f;
		int pixelDensityCopy = p5.pixelDensity;
		p5.pixelDensity = 1;
		if (vector) {
			boundingSize = Math.max(boundingSize, 100);
			float boundingSizePts = boundingSize * 2.83464567F;
			float margin = boundingSizePts * marginRelative;
			float[] sizeRendering = Utilities.resizeToFitInside(canvas.width, canvas.height, boundingSizePts - margin * 2, boundingSizePts - margin * 2);
			PGraphics output = p5.createGraphics(Math.round(sizeRendering[0] + margin * 2), Math.round(sizeRendering[1] + margin * 2), PConstants.PDF, pathOutputImage.toString());
			output.beginDraw();
			output.translate(margin, margin);
			draw(output, sizeRendering[0] / (float) canvas.width);
			output.dispose();
			output.endDraw();
		} else {
			boundingSize = Math.max(boundingSize, 500);
			getOutput(boundingSize, boundingSize, marginRelative).save(pathOutputImage.toString());
		}
		p5.pixelDensity = pixelDensityCopy;

		// open output file with default software
		if (open) {
			try {
				Desktop.getDesktop().open(pathOutputImage.toFile());
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage());
				System.exit(-1);
			}
		}
	}

	public void saveDebug() {
		waitToCalculate();

		// set path of output file
		Date currDate = new Date();
		String dateStamp = new SimpleDateFormat("yyyy-MM-dd").format(currDate);
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(currDate);
		Path pathOutputDir = Utilities.getCurrentDirectory().resolve("output").resolve(dateStamp).resolve(timeStamp + "-debug");

		// export input image
		imageOriginal.save(pathOutputDir.resolve("step1-imageInput.png").toString());

		// export image processed
		imageProcessed.save(pathOutputDir.resolve("step2-imageProcessed.png").toString());

		// export quad tree
		PGraphics pg = p5.createGraphics(imageProcessed.width, imageProcessed.height);
		pg.beginDraw();
		pg.background(255);
		pg.noStroke();
		for (Quad q : quadTree.quads) {
			pg.fill(q.brightness);
			pg.rect(q.x1, q.y1, q.w, q.h);
		}
		pg.endDraw();
		pg.save(pathOutputDir.resolve("step3-quadTree.png").toString());

		// export quad tree filtered
		pg.beginDraw();
		pg.background(200, 200, 255);
		pg.noStroke();
		for (Quad q : quads) {
			pg.fill(q.brightness);
			pg.rect(q.x1, q.y1, q.w, q.h);
		}
		pg.endDraw();
		pg.save(pathOutputDir.resolve("step4-quadTreeFiltered.png").toString());

		// export canvas
		canvas.save(pathOutputDir.resolve("step5-canvas.png").toString());

		// export rendering
		getOutput(canvas.width, canvas.height, 0).save(pathOutputDir.resolve("step6-rendering.png").toString());

		// open output dir
		try {
			Desktop.getDesktop().open(pathOutputDir.toFile());
		} catch (IOException e) {
			logger.log(Level.SEVERE, e.getMessage());
			System.exit(-1);
		}
	}


	public boolean calculating() {
		return calculating;
	}

	public void waitToCalculate() {
		// System.out.println("wait to calculate");
		while (calculating) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				logger.log(Level.WARNING, e.getMessage());;
			}
		}
	}

	public int getWidth() {
		return canvas.width;
	}

	public int getHeight() {
		return canvas.height;
	}

	public void updateParams () {
		INPUT_IMAGE_RESIZE = config.Portraits.INPUT_IMAGE_RESIZE;
		THRESHOLD_BRIGHTNESS_MIN = Portraits.THRESHOLD_BRIGHTNESS_MIN;
		THRESHOLD_BRIGHTNESS_MAX = config.Portraits.THRESHOLD_BRIGHTNESS_MAX;
		QUAD_TREE_DEPTH_MAX = Portraits.QUADTREE_SETUP_DEPTH_MAX;
		QUAD_TREE_BRIGHTNESS_DIVISION_TOLERANCE = Portraits.QUADTREE_SETUP_BRIGHTNESS_DIVISION_TOLERANCE;
		QUAD_TREE_BRIGHTNESS_THRESHOLD_FILTER = Portraits.QUADTREE_SETUP_BRIGHTNESS_THRESHOLD_FILTER;
		ELEMENTS_ANGLE_MAX = Portraits.ELE_MENTS_ANGLE_MAX;
		PLACEMENT_ATTEMPTS_BLACK = (int) Portraits.PLACEMENT_BLACK_ATTEMPTS;
		PLACEMENT_ATTEMPTS_WHITE = (int) config.Portraits.PLACEMENT_WHITE_ATTEMPTS;
		PLACEMENT_MIN_AREA_ON_INK_BLACK = Portraits.PLACEMENT_BLACK_MIN_AREA_ON_INK_BLACK;
		PLACEMENT_MIN_AREA_ON_INK_WHITE = Portraits.PLACEMENT_WHITE_MIN_AREA_ON_INK_WHITE;
		PLACEMENT_OUTLINE_BLACK = Portraits.PLACEMENT_BLACK_OUTLINE;
		PLACEMENT_OUTLINE_WHITE =  Portraits.PLACEMENT_WHITE_OUTLINE;

		quadTree = new QuadTree(QUAD_TREE_DEPTH_MAX, QUAD_TREE_BRIGHTNESS_DIVISION_TOLERANCE);

		logger.log(Level.INFO, "portrait params updated");

	}

	public void updateFontSizes () {
		FONT_SIZES =  config.Portraits.FONT_SIZES;

		// create shapes of the glyphs
		glyphs = new PShape[FONT_SIZES.length][CHARACTERS.length()];
		for (int iSize = 0; iSize < FONT_SIZES.length; iSize++) {
			PFont font = p5.createFont(FONT_PATH, FONT_SIZES[iSize]);
			for (int iChar = 0; iChar < CHARACTERS.length(); iChar++) {
				PShape glyph = font.getShape(CHARACTERS.charAt(iChar), 0.01f);
				glyph.translate(-glyph.getWidth() * 0.5F, font.getSize() * 0.35f);
				glyph.disableStyle();
				glyphs[iSize][iChar] = glyph;
			}
		}

		// calculate points at the ink pixels of the glyphs
		glyphsPoints = new float[FONT_SIZES.length][CHARACTERS.length()][][];
		for (int iSize = 0; iSize < FONT_SIZES.length; iSize++) {
			for (int iChar = 0; iChar < CHARACTERS.length(); iChar++) {
				PGraphics pg = p5.createGraphics(Math.round(FONT_SIZES[iSize] * 1.1f), Math.round(FONT_SIZES[iSize] * 1.1f));
				pg.smooth(8);
				pg.beginDraw();
				pg.background(255);
				pg.noStroke();
				pg.fill(0);
				pg.shape(glyphs[iSize][iChar], pg.width * 0.5F, pg.height * 0.5F);
				pg.endDraw();
				ArrayList<float[]> inkPointsTemp = new ArrayList<>();
				for (int y = 0; y < pg.height; y++) {
					for (int x = 0; x < pg.width; x++) {
						float brightness = p5.brightness(pg.get(x, y));
						if (brightness < 255) {
							float pointX = x - pg.width * 0.5F;
							float pointY = y - pg.height * 0.5F;
							inkPointsTemp.add(new float[]{pointX, pointY, brightness});
						}
					}
				}
				glyphsPoints[iSize][iChar] = new float[inkPointsTemp.size()][3];
				for (int p = 0; p < inkPointsTemp.size(); p++) {
					System.arraycopy(inkPointsTemp.get(p), 0, glyphsPoints[iSize][iChar][p], 0, 3);
				}
			}
		}

		logger.log(Level.INFO, "portrait font sizes updated");
	}

	public class Element {

		private PShape shape;
		public float x;
		public float y;
		public float angle;
		public String type;

		Element(PShape shape, float x, float y, float angle, int fontSize, char character) {
			this.shape = shape;
			this.x = x;
			this.y = y;
			this.angle = angle;
			type = fontSize + "" + character;
		}

		void draw(PGraphics pg) {
			pg.pushMatrix();
			pg.translate(x, y);
			pg.rotate(angle);
			pg.shape(shape, 0, 0);
			pg.popMatrix();
		}

	}
}
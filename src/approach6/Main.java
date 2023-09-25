package approach6;

import Utils.Utilities;
import Utils.logger.LogFilter;
import Utils.logger.LogFormatter;
import Utils.logger.LogHandler;
import approach6.quadtree.Quad;
import processing.core.PApplet;
import processing.core.PImage;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.*;

public class Main extends PApplet {

    private static final int MARGIN = 10;

    private Portraitor p;
    private PImage rendering;
    private int mode = 3;

    public static Logger logger = Logger.getLogger(Main.class.getName());

    public void settings() {
        smooth(8);
    }

    public void setup() {
        frameRate(60);
        p = new Portraitor(this, logger);
        PImage inputImage = loadImage("/Users/srebelo/Documents/localhost/fnp-typortraits/test/imgs/1.jpg");
        inputImage.resize((int) (inputImage.width * 1.25f), (int) (inputImage.height * 1.25f));
        p.portrait(inputImage, true);
        surface.setSize(p.canvas.width + MARGIN * 2, p.canvas.height + MARGIN * 2);
        surface.setLocation(displayWidth / 2 - width / 2, displayHeight / 2 - height / 2);
    }

    public void draw() {
        background(255);
        pushMatrix();
        translate(MARGIN, MARGIN);
        if (mode == 1) {
            image(p.imageOriginal, 0, 0);
        } else if (mode == 2) {
            image(p.imageProcessed, 0, 0);
        } else if (mode == 3) {
            noStroke();
            strokeWeight(0.5f);
            stroke(255);
            for (Quad q : p.quadTree.quads) {
                fill(q.brightness);
                rect(q.x1, q.y1, q.w, q.h);
            }
        } else if (mode == 4) {
            noStroke();
            fill(240, 240, 255);
            rect(0, 0, width - MARGIN * 2, height - MARGIN * 2);
            for (Quad q : p.quads) {
                fill(q.brightness);
                rect(q.x1, q.y1, q.w, q.h);
            }
        } else if (mode == 5) {
            image(p.canvas, 0, 0);
        } else {
            if (!p.calculating()) {
                rendering = p.getOutput(width - MARGIN * 2, height - MARGIN * 2, 0);
            }
            if (rendering != null) {
                image(rendering, 0, 0);
            }
        }
        popMatrix();

        if (p.calculating()) {
            pushMatrix();
            translate(width / 2, height / 2);
            scale(40 + sin(frameCount / 15f) * 10);
            rotate(frameCount * 0.05f);
            pushStyle();
            noStroke();
            fill(230, 50, 50);
            triangle(-0.5f, 0.289f, 0, -0.577f, 0.5f, 0.289f);
            popStyle();
            popMatrix();
        }
//		endRecord();
//		exit();
    }

    public void keyReleased() {
        if (key == '1') {
            mode = 1;
        } else if (key == '2') {
            mode = 2;
        } else if (key == '3') {
            mode = 3;
        } else if (key == '4') {
            mode = 4;
        } else if (key == '5') {
            mode = 5;
        } else if (key == '6') {
            mode = 6;
        } else if (key == ' ') {
            if (mode != 5 && mode != 6) {
                mode = 6;
            }
            p.placeElements();
        } else if (key == 'e') {
            p.saveOutputPDF(false);
            p.saveOutputPNG(2000, true);
        } else if (key == 'r') {
            p.clear();
        } else if (key == 'd') {
            p.saveDebug();
        }
    }

    public static void main(String args[]) {
        PApplet.main(Main.class.getName());

        // logger setup
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream("log.properties"));
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }

        // logger.setLevel(Level.CONFIG);
        logger.addHandler(new ConsoleHandler());
        logger.addHandler(new LogHandler());
        try {
            // FileHandler file name with max size and number of log files limit
            Handler fileHandler = new FileHandler(Utilities.getCurrentDirectory().resolve("log").resolve("logger.log").toString(), 100000, 100);
            fileHandler.setFormatter(new LogFormatter());
            // setting custom filter for FileHandler
            fileHandler.setFilter(new LogFilter());
            logger.addHandler(fileHandler);
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
    }

}
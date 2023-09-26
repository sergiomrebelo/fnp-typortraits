package pt.uc.dei.fnp;

import approach6.Portraitor;
import processing.core.*;
import pt.uc.dei.fnp.utils.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;

public class Main extends PApplet {
    static private Properties prop = new Properties();
    static private int MARGIN, BACKGROUND_COLOR = 0;
    static private boolean DEBUG = false;
    protected ArrayList<Container> containers = new ArrayList<>();
    protected ArrayList<Portraitor> portraitors = new ArrayList<>();
    public static Logger logger = Logger.getLogger(approach6.Main.class.getName());
    public FnpDataReader reader;

    public void settings() {
        // size(500, 500);
        fullScreen(1);
    }

    public void setup() {
        int nCols = parseInt(prop.getProperty("fnp.cols"));
        int nRows = parseInt(prop.getProperty("fnp.rows"));
        int [] lifespan = {
                parseInt(prop.getProperty("container.maxLifespan")),
                parseInt(prop.getProperty("container.minLifespan"))
        };
        float rectWidth = width/ (nCols * 1f);
        float rectHeight = height/ (nRows * 1f);

        MARGIN = parseInt(prop.getProperty("portrait.margin"));
        BACKGROUND_COLOR = parseInt(prop.getProperty("fnp.backgroundColor"));
        DEBUG = parseBoolean(prop.getProperty("debug"));
        System.out.println("DEBUG="+DEBUG);

        reader = new FnpDataReader("cam1_frame_grayscale_full", "cam1_face_detections");
        // reader2.setReadingsPerSecond(5);

        for (int i=0; i<nCols; i++) {
            for (int j=0; j<nRows; j++) {
                float x = i * rectWidth;
                float y = j * rectHeight;
                String s = this.getPImage();
                PImage img = loadImage(s);
                Container c = new Container(null, x, y, rectWidth, rectHeight, lifespan, this);
                Portraitor p = new Portraitor(this, logger);
                p.portrait(img, true);
                p.placeElements();
                if (DEBUG) {
                    c.turnOnDebug();
                    c.setCaption(s);
                }
                portraitors.add(p);
                containers.add(c);
            }
        }
    }


    public void draw () {
        // get frame
        PImage newFrame = reader.getValueAsPImage("cam1_frame_grayscale_full");
        System.out.println(newFrame);

        background(BACKGROUND_COLOR);
        for (int i=0; i<containers.size(); i++) {
            Container c = containers.get(i);
            Portraitor p = portraitors.get(i);
            if (c.alive) {
                c.draw();
            } else if (c.full) {
                // assign a new image to container
                String s = this.getPImage();
                PImage img = loadImage(s);
                p.portrait(img, true);
                p.placeElements();
                if (DEBUG) {
                    c.draw();
                    c.setCaption(s);
                }
                c.full = false;
            } else if (!p.calculating() && !c.full) {
                int [] boundingBox = c.getBoundingBox();
                PImage img = p.getOutput(
                        boundingBox[0] - MARGIN * 2,
                        boundingBox[1] - MARGIN * 2,
                        0
                );
                c.setImage(img);
                c.display();
            } else {
                if (DEBUG) {
                    c.draw();
                }
            }
        }

    }



    // PImage
    public String getPImage () {
        // fake face request
        try {
            String path = "test/imgs";
            File folder = new File(path);
            String contents[] = folder.list();
            Object res[] = Arrays.stream(contents).filter(x -> x.contains(".jpg")).toArray();
            int r = round(random(res.length-1));
            String s = path+"/"+res[r].toString();
            // PImage img = loadImage(s);
            // return img;
            return s;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println("👋");
        try (InputStream input = new FileInputStream("config.properties")) {
            prop.load(input);
            PApplet.main(Main.class.getName(), args);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }
}
package pt.uc.dei.fnp;

import approach6.Portraitor;
import processing.core.*;
import processing.data.JSONArray;
import processing.data.JSONObject;
import pt.uc.dei.fnp.utils.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Logger;

public class Main extends PApplet {
    static private Properties prop = new Properties();
    static private int MARGIN, BACKGROUND_COLOR = 0;
    static private int MIN_CONFIDANCE = 20;
    static private boolean DEBUG = false;
    protected ArrayList<Container> containers = new ArrayList<>();
    protected ArrayList<Portraitor> portraitors = new ArrayList<>();
    public static Logger logger = Logger.getLogger(approach6.Main.class.getName());
    public FnpDataReader reader;
    private PImage res = null;

    public void settings() {
        // size(900, 100);
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
        MIN_CONFIDANCE = parseInt(prop.getProperty("fnp.minConf"));
        DEBUG = parseBoolean(prop.getProperty("debug"));

        System.out.println("DEBUG="+DEBUG);

        reader = new FnpDataReader("cam1_frame_grayscale_full", "cam1_face_detections");
        reader.setReadingsPerSecond(1);

        for (int i=0; i<nCols; i++) {
            for (int j=0; j<nRows; j++) {
                float x = i * rectWidth;
                float y = j * rectHeight;
                Container c = new Container(null, x, y, rectWidth, rectHeight, lifespan, this);
                Portraitor p = new Portraitor(this, logger);
                if (DEBUG) {
                    c.turnOnDebug();
                }
                portraitors.add(p);
                containers.add(c);
            }
        }
    }


    public void draw () {
        // get frame
        // frame && data ?
        PImage frame = reader.getValueAsPImage("cam1_frame_grayscale_full");
        if (frame != null) {
            res = frame.copy();
        }
        JSONObject facesData = reader.getValueAsJSON("cam1_face_detections");
        // System.out.println("NEW_FRAME= "+newFrame);
        if (facesData != null && res !=null) {
            ArrayList<Integer> availableWallPositions = getAvailableWallPositions();
            int currentAvailablePos = availableWallPositions.size();
            if (currentAvailablePos > 0) {
                int j = 0;
                Collections.shuffle(availableWallPositions);
                JSONArray facesBounds = facesData.getJSONArray("detections");
                for (int i = 0; i < facesBounds.size(); i++) {
                    if (currentAvailablePos > 0) {
                        JSONObject detection = facesBounds.getJSONObject(i);
                        if (detection.getFloat("confidance") > MIN_CONFIDANCE) {
                            // System.out.println("detection="+detection+" round="+((round(detection.getFloat("x"))))+"mult="+(round(detection.getFloat("x")) * res.width));
                            System.out.println("detection=" + detection.getFloat("confidance"));
                            int faceX = round(detection.getFloat("x") * res.width);
                            int faceY = round(detection.getFloat("y") * res.height);
                            int faceW = round(detection.getFloat("w") * res.width);
                            int faceH = round(detection.getFloat("h") * res.height);
                            PImage img = res.get(faceX, faceY, faceW, faceH);


                            // add image to wall
                            int pos = availableWallPositions.get(j);
                            String s = String.valueOf(millis());
                            Container c = containers.get(pos);
                            Portraitor p = portraitors.get(pos);
                            int[] bbox = c.getBoundingBox();
                            img.resize(bbox[0] - MARGIN * 2, 0);
                            // System.out.println("res="+res+" w="+res.width+" h="+res.height);
                            // System.out.println("img="+img+" w="+img.width+" h="+img.height);
                            // System.out.println("facesValues"+faceX+" "+faceY+" "+faceW+" "+faceH);
                            p.portrait(img, true);
                            p.placeElements();
                            if (DEBUG) {
                                c.draw();
                                c.setCaption(s);
                            }
                            c.full = true;
                            c.waiting = true;
                            System.out.println("🌝 add real image to wall");

                            currentAvailablePos--;
                            j++;
                        }
                    }
                }
            }
        }

        // System.out.println("getAvailableWallPositions="+getAvailableWallPositions());

        background(BACKGROUND_COLOR);

        for (int i=0; i<containers.size(); i++) {
            Container c = containers.get(i);
            Portraitor p = portraitors.get(i);
            // System.out.println("p.calculating="+p.calculating()+" c.full="+c.full+" c.waiting="+c.waiting);
            if (c.alive) {
                c.draw();
            } else if (!p.calculating() && c.waiting) {
                // System.out.println("nex");
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


    public void addFakeImage (Container c, Portraitor p) {
        String s = this.getPImage();
        PImage img = loadImage(s);
        p.portrait(img, true);
        p.placeElements();
        if (DEBUG) {
            c.draw();
            c.setCaption(s);
        }
        c.full = true;
        c.waiting = true;
        System.out.println("🌚 add fake image to wall");
    }


    public ArrayList<Integer> getAvailableWallPositions () {
        ArrayList<Integer> availablePositions = new ArrayList<>();
        for (int i=0; i<containers.size(); i++) {
            Container c = containers.get(i);
            if (!c.alive && !c.full) {
                availablePositions.add(i);
            }
        }

        return availablePositions;
    }


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

    public void keyPressed () {
        if (Character.toLowerCase(key) == 'a' ) {
            ArrayList<Integer> availablePositions = getAvailableWallPositions();
            if (availablePositions.size() > 0) {
                Collections.shuffle(availablePositions);
                int pos = availablePositions.get(0);
                addFakeImage(containers.get(pos), portraitors.get(pos));
            }
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
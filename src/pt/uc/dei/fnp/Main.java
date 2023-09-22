package pt.uc.dei.fnp;

import processing.core.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class Main extends PApplet {
    static private Properties prop = new Properties();
    protected ArrayList<Container> containers = new ArrayList<Container>();

    public void settings() {
        // size(500, 500);
        fullScreen(2);
    }

    public void setup() {
        background(0);

        int nCols = parseInt(prop.getProperty("fnp.cols"));
        int nRows = parseInt(prop.getProperty("fnp.rows"));
        int [] lifespan = {
                parseInt(prop.getProperty("container.maxLifespan")),
                parseInt(prop.getProperty("container.minLifespan"))
        };
        float rectWidth = width/ (nCols * 1f);
        float rectHeight = height/ (nRows * 1f);



        // create containers
        for (int i=0; i<nCols; i++) {
            for (int j=0; j<nRows; j++) {
                float x = i * rectWidth;
                float y = j * rectHeight;
                PImage img = this.getPImage();
                Container c = new Container(img, x, y, rectWidth, rectHeight, lifespan, this);
                containers.add(c);
            }
        }
    }

    public void draw () {
        background(0);
        for (Container c : containers) {
            if (c.alive) {
                c.draw();
            }
        }
    }

    public PGraphics createPortrait() {
        // new PGRAPHIC
        return null;
    }

    public PImage getPImage () {
        // fake face request
        try {
            String path = "test/imgs";
            File folder = new File(path);
            String contents[] = folder.list();
            Object res[] = Arrays.stream(contents).filter(x -> x.contains(".jpg")).toArray();
            int r = round(random(res.length-1));
            String s = path+"/"+res[r].toString();
            PImage img = loadImage(s);
            return img;
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
package pt.uc.dei.fnp;

import processing.core.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
                Container c = new Container(x, y, rectWidth, rectHeight, lifespan, this);
                containers.add(c);
            }
        }
    }

    public void draw () {
        background(245);
        for (Container c : containers) {
            if (c.alive) {
                c.draw();
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
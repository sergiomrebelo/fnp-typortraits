package pt.uc.dei.fnp;

import processing.core.PImage;
import static processing.core.PApplet.constrain;
import static processing.core.PConstants.CENTER;

public class Container {
    private static final int TEXT_SIZE = 16;
    protected float x = 0, y = 0, w = 0, h=0;
    protected int c = 255;
    protected int lifespan = 0;
    protected PImage img;
    protected boolean alive = false, full=false;
    private String caption = null;
    private Main p5;
    private boolean DEBUG = false;


    public Container (PImage img, float x, float y, float w, float h, int[] lifespan, Main p5) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.img = img;
        if (this.img != null) {
            this.img.resize((int) this.w, 0);
        }
        this.lifespan = (int) (lifespan[0] + (Math.random()*(lifespan[0] - lifespan[1])));
        this.c = (int) (Math.random() * 255);
        this.p5 = p5;
        this.alive = false;
    }

    public void draw () {
        if (this.alive) {
            p5.pushStyle();
            p5.noStroke();
            int opacity = this.lifespan;
            opacity = constrain(opacity, 0, 255);
            // p5.fill(this.c, opacity);
            if (this.DEBUG) {
                p5.noFill();
                p5.stroke(0);
                p5.rect(this.x, this.y, this.w, this.h);
            }

            p5.imageMode(CENTER);
            // p5.tint(opacity);
            p5.image(this.img, this.x+this.w/2, this.y+this.h/2);

            p5.fill(255, 255-opacity);
            p5.rect(this.x, this.y, this.w, this.h);

            if (this.DEBUG && this.caption != null) {
                p5.textAlign(CENTER, CENTER);
                p5.textSize(TEXT_SIZE);
                p5.fill(0);
                p5.text(this.caption, this.x + this.w / 2,  this.y + this.h - (TEXT_SIZE * 1.35f));
            }

            p5.popStyle();

            if (this.lifespan <= 0) {
                this.lifespan = 0;
                System.out.println("dead");
                this.alive = false;
            } else {
                this.lifespan -= 1;
            }
        }
    }

    protected void setImage (PImage img) {
        this.img = img;
        // this.img.resize((int) this.w, 0);
    }

    protected boolean display() {
        this.alive = true;
        this.full = true;
        return true;
    }

    protected boolean turnOnDebug () {
        this.DEBUG = true;
        return true;
    }

    protected void setCaption (String caption) {
        this.caption = caption;
    }

    protected int [] getBoundingBox () {
        return new int[]{(int) this.w, (int) this.y};
    }
}

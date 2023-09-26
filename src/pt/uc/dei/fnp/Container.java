package pt.uc.dei.fnp;

import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import static processing.core.PApplet.constrain;
import static processing.core.PConstants.CENTER;

public class Container {
    private static final int TEXT_SIZE = 16;
    protected float x = 0, y = 0, w = 0, h=0;
    private float s = 1f;
    protected int c = 255;
    protected int lifespan = 0, oLifespan =0;
    protected int[] lifespanParams;
    protected PImage img;
    protected boolean alive = false, full=false;
    private String caption = null;
    private Main p5;
    private boolean DEBUG = false;
    private PGraphics graphic = null;
    protected boolean waiting = false;

    public Container (PImage img, float x, float y, float w, float h, int[] lifespan, Main p5) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.img = img;
        this.lifespanParams = lifespan;
        if (this.img != null) {
            this.img.resize((int) this.w, 0);
        }
        this.renewLifespan(lifespan);
        this.c = (int) (Math.random() * 255);
        this.p5 = p5;
        this.alive = false;
    }

    public void draw () {
        p5.pushStyle();
        if (this.alive) {
            p5.noStroke();
            int opacity = this.lifespan;
            if (this.oLifespan - this.lifespan < 255) {
                opacity = Math.abs(this.oLifespan - this.lifespan);
            }
            opacity = constrain(opacity, 0, 255);
            // p5.fill(this.c, opacity);
            if (this.DEBUG) {
                p5.noFill();
                p5.stroke(0);
                p5.rect(this.x, this.y, this.w, this.h);
            }

            p5.imageMode(CENTER);
            // p5.tint(opacity);
            if (graphic == null) {
                this.placeGraphics();
            }
             p5.image(this.graphic, this.x+this.w/2, this.y+this.h/2);

            p5.fill(255, 255-opacity);
            p5.rect(this.x, this.y, this.w, this.h);

            if (this.DEBUG && this.caption != null) {
                p5.textAlign(CENTER, CENTER);
                p5.textSize(TEXT_SIZE);
                p5.fill(0);
                p5.text(this.caption+" "+this.lifespan, this.x + this.w / 2,  this.y + this.h - (TEXT_SIZE * 1.35f));
            }
            if (this.lifespan <= 0) {
                this.lifespan = 0;
                this.alive = false;
            } else {
                this.lifespan -= 1;
            }
        } else if (this.full) {
            p5.fill(255,0,0);
            p5.rect(this.x, this.y, this.w, this.h);
        } else {
            p5.fill(255,0,255);
            p5.rect(this.x, this.y, this.w, this.h);
        }
        p5.popStyle();
    }

    protected void setImage (PImage img) {
        this.img = img;
        this.graphic = null;
        this.waiting = true;
        // this.img.resize((int) this.w, 0);
    }

    protected boolean display() {
        this.alive = true;
        this.full = true;
        this.renewLifespan(this.lifespanParams);
        return true;
    }

    private void renewLifespan (int[] params) {
        this.lifespan = (int) (params[0] + (Math.random()*(params[0] - params[1])));
        this.oLifespan = this.lifespan;
    }

    protected boolean turnOnDebug () {
        this.DEBUG = true;
        return true;
    }

    private void placeGraphics () {
        this.graphic = p5.createGraphics((int)(this.w * this.s), (int)(this.h * this.s));
        this.graphic.beginDraw();
        this.graphic.background(255);
        this.graphic.imageMode(CENTER);
        this.graphic.image(this.img, this.graphic.width/2, this.graphic.height/2);
        this.graphic.endDraw();

        this.waiting = false;
    }

    protected void setCaption (String caption) {
        this.caption = caption;
    }

    protected int [] getBoundingBox () {
        return new int[]{(int) this.w, (int) (this.h*2)};
    }

    public String toString() {
        return "["+(this.x/this.w)+","+(this.y/this.h)+"] lifespan:"+this.lifespan+" | alive:"+this.alive+" | full:"+this.full;
    }
}

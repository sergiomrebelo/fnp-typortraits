package pt.uc.dei.fnp;

import processing.core.PApplet;

import static processing.core.PApplet.constrain;

public class Container {
    protected float x = 0, y = 0, w = 0, h=0;
    protected int c = 255;
    protected int lifespan = 0;
    protected boolean alive = false;
    private Main p5;


    public Container (float x, float y, float w, float h, int[] lifespan, Main p5) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.lifespan = (int) (lifespan[0] + (Math.random()*(lifespan[0] - lifespan[1])));
        this.c = (int) (Math.random() * 255);
        this.p5 = p5;
        this.alive = true;
    }

    public void draw () {
        if (this.alive) {
            p5.pushStyle();
            p5.noStroke();
            int opacity = this.lifespan;
            opacity = constrain(opacity, 0, 255);
            p5.fill(this.c, opacity);
            p5.rect(this.x, this.y, this.w, this.h);
            p5.popStyle();

            if (this.lifespan < 0) {
                this.lifespan = 0;
                this.alive = false;
            } else {
                this.lifespan -= 1;
            }
        }
    }
}

package edu.acmX.exhibit.fall;

import java.awt.geom.Rectangle2D;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;


public class Obstacle {
	private int y, x;
	private int gap;
	private int height;
	private Module parent;
	private int COLOR = 125;
	public Rectangle2D left, right;

	public Obstacle(Module parent){
		this.parent = parent;
        gap = parent.width/10;
        height = parent.height/40;
		y = parent.height;
		x = randomGen();
        left = new Rectangle2D.Float(0,y,x,height);
        right = new Rectangle2D.Float(x + gap, y, parent.width - (gap + x), height);
	}

	public void update() {
        //Update rects
		left.setRect(0,y,x,height);
		right.setRect(x + gap, y, parent.width - (gap + x), height);
	}

	public void draw() {
		parent.fill(COLOR);
        //Draw rectangles
		parent.rect((float) left.getX(), (float) left.getY(), (float) left.getWidth(), (float) left.getHeight());
        parent.rect((float)(right.getX()), (float)(right.getY()), (float) right.getWidth(),(float) right.getHeight());
	}
    //Function to decide where the gap gets put
	public int randomGen(){
        return 1 + (int)(Math.random()*(parent.width - gap - 1));
	}

	public void setY(int y){
		this.y = y;
	}

	public int getY(){
		return y;
	}

	public void rise(int t){
		this.y -= t;
	}
}

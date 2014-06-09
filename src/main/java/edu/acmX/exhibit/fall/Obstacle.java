package edu.acmX.exhibit.fall;

import java.awt.geom.Rectangle2D;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by User on 6/9/2014.
 */
public class Obstacle {
	private int y, x;
	private int gap = 125;
	private int height = 25;
	private Module parent;
	private int COLOR = 125;
	public Rectangle2D left, right;

	public Obstacle(Module parent){
		this.parent = parent;
		y = parent.height;
		x = randomGen();
		left = new Rectangle2D.Float(0,y,height,x);
		right = new Rectangle2D.Float(x + gap, y, height, parent.width - (gap + x));
	}

	public void update() {
		left.setRect(0,y,height,x);
		right.setRect(x + gap, y, height, parent.width - (gap + x));
	}

	public void draw() {
		parent.fill(COLOR);
		parent.rect((float) left.getX(), (float) left.getY(), (float) left.getHeight(), (float) left.getWidth());
		parent.rect((float)(right.getX()), (float)(right.getY()), (float) right.getHeight(),(float) right.getWidth());
	}

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
		//this.y = y;
	}
}

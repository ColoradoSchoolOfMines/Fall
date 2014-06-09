package edu.acmX.exhibit.fall;

import java.awt.geom.Rectangle2D;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by User on 6/9/2014.
 */
public class Obstacle {
	private Rectangle2D[] obstacleList;
	private int y, x;
	private int gap = 125;
	private int height = 25;
	private Module parent;
	private int COLOR = 125;

	public Obstacle(Module parent){
		this.parent = parent;
		obstacleList = new Rectangle2D[2];
		y = parent.height;
		x = randomGen();
		obstacleList[0] = new Rectangle2D.Float(0,y,height,x);
		obstacleList[1] = new Rectangle2D.Float(x + gap, y, height, parent.width - (gap + x));
	}

	public void update() {
		obstacleList[0].setRect(0,y,height,x);
		obstacleList[1].setRect(x + gap, y, height, parent.width - (gap + x));
	}

	public void draw() {
		parent.fill(COLOR);
		parent.rect((float)(obstacleList[0].getX()), (float)(obstacleList[0].getY()), (float) obstacleList[0].getHeight(),(float) obstacleList[0].getWidth());
		parent.rect((float)(obstacleList[1].getX()), (float)(obstacleList[1].getY()), (float) obstacleList[1].getHeight(),(float) obstacleList[1].getWidth());
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

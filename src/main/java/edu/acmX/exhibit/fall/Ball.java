package edu.acmX.exhibit.fall;

import java.awt.geom.Rectangle2D;

public class Ball {

    private Module parent;
    private float x;
    private float y;
    private float radius;
    public Rectangle2D rect;
    public static int COLOR;
	//does this need to be public?
    public static int SPEED;
	private int gravity;

    public Ball(Module parent, float x, float y, float radius) {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.radius = radius;
        rect = new Rectangle2D.Float(x, y, radius, radius);
        COLOR = parent.color(176, 196, 222);
        SPEED = parent.height/100;
		gravity = parent.height/120;
    }


    public void draw() {
        parent.fill(COLOR);
        //Draw rectangle at ball's location
		parent.rect(x, y, radius, radius);

    }

    public void update() {
		int handPosX = (int) parent.getHandX();
        //Eliminate jitter
		if(handPosX > x + radius / 3 && handPosX < x + 2 * radius / 3) {
			return ;
		}
        //Move to the left
		if(x + radius / 2 > handPosX) {
			x -= SPEED;
		}
        //Move to the right
		else if(x + radius / 2 < handPosX) {
			x += SPEED;
		}

		// Update rect
		rect.setRect(x, y, radius, radius);
    }

	public void fall(){
		if(y < parent.height - radius){
			y = y + gravity;
		}
		
	}

    public void setX(float x) {
        this.x = x;
        // Update rect
        rect.setRect(x, y, radius, radius);
    }

	public void setY(float y) {
		this.y = y;
        // update rect
        rect.setRect(x, y, radius, radius);
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getRadius() {
		return radius;
	}
}

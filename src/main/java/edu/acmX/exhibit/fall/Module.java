package edu.acmX.exhibit.fall;

import edu.mines.acmX.exhibit.input_services.events.EventManager;
import edu.mines.acmX.exhibit.input_services.events.EventType;
import edu.mines.acmX.exhibit.input_services.hardware.devicedata.HandTrackerInterface;
import edu.mines.acmX.exhibit.input_services.hardware.drivers.InvalidConfigurationFileException;
import edu.mines.acmX.exhibit.module_management.modules.ProcessingModule;
import edu.mines.acmX.exhibit.input_services.hardware.*;
import edu.mines.acmX.exhibit.stdlib.input_processing.tracking.HandTrackingUtilities;

import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import java.util.*;

/**
 * Created by User on 6/6/2014.
 */
public class Module extends ProcessingModule{

    private static EventManager eventManager;
    private HandTrackerInterface driver;
    private MyHandReceiver receiver;

    private float handX;
    private float handY;
    private boolean gamePaused;
    private Ball ball;
	private int BACKGROUND_COLOR;
	private ArrayList<Obstacle> obstacleList;
	private static int count = 0;
	private int obstacleRate = 200;
	public boolean collide = false;

    public void setup() {
		BACKGROUND_COLOR = color(81,159,201);
        background(BACKGROUND_COLOR);
		ball = new Ball(this, width / 2, 50, 50);
		obstacleList = new ArrayList<>();
        registerTracking();
        gamePaused = true;
    }

    public void update() {
        driver.updateDriver();
		count++;
		collide = false;
        if (receiver.whichHand() != -1) {
            gamePaused = false;
            float marginFraction = (float) 1 / 6;
            handX = HandTrackingUtilities.getScaledHandX(receiver.getX(),
                    driver.getHandTrackingWidth(), width, marginFraction);
            handY = HandTrackingUtilities.getScaledHandY(receiver.getY(),
                    driver.getHandTrackingHeight(), height, marginFraction);
			if(count %obstacleRate == 0){
				obstacleList.add(new Obstacle(this));
			}
			for(Obstacle o : obstacleList){
				o.rise(2);
				o.update();
			}
			collide = checkCollisions();
			if(collide){
				ball.rise(2);
			}
			else{
				ball.fall();
			}
			if(ball.getY() <= 0){
				gamePaused = true;
			}
			ball.update();
			checkOffscreen();
        }
        else if (receiver.whichHand() == -1) {
            gamePaused = true;
        }
    }

    public void draw() {
		update();
		background(BACKGROUND_COLOR);
		if (gamePaused) {
			textAlign(CENTER, CENTER);
			textSize(96);
			fill(255, 255, 255);
			text("Wave to Continue", width / 2, height / 2);
			textAlign(LEFT, TOP);
		}
		ball.draw();
		for(Obstacle o : obstacleList){
			o.draw();
		}
    }

	public void checkOffscreen() {
		for(Obstacle o: obstacleList){
			if(o.getY() <= 0){
				obstacleList.remove(o);
			}
		}
	}

	public boolean checkCollisions() {
		for(Obstacle o : obstacleList) {
			Rectangle2D intersect1 = ball.rect.createIntersection(o.left);
			Rectangle2D intersect2 = ball.rect.createIntersection(o.right);
			if (!intersect1.isEmpty() || !intersect2.isEmpty()) {
				return true;
			}
		}
		return false;
	}

    public void registerTracking() {
        try {
            driver = (HandTrackerInterface) getInitialDriver("handtracking");
        } catch (BadFunctionalityRequestException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidConfigurationFileException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnknownDriverRequest e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( RemoteException e ) {
            e.printStackTrace();
        } catch ( BadDeviceFunctionalityRequestException e ) {
            e.printStackTrace();
        }

        eventManager = EventManager.getInstance();
        receiver = new MyHandReceiver();
        eventManager.registerReceiver(EventType.HAND_CREATED, receiver);
        eventManager.registerReceiver(EventType.HAND_UPDATED, receiver);
        eventManager.registerReceiver(EventType.HAND_DESTROYED, receiver);
    }

    public float getHandX() {
        return handX;
    }

    public float getHandY() {
        return handY;
    }
}

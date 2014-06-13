package edu.acmX.exhibit.fall;

import edu.mines.acmX.exhibit.input_services.events.EventManager;
import edu.mines.acmX.exhibit.input_services.events.EventType;
import edu.mines.acmX.exhibit.input_services.hardware.devicedata.HandTrackerInterface;
import edu.mines.acmX.exhibit.input_services.hardware.drivers.InvalidConfigurationFileException;
import edu.mines.acmX.exhibit.module_management.modules.ProcessingModule;
import edu.mines.acmX.exhibit.input_services.hardware.*;
import edu.mines.acmX.exhibit.stdlib.input_processing.tracking.HandTrackingUtilities;
import edu.mines.acmX.exhibit.stdlib.input_processing.tracking.HoverClick;
import edu.mines.acmX.exhibit.stdlib.scoring.ScoreSaver;
import org.w3c.dom.css.Rect;
import processing.core.PImage;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import java.util.*;


public class Module extends ProcessingModule{

    private static EventManager eventManager;
    private HandTrackerInterface driver;
    private MyHandReceiver receiver;

    private float handX;
    private float handY;
    private boolean gamePaused;
    private boolean gameLost;
    private Ball ball;
	private int BACKGROUND_COLOR;
	private ScoreSaver saver;
	private HoverClick end;
	private Rectangle endRect;
	private HoverClick playAgain;
	private Rectangle playAgainRect;
	private HoverClick submitScore;
	private Rectangle submitScoreRect;
	private ArrayList<Obstacle> obstacleList;
	private ArrayList<Obstacle> olToRemove;

	private static int count = 0;
	private int obstacleRate = 150;
	public boolean collide = false;
    private int score = 0;
    public static final int OBSTACLE_POINTS = 5;
    private static final int RISE_SPEED = 2;

	public static final String CURSOR_FILENAME = "hand_cursor.png";
	private PImage cursor_image;

	private static final String GAME_RESTART_TEXT = "Replay";
	private static final String GAME_END_TEXT = "Exit";
	private static final String GAME_SUBMIT_TEXT = "Submit";


    public void setup() {
		BACKGROUND_COLOR = color(81,159,201);
        background(BACKGROUND_COLOR);
		ball = new Ball(this, width / 2, height/30, width/30);
		obstacleList = new ArrayList<>();  //each wall/hole combo as obstacles
		olToRemove = new ArrayList<>();
		endRect = new Rectangle(width / 10, 5 * height/ 7, width / 5, height /5);
		end = new HoverClick(1000, endRect);
		playAgainRect = new Rectangle(2 * width / 5, 5 * height / 7, width / 5, height / 5);
		playAgain = new HoverClick(1000, playAgainRect);
		submitScoreRect = new Rectangle(7 * width / 10, 5 * height / 7, width / 5, height / 5);
		submitScore = new HoverClick(1000, submitScoreRect);
		noCursor();
		cursor_image = loadImage(CURSOR_FILENAME);
		cursor_image.resize(32, 32);
		saver = new ScoreSaver("Fall");
        registerTracking();
        gamePaused = true;
		gameLost = false;
    }

    public void update() {
        driver.updateDriver();
        if (receiver.whichHand() != -1) {
            gamePaused = false;
            float marginFraction = (float) 1 / 6;
            handX = HandTrackingUtilities.getScaledHandX(receiver.getX(),
                    driver.getHandTrackingWidth(), width, marginFraction);
            handY = HandTrackingUtilities.getScaledHandY(receiver.getY(),
                    driver.getHandTrackingHeight(), height, marginFraction);
        }
        else if (receiver.whichHand() == -1) {
            gamePaused = true;
        }
        if (!gameLost){ //while game is still playing
			if(!gamePaused) {
				count++;
				collide = false;
				//Add a new obstacle every obstacleRate updates
				if (count % obstacleRate == 0) {
					synchronized (this) {
						obstacleList.add(new Obstacle(this));
					}
				}
				//Make all obstacles rise
				synchronized (this) {
					for (Obstacle o : obstacleList) {
						o.rise(RISE_SPEED);
						o.update();
					}
				}
				//Does the ball hit an obstacle
				collide = checkCollisions();
				if (!collide) {
					ball.fall();
				}
				//If we reach the top of the screen, game over
				if (ball.getY() <= 0) {
					gamePaused = true;
					gameLost = true;
				}
				ball.update();
				checkOffscreen();
				for (Obstacle o : olToRemove) {
					if (obstacleList.contains(o)) {
						obstacleList.remove(o);
					}
				}
				olToRemove.clear();
			}
        }
		else{
			end.update((int) handX, (int) handY, millis());
			playAgain.update((int) handX, (int) handY, millis());
			submitScore.update((int) handX, (int) handY, millis());
			if(end.durationCompleted(millis())) {

					System.out.println("Before clearAllHands");
					driver.clearAllHands();
					destroy();
				System.out.println("End");

			} else if(playAgain.durationCompleted(millis())) {
				noCursor();
				score = 0;
				gameLost = false;
				gamePaused = false;
				reset();
			} else if(submitScore.durationCompleted(millis())) {
				//saver.addNewScore(points);
				receiver.hold();
				handX = handY = 0;
				saver.showPanel(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						receiver.release();
					}
				}, score, receiver.whichHand(), driver);
				receiver.setHand(-1);
			}
		}

    }

    public void draw() {
		update();
		background(BACKGROUND_COLOR);
		if(gameLost){
			drawGameOver();
		}
		if (gamePaused) {
			textAlign(CENTER, CENTER);
			textSize(96);
			fill(255, 255, 255);
			text("Wave to Continue", width / 2, height / 2);
			textAlign(LEFT, TOP);
		}
		if(!gameLost) {
			ball.draw();
			synchronized (this) {
				for (Obstacle o : obstacleList) {
					o.draw();
				}
			}
			drawScore();
		}
    }
    // Prints the score in the upper right portion of the screen
    public void drawScore() {
        fill(255, 215, 0);
        textSize(32);
        text("" + score, 19 * width / 20, height / 20);
    }
    //When the obstacles reach the top of the screen, delete them and add too score
	public void checkOffscreen() {
		synchronized (this) {
			for (Obstacle o : obstacleList) {
				if (o.getY() <= 0) {
					olToRemove.add(o);
					score += OBSTACLE_POINTS; //add score when obstacle is deleted
				}
			}
		}
	}
    //When the ball intersects a wall, put it on top of the wall
	public boolean checkCollisions() {
		synchronized (this) {
			for (Obstacle o : obstacleList) {
				Rectangle2D intersect1 = ball.rect.createIntersection(o.left);
				Rectangle2D intersect2 = ball.rect.createIntersection(o.right);
				if (!intersect1.isEmpty() || !intersect2.isEmpty()) {
					ball.setY(o.getY() - ball.getRadius());
					return true;
				}
			}
			return false;
		}
	}

	public void reset(){
		obstacleList.clear();
		ball.setY(width / 30);
	}

	public void drawGameOver() {
		background(BACKGROUND_COLOR);
		fill(0, 0, 0);
		rect(0, 0, width, height);
		fill(255, 69, 0);
		textSize(min(width / 8, height / 6));
		//rectMode(CENTER);
		textAlign(CENTER, CENTER);
		text("GAME OVER", width / 2, height / 6);
		textSize(min(width / 12, height / 9));
		text("YOUR SCORE: " + score, width / 2, height / 3);
		//textAlign(LEFT, TOP);
		textSize(min(width / 16, height / 12));
		text("HIGH SCORE:", width / 2, height / 2);
		text(saver.getBestScoreString(ScoreSaver.ScorePattern.HIGH_BEST), width / 2, 3 * height / 5);
		stroke(0);
		strokeWeight(4);

		fill(255, 0, 0);
		rect((float)endRect.getX(), (float) endRect.getY(), (float) endRect.getWidth(), (float) endRect.getHeight(), (float) endRect.getWidth() / 6);

		//draw text for end game box
		textAlign(CENTER, CENTER);
		textSize((float)endRect.getWidth()/10);
		fill(0,0,0);
		text(GAME_END_TEXT, (float) endRect.getX(), (float) endRect.getY(), (float) endRect.getWidth(), (float) endRect.getHeight());

		fill(50, 205, 50);
		rect((float) playAgainRect.getX(), (float) playAgainRect.getY(), (float) playAgainRect.getWidth(), (float) playAgainRect.getHeight(), (float) playAgainRect.getWidth() / 6);

		//draw text for new game box
		textAlign(CENTER, CENTER);
		textSize((float) playAgainRect.getWidth()/10);
		fill(0,0,0);
		text(GAME_RESTART_TEXT, (float) playAgainRect.getX(), (float) playAgainRect.getY(), (float) playAgainRect.getWidth(), (float) playAgainRect.getHeight());

		fill(0, 0, 255);
		rect((float) submitScoreRect.getX(), (float) submitScoreRect.getY(), (float) submitScoreRect.getWidth(), (float) submitScoreRect.getHeight(), (float) submitScoreRect.getWidth() / 6);

		//draw text for submit score box
		textAlign(CENTER, CENTER);
		textSize((float) submitScoreRect.getWidth()/10);
		fill(0,0,0);
		text(GAME_SUBMIT_TEXT, (float) submitScoreRect.getX(), (float) submitScoreRect.getY(), (float) submitScoreRect.getWidth(), (float) submitScoreRect.getHeight());

		noStroke();
		image(cursor_image, handX, handY);

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

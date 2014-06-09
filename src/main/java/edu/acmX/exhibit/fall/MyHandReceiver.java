package edu.acmX.exhibit.fall;

import edu.mines.acmX.exhibit.stdlib.graphics.Coordinate3D;
import edu.mines.acmX.exhibit.stdlib.graphics.HandPosition;
import edu.mines.acmX.exhibit.stdlib.input_processing.receivers.HandReceiver;



public class MyHandReceiver extends HandReceiver {

    private Coordinate3D position;
    int handID = -1;

    public MyHandReceiver() {
        position = new Coordinate3D(0, 0, 0);
    }

    public void handCreated(HandPosition pos) {
        if (handID == -1) {
            handID = pos.getId();
        }
        position = pos.getPosition();
    }

    public void handUpdated(HandPosition pos) {
        if (pos.getId() == handID) {
            position = pos.getPosition();
        }
    }

    public void handDestroyed(int id) {
        if (id == handID) {
            handID = -1;
        }
    }

    public int whichHand() {
        return handID;
    }

    public float getX() {
        return position.getX();
    }

    public float getY() {
        return position.getY();
    }

    public float getZ() {
        return position.getZ();
    }
}


package r2d2Final;
import battlecode.common.*;

public class MovementController {
    float pi = (float)Math.PI;
    public MapLocation target;
    public float bestDistance;
    public RobotController rc;
    public MapLocation[] radar = new MapLocation[16];
    public MapLocation here;
    public Direction movingDirection;
    public Direction lastDirectionUsed = null;
    public float radius;

    public MovementController(MapLocation target, RobotController rc, float rad){
        this.target = target;
        this.rc = rc;
        here = rc.getLocation();
        bestDistance = here.distanceTo(target);
        this.radius = rad;
    }

    public void updateTarget(MapLocation t){
        this.target = t;
        here = rc.getLocation();
        bestDistance = here.distanceTo(target);
    }

    public void updateRadar() throws GameActionException{
        here = rc.getLocation();
        Direction d = here.directionTo(target);
        for(int i=0;i<radar.length;i++){
            radar[i] = getFarthestLocation(d.rotateLeftRads(i*pi/8),0.8f);
        }
    }

    public MapLocation getFarthestLocation(Direction d, float offset) throws GameActionException{
        if(rc.isCircleOccupiedExceptByThisRobot(here.add(d,offset),radius) || !rc.onTheMap(here.add(d,offset),radius) || here.add(d,offset).distanceTo(target) > here.distanceTo(target)){
            return here.add(d,offset-0.8f);
        }
        else if(offset > 7.0f-radius-0.8f){
            return here.add(d,offset);
        }
        else{
            return getFarthestLocation(d,offset+0.8f);
        }
    }

    public boolean findBestDirection(){
        float currentBest = here.distanceTo(target);
        Direction bestDirection = null;
        for(MapLocation m: radar){
            if(m.distanceTo(target) < currentBest){
                currentBest = m.distanceTo(target);
                bestDirection = here.directionTo(m);
            }
        }
        movingDirection = bestDirection;
        return (currentBest < bestDistance);
    }

    public boolean move(float success) throws GameActionException{
        updateRadar();
        if(lastDirectionUsed == null){
            lastDirectionUsed = here.directionTo(target);
        }
        float angle = 0.0f;
        boolean found = findBestDirection();
        if(found && movingDirection != null){
            if(rc.canMove(movingDirection)) {
                rc.move(movingDirection);
                lastDirectionUsed = movingDirection;
            }
             else {
                movingDirection = lastDirectionUsed;
                if (!rc.canMove(movingDirection)) {
                    while (!rc.canMove(movingDirection) && angle < 360.0f) {
                        movingDirection = movingDirection.rotateRightDegrees(15f);
                        angle = angle + 15f;
                    }
                    if (rc.canMove(movingDirection)) {
                        rc.move(movingDirection);
                    }
                    lastDirectionUsed = movingDirection;
                } else {
                    while (rc.canMove(movingDirection) && angle < 360.0f) {
                        movingDirection = movingDirection.rotateLeftDegrees(15f);
                        angle = angle + 15f;
                    }
                    movingDirection = movingDirection.rotateRightDegrees(15f);
                    if (rc.canMove(movingDirection)) {
                        rc.move(movingDirection);
                    }
                    lastDirectionUsed = movingDirection;
                }
            }
        }
        else{
            movingDirection = lastDirectionUsed;
            if(!rc.canMove(movingDirection)){
               while(!rc.canMove(movingDirection) && angle < 360.0f){
                   movingDirection = movingDirection.rotateRightDegrees(15f);
                   angle = angle + 15f;
               }
               if(rc.canMove(movingDirection)){
                   rc.move(movingDirection);
               }
               lastDirectionUsed = movingDirection;
            }
            else{
                while(rc.canMove(movingDirection) && angle < 360.0f){
                    movingDirection = movingDirection.rotateLeftDegrees(15f);
                    angle = angle + 15f;
                }
                movingDirection = movingDirection.rotateRightDegrees(15f);
                if(rc.canMove(movingDirection)){
                    rc.move(movingDirection);
                }
                lastDirectionUsed = movingDirection;
            }
        }
        if(rc.getLocation().distanceTo(target) < bestDistance){
            bestDistance = rc.getLocation().distanceTo(target);
        }
        rc.setIndicatorLine(rc.getLocation(),target,255,0,0);
        return (rc.getLocation().distanceTo(target) < success);
    }
}

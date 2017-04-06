package r2d2Final;
import battlecode.common.*;

public class plantingLocation {
    public Direction[] trees = new Direction[5];
    public Direction manufacturingLoc;
    public MapLocation gardenerLocation;
    public int numberOfTreesBlockedByNeutral;

    public plantingLocation(MapLocation here, Direction enemy){
        gardenerLocation = here;
        Direction d = new Direction(0);
        for(int i=0;i<5;i++){
            d = new Direction(i*(float)Math.PI/6);
            if(d.radiansBetween(enemy) < (float)Math.PI/12){
                if((d.getDeltaX(1.0f) > 0 && enemy.getDeltaX(1.0f) < 0) || (d.getDeltaX(1.0f) < 0 && enemy.getDeltaX(1.0f) > 0 )){
                    d = d.opposite();
                }
                break;
            }
        }
        for(int i=0;i<5;i++){
            trees[i] = d.rotateLeftDegrees((i+1)*60f);
        }
        manufacturingLoc = d;
    }
}

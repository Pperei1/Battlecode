package r2d2Scout;
import battlecode.common.*;

public class Archon extends Global{
    private static Direction d;
    private static int lastTreeNumber = -3;
    private static Direction awayFromGard;
    public static void loop() throws GameActionException{
        init(rc);
        rc.broadcast(20,0);
        rc.broadcast(600,601);
        d = buildDirection.opposite();
        while(true){
            try{
                updateRobot();
                if(findBuildDirection(RobotType.GARDENER) && ((rc.getTreeCount() > lastTreeNumber + 2 && rc.getTreeCount()<20) || (rc.getTreeCount() == 0 && rc.getRoundNum() > 300))) {
                    lastTreeNumber = rc.getTreeCount();
                    rc.buildRobot(RobotType.GARDENER,buildDirection);
                    rc.broadcast(20,1);
                }
                if(lastTreeNumber>rc.getTreeCount()){
                    lastTreeNumber = rc.getTreeCount();
                }
                if(rc.canMove(d)){
                    rc.move(d);
                }
                else{
                    awayFromGard = here.directionTo(findNearbyGard()).opposite();
                    if(rc.canMove(awayFromGard)){
                        rc.move(awayFromGard);
                    }
                }
                if(rc.getTeamBullets() > 10000) {
                    rc.donate(10000);
                }
            }
            catch(Exception e){
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
            Clock.yield();
        }
    }

    public static MapLocation findNearbyGard(){
        if(nearAllyRobot.length == 0){
            return here.translate(0.01f,0.01f);
        }
        else{
            for(RobotInfo i:nearAllyRobot){
                if(i.getType() == RobotType.GARDENER){
                    return i.getLocation();
                }
            }
            return here.translate(0.01f,0.01f);
        }
    }
}

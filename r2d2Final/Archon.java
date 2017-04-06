package r2d2Final;
import battlecode.common.*;

public class Archon extends Global{
    private static Direction d;
    private static Direction awayFromGard;
    private static MapLocation current;
    private static MovementController mc;
    private static float lastHealth;
    private static int counter;
    public static void loop() throws GameActionException{
        init(rc);
        lastHealth = rc.getHealth();
        if(rc.readBroadcast(20) == 1){

        }
        else{
            rc.broadcast(20,0);
        }
        rc.broadcast(900,rc.getRoundNum());
        rc.broadcast(903,rc.getRoundNum());
        rc.broadcast(906,rc.getRoundNum());
        rc.broadcast(800,801);
        rc.broadcast(600,601);
        d = buildDirection;
        buildDirection = d.opposite();
        counter = 0;
        while(true){
            try{
                updateRobot();
                float offset2 = 3.0f;
                System.out.println(rc.readBroadcast(20));
                if(findBuildDirectionSpecial(RobotType.GARDENER,offset2) && (rc.readBroadcast(20) == 0 || counter > 70)){
                    counter = 0;
                    rc.buildRobot(RobotType.GARDENER,buildDirection);
                    rc.broadcast(20,1);
                    float offset = 0.0f;
                    if(current == null){
                        current = findBestLocation();
                        if(current == null){
                            awayFromGard = here.directionTo(findNearbyGard()).opposite();
                            while(!rc.canMove(awayFromGard) && !rc.hasMoved() && offset<360f ){
                                awayFromGard = awayFromGard.rotateLeftDegrees(15f);
                                offset = offset + 15f;
                            }
                            if(rc.canMove(awayFromGard)){
                                rc.move(awayFromGard);
                            }
                        }
                        else{
                            beginning = here;
                            target = current;
                        }
                    }
                }
                else{
                    counter++;
                }
                if(current == null){

                }
                else{
                    if(bug2MoveAlex(beginning,target,1.0f)){
                        current = null;
                    }
                }
                if(lastHealth - rc.getHealth() > 6){
                    lastHealth = rc.getHealth();
                    if(rc.getRoundNum()> 700){
                        broadcastDanger();
                    }
                }
                if(rc.getTeamBullets() > 500) {
                    rc.donate(100);
                }
            }
            catch(Exception e){
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
            Clock.yield();
        }
    }

    private static void broadcastDanger() throws GameActionException{
        int turn = rc.readBroadcast(900);
        if(rc.getRoundNum() != turn){
            rc.broadcast(906,rc.getRoundNum());
            rc.broadcast(907,(int)here.x);
            rc.broadcast(908,(int)here.y);
        }
    }

    public static MapLocation findBestLocation() throws GameActionException{
        int bestScore = 20;
        int currentScore;
        MapLocation current;
        MapLocation bestCurrent = null;
        for(float offset = 0.0f;offset<360.0f;offset = offset + 15.0f){
            current = rc.getLocation().add(new Direction(offset),5.0f);
            if(!rc.isCircleOccupied(current,2.0f)){
                rc.setIndicatorDot(current,255,255,255);
                currentScore = evaluatePosition(current);
                if(currentScore < bestScore){
                    bestCurrent = current;
                }
            }
        }
        rc.setIndicatorDot(bestCurrent,255,0,0);
        return bestCurrent;
    }

    public static int evaluatePosition(MapLocation current){
        int score = 0;
        RobotInfo[] nearAllyGard = rc.senseNearbyRobots(current,4.0f,myTeam);
        for(RobotInfo r:nearAllyGard){
            if(r.getType() == RobotType.GARDENER){
                score++;
            }
        }
        return score;
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

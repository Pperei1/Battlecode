package r2d2Final;
import battlecode.common.*;

public class Scout extends Global{
    // state = 0 : searching state = 1:attacking
    private static int targetID = 0;
    private static RobotInfo targetRobot;
    private static MapLocation targetLocation;
    private static Direction targetDirection;
    private static float targetDistance;
    private static Direction dest = randomDirection();
    public static void loop() throws GameActionException{
        init(rc);
        beginning = here;
        target = enemyArchonStartingLocation[rnd.nextInt(numberOfEnemyArchons)];
        while(true){
            try{
                updateRobot();
                //scoutRusher();
                scoutCollector();
            }
            catch(Exception e){
                System.out.println("Scout Exception");
                e.printStackTrace();
            }
            Clock.yield();
        }
    }
    private static void scoutCollector() throws GameActionException{
        boolean found = false;
        for(TreeInfo i:nearTree){
            if(i.getContainedBullets() > 0){
                found = true;
                if(rc.canShake(i.getID())){
                    rc.shake(i.getID());
                }
                else {
                    dest = rc.getLocation().directionTo(i.getLocation());
                }
                break;
            }
        }
        while(!rc.canMove(dest)){
            if(found){
                dest = dest.rotateRightDegrees(pi/16);
            }
            else{
                dest = randomDirection();
            }
        }
        rc.move(dest);
    }
    private static void scoutRusher() throws GameActionException{
        eraseBroadcast();
        if(rc.readBroadcast(9) != 0){
            target = new MapLocation(rc.readBroadcast(10),rc.readBroadcast(11));
            targetID = rc.readBroadcast(12);
            beginning = here;
        }
        else {
            senseGardener();
        }
        if(targetID != 0){
            lookForTarget();
        }
        if(rc.canSenseRobot(targetID) && targetID != 0){
            targetRobot = rc.senseRobot(targetID);
            targetLocation = targetRobot.getLocation();
            targetDirection = here.directionTo(targetLocation);
            targetDistance = Math.min(2.5f,here.distanceTo(targetLocation)-2);
            while(!rc.canMove(targetDirection,targetDistance)){
                targetDirection = targetDirection.rotateRightDegrees(pi/16);
            }
            rc.move(targetDirection,targetDistance);
            shootScout();
        }
        else{
            targetRobot = null;
            targetLocation = null;
            targetDirection = null;
            moveScout();
        }
        tryToShake();
    }

    private static void eraseBroadcast() throws GameActionException{
        if(rc.readBroadcast(8) == rc.getID()){
            for(int i = 8;i<13;i++){
                rc.broadcast(i,0);
            }
        }
    }

    private static void shootScout() throws GameActionException{
        MapLocation t = rc.senseRobot(targetID).getLocation();
        System.out.println(here.distanceTo(t));
        if(here.distanceTo(t) < 3.0 && rc.canFireSingleShot()){
            rc.fireSingleShot(here.directionTo(t));
        }
    }

    private static void lookForTarget() throws GameActionException{
        if(rc.canSenseRobot(targetID)){
            beginning = here;
            target = new MapLocation(rc.senseRobot(targetID).getLocation().x,rc.senseRobot(targetID).getLocation().y);
        }
    }
    private static void moveScout() throws GameActionException{
        if(target == null){
            moveRandom();
        }
        else if(bug2Move(beginning,target,2.5f)){
            beginning = here;
            target = null;
        }
    }


    private static boolean senseGardener() throws GameActionException{
        for(RobotInfo i: nearEnemyRobot){
            if((i.getType() == RobotType.GARDENER)){
                rc.broadcast(8, rc.getID());
                rc.broadcast(9,(int)i.getLocation().x);
                rc.broadcast(10,(int)i.getLocation().x);
                rc.broadcast(11,(int)i.getLocation().y);
                rc.broadcast(12, i.getID());
                targetID = i.getID();
                beginning = here;
                target = new MapLocation(i.getLocation().x,i.getLocation().y);
                return true;
            }
        }
        return false;
    }
    private static void tryToShake() throws GameActionException{
        for(TreeInfo t: nearTree){
            if(rc.canShake(t.getID())){
                rc.shake(t.getID());
                return;
            }
        }
    }
}


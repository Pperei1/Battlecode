package r2d2Scout;
import battlecode.common.*;

public class Soldier extends Global{
    private static int defenderChannel;
    private static boolean isDef = false;
    private static int gardenerID;
    private static RobotInfo gardener;
    private static float dx = 0.0f;
    private static float dy = -4.0f;
    private static int targetEnemyRobotID;
    private static boolean arrived = false;
    public static void loop() throws GameActionException{
        init(rc);
        isDefender();
        while(true){
            try{
                updateRobot();
                if(isDef) {
                    beDefender();
                }
                else{
                    beSoldier();
                }
            }
            catch(Exception e){
                System.out.println("Soldier Exception");
                e.printStackTrace();
            }
            Clock.yield();
        }
    }

    private static void beSoldier() throws GameActionException{
        if(nearEnemyRobot.length != 0){
            beginning = here;
            if(canSenseTargetRobot(targetEnemyRobotID)){

            }
            else{
                target = nearEnemyRobot[0].getLocation();
                targetEnemyRobotID = nearEnemyRobot[0].getID();
            }
            while(!bug2MoveAlex(beginning,target,7.0f)){
                Clock.yield();
                rc.broadcast(defenderChannel+3,1);
                updateRobot();
                if(canSenseTargetRobot(targetEnemyRobotID)){

                }
                else if(nearEnemyRobot.length != 0){
                    target = nearEnemyRobot[0].getLocation();
                }
                else{
                    break;
                }
            }
            delta = pi/2;
            if(rc.canFirePentadShot()){
                rc.firePentadShot(here.directionTo(target));
            }
            else if(rc.canFireTriadShot()){
                rc.fireTriadShot(here.directionTo(target));
            }
            else if(rc.canFireSingleShot()){
                rc.fireSingleShot(here.directionTo(target));
            }
        }
        else if(!arrived){
            if(bug2MoveAlex(here,nearestEAStartingLocation,5.0f)){
                arrived = true;
            }
        }
        else{
            moveRandom();
        }
    }
    private static void beDefender() throws GameActionException{
        int countTurn = 0;
        rc.broadcast(defenderChannel+3,1);
        if(nearEnemyRobot.length != 0){
            beginning = here;
            if(canSenseTargetRobot(targetEnemyRobotID)){

            }
            else{
                target = nearEnemyRobot[0].getLocation();
                targetEnemyRobotID = nearEnemyRobot[0].getID();
            }
            while(!bug2MoveAlex(beginning,target,7.0f)){
                Clock.yield();
                rc.broadcast(defenderChannel+3,1);
                updateRobot();
                if(canSenseTargetRobot(targetEnemyRobotID)){

                }
                else if(nearEnemyRobot.length != 0){
                    target = nearEnemyRobot[0].getLocation();
                }
                else{
                    break;
                }
            }
            delta = pi/2;
            if(rc.canFirePentadShot()){
                rc.firePentadShot(here.directionTo(target));
            }
            else if(rc.canFireTriadShot()){
                rc.fireTriadShot(here.directionTo(target));
            }
            else if(rc.canFireSingleShot()){
                rc.fireSingleShot(here.directionTo(target));
            }
        }
        else{
            beginning = here;
            dx = here.directionTo(enemyArchonStartingLocation[0]).getDeltaX(4.0f);
            dy = here.directionTo(enemyArchonStartingLocation[0]).getDeltaY(4.0f);
            if(rc.canSenseRobot(gardenerID)){
                gardener = rc.senseRobot(gardenerID);
                target = gardener.getLocation().translate(dx,dy);
            }
            else{
                target = new MapLocation(rc.readBroadcast(defenderChannel+4),rc.readBroadcast(defenderChannel+5)).translate(dx,dy);
            }
            if(here.distanceTo(target) < 5.0f){
                while(rc.isLocationOccupied(target) && here.distanceTo(target)<5.0f){
                    target = target.translate(dx/4,dy/4);
                }
            }
            rc.broadcast(defenderChannel+3,1);
            while(!bug2Move(beginning,target,0.5f) && countTurn < 5){
                updateRobot();
                rc.setIndicatorLine(beginning,target,255,255,0);
                countTurn++;
                Clock.yield();
                rc.broadcast(defenderChannel+3,1);
            }
            delta = pi/2;
        }
    }

    private static void isDefender() throws GameActionException{
       int end = rc.readBroadcast(600);
       for(int i=601;i<end;i = i+5){
           if(rc.readBroadcast(i+1) == rc.getID()){
               defenderChannel = i;
               isDef = true;
               rc.broadcast(defenderChannel+3,1);
               gardenerID = rc.readBroadcast(defenderChannel);
           }
       }
    }

    private static boolean canSenseTargetRobot(int id){
        for(RobotInfo i:nearEnemyRobot){
            if(i.getID() == id){
                target = i.getLocation();
                return true;
            }
        }
        return false;
    }
}

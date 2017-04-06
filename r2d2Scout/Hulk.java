package r2d2Scout;
import battlecode.common.*;

public class Hulk extends Global{
    private static TreeInfo targetTree;
    private static Direction theEnemies;
    private static MapLocation targetArchonLocation;
    private static RobotInfo targetEnemy;
    public static void loop() throws GameActionException{
        init(rc);
        updateRobot();
        targetArchonLocation = enemyArchonStartingLocation[rnd.nextInt(numberOfEnemyArchons)];
        beginning = here;
        while(!bug2MoveAlex(beginning,targetArchonLocation,5.0f) && !findTreeToCut(4.0f)){
            updateRobot();
            Clock.yield();
        }
        while(true){
            try{
                updateRobot();
                if(findEnemyToStrike()){
                    attackEnemy();
                }
                if(findTreeToCut(7.0f)){
                    cutTrees();
                }
                else{
                    updateRobot();
                    moveRandom();
                    Clock.yield();
                }
            }
            catch(Exception e){
                System.out.println("Lumberjack Exception");
                e.printStackTrace();
            }
            Clock.yield();
        }
    }

    private static void attackEnemy() throws GameActionException{
        while(rc.canSenseRobot(targetEnemy.getID())){
            updateRobot();
            beginning = here;
            target = rc.senseRobot(targetEnemy.getID()).getLocation();
            rc.setIndicatorLine(beginning,target,255,255,0);
            if(bug2MoveAlex(beginning,target,1.5f+targetEnemy.getRadius())){
                rc.strike();
            }
            Clock.yield();
        }
    }

    private static boolean findEnemyToStrike(){
        for(RobotInfo i:nearEnemyRobot){
            targetEnemy = i;
            return true;
        }
        return false;
    }

    private static void cutTrees() throws GameActionException{
        beginning = here;
        target = targetTree.getLocation();
        while(!bug2MoveAlexLumber(beginning,target,targetTree.getID())){
            updateRobot();
            rc.setIndicatorLine(beginning,target,255,255,0);
            Clock.yield();
        }
        while(rc.canChop(targetTree.getID()) && rc.canSenseTree(targetTree.getID())){
            rc.chop(targetTree.getID());
            updateRobot();
            rc.setIndicatorLine(here,target,255,255,0);
            Clock.yield();
        }
    }

    private static boolean findTreeToCut(float distance){
        TreeInfo[] nearbyTrees;
        if(distance == 7.0f){
            nearbyTrees = nearTree;
        }
        else{
            nearbyTrees = rc.senseNearbyTrees(distance);
        }
        for(TreeInfo i:nearbyTrees){
            if(i.getTeam() != myTeam){
                targetTree = i;
                return true;
            }
        }
        return false;
    }
}

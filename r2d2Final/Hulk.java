package r2d2Final;
import battlecode.common.*;

public class Hulk extends Global{
    private static TreeInfo targetTree;
    private static RobotInfo targetEnemy;
    private static MapLocation startingLocation;
    private static MovementController mc;
    public static void loop() throws GameActionException{
        init(rc);
        startingLocation = here;
        while(true){
            updateRobot();
            try{
                if(nearEnemyRobot.length != 0) {
                    if (rc.getHealth() < 20 || nearEnemyRobot.length > nearAllyRobot.length + 1) {
                        runAway();
                    } else {
                        if (targetTree == null || treeWasCutDown()) {
                            if (nearTree.length != 0) {
                                targetTree = findTreeToCut();
                            }
                            if (targetTree == null) {
                                moveRandom();
                            } else {
                                mc = new MovementController(targetTree.getLocation(), rc,1.0f);
                                rc.setIndicatorLine(here, targetTree.getLocation(), 0, 255, 0);
                                cutDownTree();
                            }
                        } else {
                            rc.setIndicatorLine(here, targetTree.getLocation(), 255, 0, 0);
                            cutDownTree();
                        }
                    }
                }
                else{
                    if(targetTree == null || treeWasCutDown()) {
                        if(nearTree.length != 0){
                            targetTree = findTreeToCut();
                        }
                        if(targetTree == null){
                            moveRandom();
                        }
                        else{
                            mc = new MovementController(targetTree.getLocation(),rc,1.0f);
                            rc.setIndicatorLine(here,targetTree.getLocation(),0,255,0);
                            cutDownTree();
                        }
                    }
                    else{
                        rc.setIndicatorLine(here,targetTree.getLocation(),255,0,0);
                        cutDownTree();
                    }
                }
            }
            catch(Exception e){
                System.out.println("Lumberjack Exception");
                e.printStackTrace();
            }
            Clock.yield();
        }
    }

    private static boolean treeWasCutDown(){
        if(targetTree == null){
            return true;
        }
        else if(here.distanceTo(targetTree.getLocation()) < 7.0f){
            return (!rc.canSenseTree(targetTree.getID()));
        }
        else{
            return false;
        }
    }

    private static void cutDownTree() throws GameActionException{
        if(rc.canChop(targetTree.getID())){
            rc.chop(targetTree.getID());
            if(!rc.canSenseTree(targetTree.getID())){
                targetTree = null;
            }
        }
        else if(rc.senseNearbyTrees(1.5f,Team.NEUTRAL).length != 0){
            targetTree = rc.senseNearbyTrees(1.5f,Team.NEUTRAL)[0];
            mc = new MovementController(targetTree.getLocation(),rc,1.0f);
            if(rc.canChop(targetTree.getID())) {
                rc.chop(targetTree.getID());
            }
        }
        else if(rc.isLocationOccupiedByTree(here.add(here.directionTo(targetTree.getLocation()),2.0f))){
            TreeInfo inTheWay = rc.senseTreeAtLocation(here.add(here.directionTo(targetTree.getLocation()),2.0f));
            if(inTheWay.getTeam() == myTeam){
                mc.move(0.0f);
            }
            else{
                if(rc.canChop(inTheWay.getID())){
                    rc.chop(inTheWay.getID());
                }
                else{
                    rc.move(here.directionTo(targetTree.getLocation()),Math.min(0.75f,here.distanceTo(inTheWay.getLocation())-2));
                }
            }
        }
        else{
            mc.move(0.0f);
        }
    }
    private static void runAway() throws GameActionException{
        Direction d;
        float dx = 0.0f;
        float dy = 0.0f;
        for(RobotInfo r:nearEnemyRobot){
            if(r.getType() == RobotType.SOLDIER || r.getType() == RobotType.LUMBERJACK || r.getType() == RobotType.TANK){
                d = here.directionTo(r.getLocation()).opposite();
                dx = dx + d.getDeltaX(1.0f);
                dy = dy + d.getDeltaY(1.0f);
            }
        }
        bug2MoveAlex(here,here.add(new Direction(dx,dy),1.0f),0.0f);
    }

    private static void attackEnemy() throws GameActionException{
        if(bug2MoveAlex(beginning,target,1.5f+targetEnemy.getRadius())){
            rc.strike();
        }
    }


    private static TreeInfo findTreeToCut(){
        float bestDistance = 10000.0f;
        TreeInfo best = null;
        for(TreeInfo t:nearTree){
            if(t.getTeam() != myTeam){
                if(t.getContainedRobot() != null || best == null){
                    return t;
                }
            }
        }
        return best;
    }
}

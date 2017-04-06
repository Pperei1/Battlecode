package r2d2Scout;
import battlecode.common.*;

public class Gardener extends Global{
    private static int treeBuilt = 0;
    private static int defenderChannel;
    private static TreeInfo[] garden;
    private static RobotInfo defender;
    private static Direction plantingDirection ;
    private static int turnSinceChangedDirection;
    private static int targetTreeID;
    private static MapLocation firstTree;
    private static int firstTreeID;

    public static void loop() throws GameActionException{
        init(rc);
        beginning = null;
        target = null;
        firstTree = null;
        defenderChannel = rc.readBroadcast(600);
        plantingDirection = randomDirection();
        rc.broadcast(600,defenderChannel+5);
        defender = null;
        if(findBuildDirection(RobotType.SCOUT)){
            rc.buildRobot(RobotType.SCOUT,buildDirection);
        }
        while(true){
            try{
                beginTurn();
                gardenerBuild();
                gardenerMove();
                waterTree();
            }
            catch(Exception e){
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
            Clock.yield();
        }
    }

    private static void gardenerMove() throws GameActionException{
        if(garden.length != 0){
            findTreeToWater();
        }
        if(target == null && treeBuilt < 3){
            moveRandomLimited();
        }
        else{
            if(target == null){
                beginning = here;
                target = firstTree;
                targetTreeID = firstTreeID;
            }
            while(!bug2MoveAlexWater(beginning,target,targetTreeID)){
                rc.setIndicatorLine(beginning,target,255,255,0);
                waterTree();
                Clock.yield();
                beginTurn();
                gardenerBuild();
            }
            beginning = null;
            target = null;
        }
    }

    private static void findTreeToWater(){
        float minH = 60.0f;
        TreeInfo minTree = garden[0];
        for(TreeInfo i:garden){
            if(i.getHealth() < minH){
                minTree = i;
                minH = i.getHealth();
            }
        }
        if(minH < 40){
            target = minTree.getLocation();
            beginning = here;
            targetTreeID = minTree.getID();
        }
    }

    private static void moveRandomLimited() throws GameActionException{
        while(!rc.canMove(randomDest) || turnSinceChangedDirection > 3){
               randomDest = randomDirection();
            turnSinceChangedDirection = 0;
        }
        turnSinceChangedDirection++;
        rc.move(randomDest);
    }


    public static boolean bug2MoveGard(MapLocation begin, MapLocation end, int treeID) throws GameActionException{
        Direction d = begin.directionTo(end);
        float angle = 0.0f;
        float distance = Math.min(0.5f,here.distanceTo(end));
        if(((distanceToLine(begin,end,here)) < 0.5)){
            while(!rc.canMove(d,distance)){
                angle = angle + delta;
                d = d.rotateLeftRads(delta/4);
            }
            if(angle >= pi || angle <= -pi){
                delta = -delta;
                d = d.opposite();
            }
            while(!rc.canMove(d,distance)){
                d = d.rotateLeftRads(delta/4);
            }
            rc.move(d,distance);
            lastDirectionUsed = d;
        }
        else{
            d = lastDirectionUsed.rotateRightRads(delta);
            while(!rc.canMove(d,distance)){
                d = d.rotateLeftRads(delta/4);
                angle = angle+delta;
            }
            if(angle >= pi || angle <= -pi){
                delta = -delta;
                d = d.opposite();
            }
            while(!rc.canMove(d,distance)){
                d = d.rotateLeftRads(delta/4);
            }
            rc.move(d,distance);
            lastDirectionUsed = d;
        }
        return (rc.canWater(treeID));
    }

    private static void gardenerBuild() throws GameActionException{
        if(!hasDefender() && rc.getRoundNum() < 100){
            if(findBuildDirection(RobotType.SOLDIER)){
                rc.buildRobot(RobotType.SOLDIER,buildDirection);
                nearAllyRobot = rc.senseNearbyRobots(2.0f,myTeam);
                setDefender(rc.senseRobotAtLocation(here.add(buildDirection,2.0f)));
            }
        }
        else if((treeBuilt > 3 && rc.getTeamBullets() > 300) || (rc.getTeamBullets() > 100 && rc.getRoundNum() < 150)){
            if(findBuildDirection(RobotType.LUMBERJACK)){
                rc.buildRobot(RobotType.LUMBERJACK,buildDirection);
            }
        }
        else if(findTreePlantingLocation() && treeBuilt < 5){
            rc.plantTree(plantingDirection);
            if(treeBuilt == 0){
                firstTree = here.add(plantingDirection,2.0f);
                firstTreeID = rc.senseTreeAtLocation(firstTree).getID();
            }
            treeBuilt++;
        }
        else{
            if(findBuildDirection(RobotType.SOLDIER)){
                rc.buildRobot(RobotType.SOLDIER,buildDirection);
            }
        }
    }

    private static boolean enoughSpace(Direction plantingDir) throws GameActionException{
        return (rc.canPlantTree(plantingDirection) && noTreesNearby(here.add(plantingDir,2.0f)) && rc.onTheMap(here.add(plantingDirection,2.0f),4.5f));
    }

    private static boolean noTreesNearby(MapLocation plantingLoc){
        for(TreeInfo i : nearTree){
            if(i.getLocation().distanceTo(plantingLoc) <= 4.1f){
                return false;
            }
        }
        return true;
    }

    private static boolean findTreePlantingLocation() throws GameActionException{
        float angle = 0.0f;
        while(angle < 2*pi && !enoughSpace(plantingDirection)){
            angle = angle + pi/8;
            plantingDirection = plantingDirection.rotateLeftRads(pi/8);
        }
        return (angle < 2*pi);
    }

    private static void beginTurn() throws GameActionException{
        updateRobot();
        rc.broadcast(defenderChannel+4,(int)here.x);
        rc.broadcast(defenderChannel+5,(int)here.y);
        garden = rc.senseNearbyTrees(7.0f,myTeam);
    }

    private static void setDefender(RobotInfo def) throws GameActionException{
        defender = def;
        int countTurn = 0;
        defenderSetup();
        while(!checkForConfirmation()){
            if(countTurn > 21){
                defender = null;
                break;
            }
            countTurn++;
            waterTree();
            Clock.yield();
        }
    }

    private static void defenderSetup() throws GameActionException{
        rc.broadcast(defenderChannel,rc.getID());
        rc.broadcast(defenderChannel+1,defender.getID());
        rc.broadcast(defenderChannel+2,1);
        rc.broadcast(defenderChannel+3,0);
        rc.broadcast(defenderChannel+4,(int)here.x);
        rc.broadcast(defenderChannel+5,(int)here.y);
    }

    private static boolean checkForConfirmation() throws GameActionException{
        return (rc.readBroadcast(defenderChannel+3) == 1);
    }


    private static boolean hasDefender() throws GameActionException{
        if(defender == null || rc.readBroadcast(defenderChannel+3) == 0){
            defender = null;
            return false;
        }
        rc.broadcast(defenderChannel+3,0);
        return true;
    }

    private static void waterTree() throws  GameActionException{
        float minHealth = 10000;
        int minID = 0;
        for(TreeInfo i:garden){
            if(i.getHealth()<minHealth && rc.canWater(i.getID())){
                minHealth = i.getHealth();
                minID = i.getID();
            }
        }
        if(minID != 0){
            rc.water(minID);
        }
    }

}

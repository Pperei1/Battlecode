package r2d2Final;
import battlecode.common.*;

public class Gardener extends Global{
    private static TreeInfo[] garden;
    private static plantingLocation pL;
    private static MapLocation[] manufacturingLocations;
    private static boolean isPlanted;
    private static Direction movingDirection;
    private static int numberOfLumberJacksBuilt = 0;
    private static int exploration;
    private static float bestScore = 0.0f;
    private static MapLocation bestLocation;
    private static boolean isFirstTree;
    private static MovementController mc;
    private static boolean isFirst = false;
    private static float lastHealth = 40.0f;
    private static boolean buildDefender = false;

    public static void loop() throws GameActionException{
        init(rc);
        isPlanted = false;
        movingDirection = null;
        bestLocation = here;
        isFirstTree = true;
        if(rc.getRobotCount()-rc.getInitialArchonLocations(myTeam).length == 1){
            isFirst = true;
            startBuildOrder();
        }
        while(true){
            try{
                updateRobot();
                if(rc.getRoundNum() - rc.readBroadcast(903) > 500 && rc.getRoundNum() > 2000 && rc.getTeamBullets()>10) {
                    rc.donate(10);
                }
               if(!isPlanted && (exploration < (70 + rc.getRoundNum()/15) || bestScore == 0)){
                   manufacturingLocations = getManufacturingLocations();
                   moveNotPlanted();
                   pL = new plantingLocation(here,here.directionTo(nearestEAStartingLocation));
                   if(isFirst){
                       movingDirection = checkIfCanPlant(pL, 3);
                   }
                   else{
                       movingDirection = checkIfCanPlant(pL, 4);
                   }
                   if(!isPlanted){
                       exploration++;
                       buildNotPlanted();
                   }
                   else{
                       updatePlantingLocation(pL);
                       buildPlanted();
                   }
               }
               else if(!isPlanted){
                   beginning = here;
                   mc = new MovementController(bestLocation,rc,1.0f);
                   int count = 0;
                   while(!mc.move(0.5f) && count < 20){
                       rc.setIndicatorDot(bestLocation,0,0,255);
                       count++;
                       Clock.yield();
                   }
                   if(rc.isLocationOccupiedByTree(here.add(pL.manufacturingLoc,2.0f))){
                       for(Direction d:pL.trees){
                           if(!rc.isLocationOccupiedByTree(here.add(d,2.0f))){
                               pL.manufacturingLoc = d;
                               rc.setIndicatorDot(here.add(pL.manufacturingLoc,2.0f),0,0,0);
                           }
                       }
                   }
                   isPlanted = true;
                   if(!isFirst){
                       rc.broadcast(20,0);
                   }
                   broadcastManufacturingLocation(here.add(pL.manufacturingLoc,2.0f));
               }
               else{
                    updatePlantingLocation(pL);
                    buildPlanted();
                    waterGarden();
               }
                if(rc.getTeamBullets() > 500) {
                    rc.donate(100);
                }
                if(lastHealth - rc.getHealth() > 6){
                   lastHealth = rc.getHealth();
                   buildDefender = true;
                   if(!senseOnlyScoutBullets()){
                       broadcastDanger();
                   }
                }
            }
            catch(Exception e){
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
            Clock.yield();
        }
    }

    private static boolean senseOnlyScoutBullets() {
        for (BulletInfo b : rc.senseNearbyBullets()) {
            if (b.getDamage() > 1) {
                return true;
            }
        }
        return false;
    }

    private static void broadcastDanger() throws GameActionException{
        int turn = rc.readBroadcast(900);
        if(rc.getRoundNum() != turn){
            rc.broadcast(900,rc.getRoundNum());
            rc.broadcast(901,(int)here.x);
            rc.broadcast(902,(int)here.y);
        }
    }
    private static void buildNotPlanted() throws GameActionException{
        if(buildDefender && findBuildDirection(RobotType.SOLDIER)){
            rc.buildRobot(RobotType.SOLDIER,buildDirection);
        }
        else if(findBuildDirection(RobotType.LUMBERJACK) && numberOfLumberJacksBuilt < 2 && rc.senseNearbyTrees(7.0f,Team.NEUTRAL).length > 2){
            rc.buildRobot(RobotType.LUMBERJACK,buildDirection);
            numberOfLumberJacksBuilt++;
        }
        else if(findBuildDirection(RobotType.SOLDIER) && rc.getTeamBullets() >= 200){
            rc.buildRobot(RobotType.SOLDIER,buildDirection);
        }
    }
    private static void buildPlanted() throws GameActionException{
        if((buildDefender || rc.getRoundNum()-rc.readBroadcast(903) < 8) && findBuildDirection(RobotType.SOLDIER)){
            rc.buildRobot(RobotType.SOLDIER,buildDirection);
            buildDefender = false;
        }
        else if((pL.numberOfTreesBlockedByNeutral >= 1 || rc.senseNearbyTrees(7.0f,Team.NEUTRAL).length > 2) && rc.canBuildRobot(RobotType.LUMBERJACK,pL.manufacturingLoc) && (rc.getRoundNum()-rc.readBroadcast(903)) > 10 && numberOfLumberJacksBuilt < 2 && rc.getTeamBullets() > 100){
            rc.buildRobot(RobotType.LUMBERJACK,pL.manufacturingLoc);
            numberOfLumberJacksBuilt++;
        }
        else if(rc.readBroadcast(500) == 0 && rc.getTreeCount() > 3 && rc.canBuildRobot(RobotType.SCOUT,pL.manufacturingLoc)){
            rc.buildRobot(RobotType.SCOUT,pL.manufacturingLoc);
            rc.broadcast(500,1);
        }
        else if(rc.canBuildRobot(RobotType.SOLDIER,pL.manufacturingLoc) && rc.getTeamBullets() > 150){
            rc.buildRobot(RobotType.SOLDIER,pL.manufacturingLoc);
        }
    }

    private static void updatePlantingLocation(plantingLocation pL) throws GameActionException{
        pL.numberOfTreesBlockedByNeutral = 0;
        for(int i=0;i<5;i++){
            if((isValidPlantingLocation(pL.trees[i]) == 0) && rc.canPlantTree(pL.trees[i])){
                rc.setIndicatorDot(here.add(pL.trees[i],2.0f),255,0,0);
                if(isFirst && isFirstTree){
                    rc.broadcast(20,0);
                    isFirstTree = false;
                }
                rc.plantTree(pL.trees[i]);
            }
           else{
               rc.setIndicatorDot(here.add(pL.trees[i],2.0f),0,255,0);
               if(rc.isLocationOccupiedByTree(here.add(pL.trees[i],2.0f))){
                   if(rc.senseTreeAtLocation(here.add(pL.trees[i],2.0f)).getTeam() == Team.NEUTRAL){
                       pL.numberOfTreesBlockedByNeutral++;
                   }
               }
           }
       }
    }

    private static MapLocation[] getManufacturingLocations() throws GameActionException{
        int end = rc.readBroadcast(800);
        if(end != 801) {
            MapLocation[] manufacturingLocations = new MapLocation[(end - 801) / 2];
            int j = 0;
            for (int i = 801; i < end; i = i + 2) {
                manufacturingLocations[j] = new MapLocation(rc.readBroadcast(i), rc.readBroadcast(i + 1));
                j++;
            }
            return manufacturingLocations;
        }
        else{
            return null;
        }
    }

    private static double isValidPlantingLocation(Direction d) throws GameActionException{
        TreeInfo[] t;
        MapLocation l = here.add(d,2.0f);
        for (RobotInfo i : nearAllyRobot) {
            if (i.getType() == RobotType.ARCHON && l.distanceTo(i.getLocation()) < 2.1f) {
                return -1;
            }
        }
        if(manufacturingLocations != null) {
            for (MapLocation k : manufacturingLocations) {
                if (k.distanceTo(l) < 4.1f) {
                    return -1;
                }
            }
        }
        if(!rc.onTheMap(l,3.1f)){
            return -1;
        }
        if(!rc.canPlantTree(d) && rc.getTeamBullets() >= 50){
            t = rc.senseNearbyTrees(l,1.0f,myTeam);
            if(t.length != 0){
                return -1;
            }
            t = rc.senseNearbyTrees(l,1.0f,Team.NEUTRAL);
            if(t.length != 0){
                return -0.5;
            }
        }
        return 0;
    }

    private static void broadcastManufacturingLocation(MapLocation l) throws GameActionException{
        int end = rc.readBroadcast(800);
        rc.broadcast(end,(int)l.x);
        rc.broadcast(end+1,(int)l.y);
        rc.broadcast(800,end+2);
    }

    private static Direction checkIfCanPlant(plantingLocation pL, int minScore) throws GameActionException{
        double score = 5;
        double s;
        float dx = 0.0f;
        float dy = 0.0f;
        if(movingDirection != null){
            dx = movingDirection.getDeltaX(1.0f);
            dy = movingDirection.getDeltaX(1.0f);
        }
        for(Direction l: pL.trees){
            s = isValidPlantingLocation(l);
            if(s < -0.6){
                rc.setIndicatorDot(here.add(l,2.0f),255,0,0);
                dx = dx + l.getDeltaX(1.0f);
                dy = dy + l.getDeltaY(1.0f);
            }
            score = score + s;
        }
        if(rc.isCircleOccupiedExceptByThisRobot(here.add(pL.manufacturingLoc,2.0f),1.0f)){
            rc.setIndicatorDot(here.add(pL.manufacturingLoc,2.0f),255,255,255);
            dx = dx+pL.manufacturingLoc.getDeltaX(1.0f);
            dy = dy+pL.manufacturingLoc.getDeltaY(1.0f);
            if(isFirst){
                score = -1;
            }
        }
        else if(score > minScore){
            isPlanted = true;
            broadcastManufacturingLocation(here.add(pL.manufacturingLoc,2.0f));
            return null;
        }
        if(score > bestScore){
            bestScore = (float)score;
            bestLocation = pL.gardenerLocation;
        }
        return new Direction(dx,dy).opposite();
    }

    private static void moveNotPlanted() throws GameActionException{
       if(movingDirection == null){

       }
       else{
           beginning = here;
           target = here.add(movingDirection,5.0f);
           bug2MoveAlex(beginning,target,0.0f);
       }
    }

    private static void startBuildOrder() throws GameActionException{
        if(here.distanceTo(nearestEAStartingLocation) < 40 && rc.senseNearbyTrees(7.0f,Team.NEUTRAL).length < 1){
            findBuildDirection(RobotType.SOLDIER);
            if(rc.canBuildRobot(RobotType.SOLDIER,buildDirection)){
                rc.buildRobot(RobotType.SOLDIER,buildDirection);
            }
            Clock.yield();
            updateRobot();
            while(!findBuildDirection(RobotType.SOLDIER)){
                Clock.yield();
                updateRobot();
            }
            rc.setIndicatorDot(here.add(buildDirection,2.0f),0,0,255);
            if(rc.canBuildRobot(RobotType.SOLDIER,buildDirection)){
                rc.buildRobot(RobotType.SOLDIER,buildDirection);
            }
            updateRobot();
            Clock.yield();
            if(seesBullets()){
                Clock.yield();
                updateRobot();
                while(!findBuildDirection(RobotType.SCOUT)){
                    Clock.yield();
                    updateRobot();
                }
                rc.setIndicatorDot(here.add(buildDirection,2.0f),0,0,255);
                if(rc.canBuildRobot(RobotType.SCOUT,buildDirection)){
                    rc.buildRobot(RobotType.SCOUT,buildDirection);
                }
            }
        }
        else{
            updateRobot();
            findBuildDirection(RobotType.SCOUT);
            if(rc.canBuildRobot(RobotType.SCOUT,buildDirection)){
                rc.buildRobot(RobotType.SCOUT,buildDirection);
            }
            Clock.yield();
            updateRobot();
            if(rc.senseNearbyTrees(7.0f,Team.NEUTRAL).length > 0){
                float offset = 3.0f;
                while(!findBuildDirectionSpecial(RobotType.LUMBERJACK,offset)){
                    Clock.yield();
                    updateRobot();
                    offset = offset - 0.05f;
                }
                if(rc.canBuildRobot(RobotType.LUMBERJACK,buildDirection)){
                    rc.buildRobot(RobotType.LUMBERJACK,buildDirection);
                    numberOfLumberJacksBuilt++;
                }
                Clock.yield();
                updateRobot();
            }
            while(!findBuildDirection(RobotType.SOLDIER)){
                Clock.yield();
                updateRobot();
            }
            rc.setIndicatorDot(here.add(buildDirection,2.0f),0,0,255);
            if(rc.canBuildRobot(RobotType.SOLDIER,buildDirection)){
                rc.buildRobot(RobotType.SOLDIER,buildDirection);
            }
        }
    }

    private static boolean seesBullets(){
        if(nearTree.length == 0){
            return false;
        }
        for(TreeInfo t :nearTree){
            if(t.getContainedBullets() > 0){
                return true;
            }
        }
        return false;
    }

    private static void waterGarden() throws  GameActionException{
        garden = rc.senseNearbyTrees(here,2.0f,myTeam);
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

package r2d2Scout;
import battlecode.common.*;
import java.util.Random;
public class Global{
    public static RobotController rc;
    protected static MapLocation here;
    protected static RobotInfo[] nearEnemyRobot;
    protected static RobotInfo[] nearAllyRobot;
    protected static TreeInfo [] nearTree;
    protected static MapLocation[] enemyArchonStartingLocation;
    public static MapLocation beginning;
    public static MapLocation target;
    public static Team myTeam;
    public static Team enemyTeam;
    public static Direction lastDirectionUsed;
    public final static float pi = (float)Math.PI;
    public static int numberOfEnemyArchons;
    public static float numberOfBullets;
    public static Direction buildDirection;
    public static MapLocation nearestEAStartingLocation;
    public static Direction randomDest;
    public static float bestDistanceReached = 10000.0f;
    public static float delta = pi/2;
    public static float sightRadius;
    public static Random rnd = new Random();

    public static void init(RobotController x){
        rc = x;
        here = rc.getLocation();
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        randomDest = randomDirection();
        enemyArchonStartingLocation = rc.getInitialArchonLocations(enemyTeam);
        numberOfEnemyArchons = enemyArchonStartingLocation.length;
        nearestEAStartingLocation = findNearestLocation(here,enemyArchonStartingLocation);
        buildDirection = here.directionTo(nearestEAStartingLocation);
        if(rc.getType() == RobotType.SCOUT){
            sightRadius = 14f;
        }
        else{
            sightRadius = 7f;
        }
    }

    public static void updateRobot(){
        here = rc.getLocation();
        nearEnemyRobot = rc.senseNearbyRobots(sightRadius,enemyTeam);
        nearAllyRobot = rc.senseNearbyRobots(sightRadius,myTeam);
        nearTree = rc.senseNearbyTrees();
        numberOfBullets = rc.getTeamBullets();
    }

    public static Direction randomDirection() {
        return new Direction((float)Math.random() * 2 * (float)Math.PI);
    }

    public static void moveRandom() throws  GameActionException{
        while(!rc.canMove(randomDest)){
            randomDest = randomDirection();
        }
        rc.move(randomDest);
    }

    public static MapLocation findNearestLocation(MapLocation here,MapLocation[] possibility){
        float bestDistance = here.distanceTo(possibility[0]);
        MapLocation bestLocation = possibility[0];
        float currentDistance;
        for(MapLocation m: possibility){
            currentDistance = here.distanceTo(m);
            if(currentDistance < bestDistance){
                bestLocation = m;
                bestDistance = currentDistance;
            }
        }
        return bestLocation;
    }

    public static double distanceToLine(MapLocation begin, MapLocation end, MapLocation here){
        float top = Math.abs((end.y-begin.y)*here.x - (end.x-begin.x)*here.y + end.x*begin.y - end.y*begin.x);
        double bottom = Math.sqrt(Math.pow((double)end.y-begin.y,2.0) + Math.pow((double)end.x-begin.x,2.0));
        return top/bottom;
    }


    public static boolean bug2Move(MapLocation begin, MapLocation end, float arrivalDistance) throws GameActionException{
        Direction d = begin.directionTo(end);
        float angle = 0.0f;
        float distance = Math.min(0.5f,here.distanceTo(end));
        if(((distanceToLine(begin,end,here)) < 0.5)){
            bestDistanceReached = here.distanceTo(end);
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
        return (end.distanceTo(rc.getLocation()) <= arrivalDistance);
    }

    public static boolean bug2MoveLumber(MapLocation begin, MapLocation end, int treeID) throws GameActionException{
        Direction d = begin.directionTo(end);
        float distance = Math.min(0.5f,here.distanceTo(end)-2);
        if((distanceToLine(begin,end,rc.getLocation())) < 2){
            while(!rc.canMove(d,distance)){
                d = d.rotateLeftRads(pi/16);
            }
            rc.move(d,distance);
            lastDirectionUsed = d;
        }
        else{
            if(begin.directionTo(end).radiansBetween(lastDirectionUsed) > 3/2*pi){
                d = lastDirectionUsed;
            }
            else{
                d = lastDirectionUsed.rotateRightRads(pi/2);
            }
            while(!rc.canMove(d,distance)){
                d = d.rotateLeftRads(pi/16);
            }
            rc.move(d,distance);
            lastDirectionUsed = d;
        }
        return (rc.canChop(treeID) || !rc.canSenseTree(treeID));
    }

    public static boolean simpleMove(MapLocation target, float arrivalDistance) throws GameActionException{
        Direction d = here.directionTo(target);
        float rad = 0.0f;
        while(!rc.canMove(d) && rad < 2*pi){
            d = d.rotateLeftRads(pi/16);
            rad = rad+pi/16;
        }
        if(rc.canMove(d)){
            rc.move(d);
        }
        return (target.distanceTo(rc.getLocation())<arrivalDistance);
    }

    public static boolean simpleMoveLumber(MapLocation target ,int treeID) throws GameActionException{
        Direction d = here.directionTo(target);
        float distance;
        float rad = 0.0f;
        while(!rc.canMove(d) && rad < 2*pi+1){
            d = d.rotateLeftRads(pi/16);
            rad = rad+pi/16;
        }
        distance = Math.min(0.5f,here.distanceTo(target)-2);
        if(rc.canMove(d,distance)){
            rc.move(d,distance);
        }
        return rc.canChop(treeID);
    }

    public static boolean alone(){
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(7.0f,myTeam);
        for(RobotInfo i:nearbyRobots){
            if(i.getType() == RobotType.GARDENER || (i.getType() == (RobotType.ARCHON) && here.distanceTo(i.getLocation()) < 5.0f)){
                return false;
            }
        }
        return true;
    }

    public static boolean findBuildDirection(RobotType robot){
        float add = 0f;
        while(!rc.canBuildRobot(robot,buildDirection)){
            buildDirection = buildDirection.rotateLeftDegrees(60f);
            add = add+60f;
            if(add == 360f){
                return false;
            }
        }
        return true;
    }

    public static boolean bug2MoveAlex(MapLocation begin, MapLocation end, float arrivalDistance) throws GameActionException {
            Direction direct = begin.directionTo(end);
            Direction current = lastDirectionUsed;

            float distance = Math.min(0.95f, here.distanceTo(end));
            int delta = 18;
            double[] direction_scores = new double[2 * delta]; //-PI ... 0 ... +PI
            // The robot likes to follow the direct direction (factor alpha)
            // The robot likes to follow its current direction (factor beta)
            // Using cos function as weight :
            // Find max_r(alpha * cos(direct - r) + beta * cos(current - r))
            // Flemme de faire l'analyse
            double alpha = 10.; // > 0
            double beta = 5.; // > 0
            double min_score = -alpha - beta;
            double max_score = min_score;
            int max_i = -1;
            double r = - Math.PI;
            // Find the best direction according to the heuristic
            for (int i = 0; i < direction_scores.length; i++){
                // Compute the heuristic score for each direction
                double score = 0.;
                if (rc.canMove(new Direction((float) r), distance)) {
                    if (direct != null)
                        score += alpha * Math.cos(direct.radians - r);
                    if (current != null)
                        score += beta * Math.cos(current.radians - r);
                } else {
                    score = min_score;
                }
                direction_scores[i] = score;
                if (score > max_score){
                    max_score = score;
                    max_i = i;
                }
                r += Math.PI / delta;

            }

            // Use the best direction to follow according to the heuristic
            if (max_i > -1) {
                lastDirectionUsed = new Direction((float)(-Math.PI + (Math.PI * max_i) / delta));
                rc.move(lastDirectionUsed,distance);
            }
            return (end.distanceTo(rc.getLocation()) <= arrivalDistance);
    }

    public static boolean bug2MoveAlexLumber(MapLocation begin, MapLocation end, int treeID) throws GameActionException {
        Direction direct = begin.directionTo(end);
        Direction current = lastDirectionUsed;

        float distance = Math.min(0.5f, here.distanceTo(end));
        int delta = 18;
        double[] direction_scores = new double[2 * delta]; //-PI ... 0 ... +PI
        // The robot likes to follow the direct direction (factor alpha)
        // The robot likes to follow its current direction (factor beta)
        // Using cos function as weight :
        // Find max_r(alpha * cos(direct - r) + beta * cos(current - r))
        // Flemme de faire l'analyse
        double alpha = 10.; // > 0
        double beta = 5.; // > 0
        double min_score = -alpha - beta;
        double max_score = min_score;
        int max_i = -1;
        double r = - Math.PI;
        // Find the best direction according to the heuristic
        for (int i = 0; i < direction_scores.length; i++){
            // Compute the heuristic score for each direction
            double score = 0.;
            if (rc.canMove(new Direction((float) r), distance)) {
                if (direct != null)
                    score += alpha * Math.cos(direct.radians - r);
                if (current != null)
                    score += beta * Math.cos(current.radians - r);
            } else {
                score = min_score;
            }
            direction_scores[i] = score;
            if (score > max_score){
                max_score = score;
                max_i = i;
            }
            r += Math.PI / delta;

        }

        // Use the best direction to follow according to the heuristic
        if (max_i > -1) {
            lastDirectionUsed = new Direction((float)(-Math.PI + (Math.PI * max_i) / delta));
            rc.move(lastDirectionUsed,distance);
        }
        return (rc.canChop(treeID) || !rc.canSenseTree(treeID));
    }

    public static boolean bug2MoveAlexWater(MapLocation begin, MapLocation end, int treeID) throws GameActionException {
        Direction direct = begin.directionTo(end);
        Direction current = lastDirectionUsed;

        float distance = Math.min(0.5f, here.distanceTo(end));
        int delta = 18;
        double[] direction_scores = new double[2 * delta]; //-PI ... 0 ... +PI
        // The robot likes to follow the direct direction (factor alpha)
        // The robot likes to follow its current direction (factor beta)
        // Using cos function as weight :
        // Find max_r(alpha * cos(direct - r) + beta * cos(current - r))
        // Flemme de faire l'analyse
        double alpha = 10.; // > 0
        double beta = 5.; // > 0
        double min_score = -alpha - beta;
        double max_score = min_score;
        int max_i = -1;
        double r = - Math.PI;
        // Find the best direction according to the heuristic
        for (int i = 0; i < direction_scores.length; i++){
            // Compute the heuristic score for each direction
            double score = 0.;
            if (rc.canMove(new Direction((float) r), distance)) {
                if (direct != null)
                    score += alpha * Math.cos(direct.radians - r);
                if (current != null)
                    score += beta * Math.cos(current.radians - r);
            } else {
                score = min_score;
            }
            direction_scores[i] = score;
            if (score > max_score){
                max_score = score;
                max_i = i;
            }
            r += Math.PI / delta;

        }

        // Use the best direction to follow according to the heuristic
        if (max_i > -1) {
            lastDirectionUsed = new Direction((float)(-Math.PI + (Math.PI * max_i) / delta));
            rc.move(lastDirectionUsed,distance);
        }
        return (rc.canWater(treeID) || !rc.canSenseTree(treeID));
    }
}

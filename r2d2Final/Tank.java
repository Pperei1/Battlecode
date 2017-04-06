package r2d2Final;
import battlecode.common.*;

public class Tank extends Global {
    private static int targetEnemyRobotID;
    private static boolean arrived = false;
    private static MovementController mc;
    private static MapLocation target;
    private static RobotInfo targetEnemy;
    private static BodyInfo direct;
    private static BodyInfo[] triad = new BodyInfo[2];
    private static BodyInfo[] pentad = new BodyInfo[4];
    private static int visited = 0;
    private static int changeTarget;

    public static void loop() throws GameActionException {
        init(rc);
        target = null;
        targetEnemy = null;
        while (true) {
            try {
                updateRobot();
                if (nearEnemyRobot.length != 0 && (rc.getRoundNum() > 200 || oneEnemyNotArchon())) {
                    if (targetEnemy != null) {
                        if (rc.canSenseRobot(targetEnemy.getID()) && ((targetEnemy.getType() == RobotType.SOLDIER) || (targetEnemy.getType() == RobotType.TANK))){
                            rc.setIndicatorDot(here, 255, 0, 0);
                            targetEnemy = rc.senseRobot(targetEnemy.getID());
                            broadcastEnemyLocation(targetEnemy);
                            if (moveToDodge(targetEnemy)) {
                                shoot(targetEnemy);
                            }
                        } else if (rc.canSenseRobot(targetEnemy.getID())) {
                            targetEnemy = updateTarget(targetEnemy);
                            broadcastEnemyLocation(targetEnemy);
                            if (moveToDodge(targetEnemy)) {
                                shoot(targetEnemy);
                            }
                        } else {
                            targetEnemy = newTarget();
                            mc = new MovementController(targetEnemy.getLocation(), rc, 2.0f);
                        }
                    } else {
                        targetEnemy = newTarget();
                        if (moveToDodge(targetEnemy)) {
                            shoot(targetEnemy);
                        }
                        mc = new MovementController(targetEnemy.getLocation(), rc, 2.0f);
                    }
                } else if (target == null || changeTarget > 100) {
                    target = findTarget();
                    mc = new MovementController(target, rc, 2.0f);
                    mc.move(5.0f);
                } else {
                    if(rc.canMove(here.add(here.directionTo(target))) && rc.senseNearbyTrees(2.1f,myTeam).length == 0){
                        rc.move(here.add(here.directionTo(target)));
                    }
                    else if (mc.move(5.0f)) {
                        target = null;
                        rc.setIndicatorDot(rc.getLocation(), 255, 255, 0);
                    } else {
                        changeTarget++;
                    }
                }
            } catch (Exception e) {
                System.out.println("Tank Exception");
                e.printStackTrace();
            }
            Clock.yield();
        }
    }

    public static RobotInfo updateTarget(RobotInfo currentTarget) {
        for (RobotInfo r : nearEnemyRobot) {
            if (r.getType() == RobotType.SOLDIER || r.getType() == RobotType.TANK) {
                return r;
            }
        }
        return currentTarget;
    }

    public static void broadcastEnemyLocation(RobotInfo i) throws GameActionException {
        int turn = rc.readBroadcast(903);
        if (!(rc.getRoundNum() == turn)) {
            rc.broadcast(904, (int) i.getLocation().x);
            rc.broadcast(905, (int) i.getLocation().y);
        }
    }

    public static MapLocation findTarget() throws GameActionException {
        MapLocation targetA;
        MapLocation targetB;
        int turn = rc.readBroadcast(900);
        if (rc.getRoundNum() - turn < 30 && turn > 1) {
            targetA = new MapLocation(rc.readBroadcast(901), rc.readBroadcast(902));
        } else {
            targetA = null;
        }
        turn = rc.readBroadcast(903);
        if (rc.getRoundNum() - turn < 30 && turn > 1) {
            targetB = new MapLocation(rc.readBroadcast(904), rc.readBroadcast(905));
        } else {
            targetB = null;
        }
        if (targetA == null && targetB == null) {
            targetA = enemyArchonStartingLocation[visited % enemyArchonStartingLocation.length];
            visited++;
            return targetA;
        } else {
            if (targetA == null) {
                return targetB;
            } else if (targetB == null) {
                return targetA;
            } else if (here.distanceTo(targetA) > here.distanceTo(targetB)) {
                return targetB;
            } else {
                return targetB;
            }
        }
    }

    public static boolean oneEnemyNotArchon() {
        for (RobotInfo i : nearEnemyRobot) {
            if (i.getType() != RobotType.ARCHON) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasPriority(RobotInfo i, RobotInfo b) {
        if (i.getType() == b.getType()) {
            return false;
        } else if (i.getType() == RobotType.SOLDIER) {
            return true;
        } else if (i.getType() == RobotType.GARDENER && b.getType() != RobotType.SOLDIER) {
            return true;
        }
        return false;

    }

    public static RobotInfo newTarget() {
        RobotInfo bestRobot = nearEnemyRobot[0];
        for (RobotInfo i : nearEnemyRobot) {
            if (hasPriority(i, bestRobot)) {
                bestRobot = i;
            }
            //else if(i.getHealth() < bestRobot.getHealth()){
            //   bestRobot = i;
            //}
        }
        return bestRobot;
    }

    public static void lineOfSight(MapLocation m, Direction d, boolean checkingLineOfSight) throws GameActionException {
        direct = whatWillIShoot(m, d, 2.1f);
        if (!checkingLineOfSight) {
            triad[0] = whatWillIShoot(m, d.rotateLeftDegrees(20f), 2.1f);
            triad[1] = whatWillIShoot(m, d.rotateRightDegrees(20f), 2.1f);
            pentad[0] = whatWillIShoot(m, d.rotateLeftDegrees(15f), 2.1f);
            pentad[1] = whatWillIShoot(m, d.rotateLeftDegrees(30f), 2.1f);
            pentad[2] = whatWillIShoot(m, d.rotateRightDegrees(15f), 2.1f);
            pentad[3] = whatWillIShoot(m, d.rotateRightDegrees(30f), 2.1f);
        }
    }

    private static BodyInfo whatWillIShoot(MapLocation m, Direction d, float offset) throws GameActionException {
        MapLocation bullet = m.add(d, offset);
        if(rc.getLocation().distanceTo(bullet) > 7){
            return null;
        }
        RobotInfo r = rc.senseRobotAtLocation(bullet);
        TreeInfo t = rc.senseTreeAtLocation(bullet);
        if (r == null && t == null) {
            if (offset >= 6.5f) {
                return null;
            } else {
                return whatWillIShoot(m, d, offset + 0.5f);
            }
        } else if (r == null) {
            return t;
        } else {
            return r;
        }
    }

    private static boolean isEnemyOrNull(BodyInfo b) {
        if (b == null) {
            return true;
        } else if (b.isRobot()) {
            return (((RobotInfo) b).getTeam() != myTeam);
        } else if (b.isTree()) {
            return (((TreeInfo) b).getTeam() != myTeam);
        }
        return true;
    }

    private static void shoot(RobotInfo r) throws GameActionException {
        Direction d = rc.getLocation().directionTo(r.getLocation());
        lineOfSight(rc.getLocation(), d, false);
        boolean p = true;
        for (int i = 0; i < 4; i++) {
            p = p && isEnemyOrNull(pentad[i]);
        }
        boolean t = isEnemyOrNull(triad[0]) && isEnemyOrNull(triad[1]);
        if (rc.canFirePentadShot() && p) {
            rc.firePentadShot(d);
        } else if (rc.canFireTriadShot() && t) {
            rc.fireTriadShot(d);
        }
        if (rc.canFireSingleShot()) {
            rc.fireSingleShot(d);
        }
    }

    private static boolean hasLineOfSight(MapLocation m, RobotInfo r) throws GameActionException {
        lineOfSight(m, m.directionTo(r.getLocation()), true);
        if (direct == null) {
            return false;
        }
        return (direct.getID() == r.getID());
    }

    private static boolean moveToDodge(RobotInfo r) throws GameActionException {
        Direction currentDir;
        boolean found = false;
        float distance;
        Direction bestDirection = new Direction(pi);
        float bestDistance = 0.0f;
        float currentScore;
        float bestScore = 10000;
        BulletInfo[] nearBullets = rc.senseNearbyBullets(4.0f);
        if (hasLineOfSight(here, r)) {
            found = true;
            bestScore = evaluatePosition(here, nearBullets);
        }
        if (here.distanceTo(r.getLocation()) < 5.5 && targetEnemy.getType() != RobotType.GARDENER) {
            currentDir = here.directionTo(r.getLocation()).opposite();
            float offset = 0.0f;
            while (!rc.canMove(currentDir) && offset < 180.0f) {
                currentDir.rotateLeftRads(15f);
                offset = offset + 15f;
            }
            if (rc.canMove(currentDir)) {
                rc.move(currentDir);
            } else {
                offset = 0.0f;
                while (!rc.canMove(currentDir) && offset < 180.0f) {
                    currentDir.rotateRightRads(15f);
                    offset = offset + 15f;
                }
                if (rc.canMove(currentDir)) {
                    rc.move(currentDir);
                }
            }
            if (hasLineOfSight(rc.getLocation(), r)) {
                found = true;
            }
        } else {
            for (int i = 0; i < 8; i++) {
                currentDir = new Direction(i * pi / 4);
                distance = 1.6f;
                if (rc.canMove(currentDir) && isValidMove(r.getLocation().distanceTo(here.add(currentDir, 0.5f)), r) && hasLineOfSight(here.add(currentDir, 0.5f), r)) {
                    currentScore = evaluatePosition(here.add(currentDir, distance), nearBullets);
                    found = true;
                    if (currentScore < bestScore) {
                        bestDirection = currentDir;
                        bestDistance = distance;
                        bestScore = currentScore;
                    }
                }

            }
            if (found) {
                rc.move(bestDirection, bestDistance);
            } else {
                mc.updateTarget(r.getLocation());
                mc.move(0.0f);
            }
        }
        return found;
    }

    private static boolean isValidMove(float distanceToEnemy, RobotInfo r) {
        if (r.getType() == RobotType.LUMBERJACK) {
            return (distanceToEnemy > 4.0f && distanceToEnemy < 6.3f);
        } else if (r.getType() == RobotType.SOLDIER || r.getType() == RobotType.TANK) {
            return (distanceToEnemy > 6.1f);
        } else {
            return (distanceToEnemy < 6.3f);
        }
    }

    private static float evaluatePosition(MapLocation m, BulletInfo[] nearBullets) {
        float s = 0;
        for (BulletInfo i : nearBullets) {
            if (willCollideWithMe(m, i)) {
                s = s + i.getDamage();
            }
        }
        return s;
    }

    static boolean willCollideWithMe(MapLocation m, BulletInfo bullet) {
        MapLocation myLocation = m;

        // Get relevant bullet information
        Direction propagationDirection = bullet.dir;
        MapLocation bulletLocation = bullet.location;

        // Calculate bullet relations to this robot
        Direction directionToRobot = bulletLocation.directionTo(myLocation);
        float distToRobot = bulletLocation.distanceTo(myLocation);
        float theta = propagationDirection.radiansBetween(directionToRobot);

        // If theta > 90 degrees, then the bullet is traveling away from us and we can break early
        if (Math.abs(theta) > Math.PI / 2) {
            return false;
        }

        // distToRobot is our hypotenuse, theta is our angle, and we want to know this length of the opposite leg.
        // This is the distance of a line that goes from myLocation and intersects perpendicularly with propagationDirection.
        // This corresponds to the smallest radius circle centered at our location that would intersect with the
        // line that is the path of the bullet.
        float perpendicularDist = (float) Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

        return (perpendicularDist <= rc.getType().bodyRadius);
    }

    private static void beSoldier() throws GameActionException {
        if (nearEnemyRobot.length != 0) {
            beginning = here;
            if (canSenseTargetRobot(targetEnemyRobotID)) {

            } else {
                target = nearEnemyRobot[0].getLocation();
                targetEnemyRobotID = nearEnemyRobot[0].getID();
            }
            while (!bug2MoveAlex(beginning, target, 7.0f)) {
                Clock.yield();
                updateRobot();
                if (canSenseTargetRobot(targetEnemyRobotID)) {

                } else if (nearEnemyRobot.length != 0) {
                    target = nearEnemyRobot[0].getLocation();
                } else {
                    break;
                }
            }
            delta = pi / 2;
            // if(rc.canFirePentadShot()){
            //    rc.firePentadShot(here.directionTo(target));
            // }
            // else if(rc.canFireTriadShot()){
            //    rc.fireTriadShot(here.directionTo(target));
            //}
            if (rc.canFireSingleShot()) {
                rc.fireSingleShot(here.directionTo(target));
            }
        } else if (!arrived) {
            if (bug2MoveAlex(here, nearestEAStartingLocation, 5.0f)) {
                arrived = true;
            }
        } else {
            moveRandom();
        }
    }


    private static boolean canSenseTargetRobot(int id) {
        for (RobotInfo i : nearEnemyRobot) {
            if (i.getID() == id) {
                target = i.getLocation();
                return true;
            }
        }
        return false;

    }
}

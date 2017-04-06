package r2d2Scout;

import battlecode.common.*;

public class RobotPlayer {
    static RobotController rc;

    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        r2d2Scout.RobotPlayer.rc = rc;
        Global.init(rc);
        // Here, we've separated the controls into a different method for each RobotType.
        // You can add the missing ones or rewrite this into your own control structure.
        switch (rc.getType()) {
            case ARCHON:
               Archon.loop();
                break;
            case GARDENER:
                Gardener.loop();
                break;
            case SOLDIER:
                Soldier.loop();
                break;
            case LUMBERJACK:
                Hulk.loop();
                break;
            case SCOUT:
                Scout.loop();
                break;
        }
    }
}

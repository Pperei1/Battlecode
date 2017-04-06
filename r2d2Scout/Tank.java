package r2d2Scout;
import battlecode.common.*;

public class Tank extends Global{
    public static void loop() throws GameActionException{
        while(true){
            try{

            }
            catch(Exception e){
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
            Clock.yield();
        }
    }
}

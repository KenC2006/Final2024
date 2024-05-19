public class MeleeEnemy extends Enemy {

    public final static double defaultHeight = 5;
    public final static double defaultWidth = 2;
    public final static double defaultWalkSpeed = 5;

    public MeleeEnemy(double x, double y, int health) {
        super(x, y, 2, 5, health, 10);
    }

    // need weapon implementation for sword

    public String getType() {
        return "MELEE";
    }

    public void dashLeft() {

    }
    public void dashRight() {

    }
    public void swingSword() {

    }
}
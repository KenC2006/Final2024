import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel{
    private ActionManager manager = new ActionManager();
    private RoomManager roomManager = new RoomManager();
    private Player player = new Player(10, 10);
    private Camera camera = new Camera(10);
    private boolean isRunning = true;
    private GameCharacter tempCharacter = new GameCharacter(20, 20, 3, 4,100);

    public GamePanel() {
        this.setLayout(null);
        this.setFocusable(true);
        this.setVisible(true);
        manager.addPanel(this);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;
        camera.setGraphics(g);
        player.paint(camera);
        tempCharacter.paint(camera);
        roomManager.drawRooms(camera);
        camera.paint();
    }

    public void update() {


        camera.updateKeyPresses(manager);
        player.updateKeyPresses(manager);

        if (player.collidesWith(tempCharacter)) {

        }
    }

    public void start() {
        Thread gameLoop = new Thread(() -> { // TEMPORARY FOR NOW https://stackoverflow.com/questions/65907092/where-should-i-put-the-game-loop-in-the-swing-app
            // how many frames should be drawn in a second
            final int FRAMES_PER_SECOND = 60;
            // calculate how many nano seconds each frame should take for our target frames per second.
            final long TIME_BETWEEN_UPDATES = 1000000000 / FRAMES_PER_SECOND;
            // track number of frames
            int frameCount;
            // if you're worried about visual hitches more than perfect timing, set this to 1. else 5 should be okay
            final int MAX_UPDATES_BETWEEN_RENDER = 1;

            // we will need the last update time.
            long lastUpdateTime = System.nanoTime();
            // store the time we started this will be used for updating map and charcter animations
            long currTime = System.currentTimeMillis();


            while (isRunning) {
                long now = System.nanoTime();
                long elapsedTime = System.currentTimeMillis() - currTime;
                currTime += elapsedTime;

                int updateCount = 0;
                // do as many game updates as we need to, potentially playing catchup.
                while (now - lastUpdateTime >= TIME_BETWEEN_UPDATES && updateCount < MAX_UPDATES_BETWEEN_RENDER) {
                    update(); //Update the entity movements and collision checks etc (all has to do with updating the games status i.e  call move() on Enitites)
                    lastUpdateTime += TIME_BETWEEN_UPDATES;
                    updateCount++;
                }

                // if for some reason an update takes forever, we don't want to do an insane number of catchups.
                // if you were doing some sort of game that needed to keep EXACT time, you would get rid of this.
                if (now - lastUpdateTime >= TIME_BETWEEN_UPDATES) {
                    lastUpdateTime = now - TIME_BETWEEN_UPDATES;
                }

                repaint(); // draw call for rendering sprites etc

                long lastRenderTime = now;

                //Yield until it has been at least the target time between renders. This saves the CPU from hogging.
                while (now - lastRenderTime < TIME_BETWEEN_UPDATES && now - lastUpdateTime < TIME_BETWEEN_UPDATES) {
                    Thread.yield();
                    now = System.nanoTime();
                }
            }
        });

        gameLoop.start();
    }
}

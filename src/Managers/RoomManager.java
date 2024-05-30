package Managers;

import Camera.Camera;
import Structure.Entrance;
import Structure.Room;
import Structure.Vector2F;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class RoomManager {
    private final ArrayList<Room> allPossibleRooms = new ArrayList<>();
    private ArrayList<Room> allRooms = new ArrayList<>();
    private ArrayList<Room> loadedRooms = new ArrayList<>();
    private Deque<Room> toGenerateNeighbours = new ArrayDeque<>();

    public RoomManager() {
//        createRectangleRoom();
        loadRoomsFromFile();
        generateRooms();
    }

    public void createRectangleRoom() {
        Room newRoom = new Room(-40, -40, 80, 80);
        allRooms.add(newRoom);
        loadedRooms.add(newRoom);
    }

    public void drawRooms(Camera c) {
        for (Room room : loadedRooms) {
            room.drawRoom(c);
        }
    }

    public ArrayList<Room> getLoadedRooms() {
        return loadedRooms;
    }

    public void generateRooms() {
        if (allPossibleRooms.isEmpty()) return;
        Room randomRoom = allPossibleRooms.get((int) (Math.random() * allPossibleRooms.size()));
//        Room randomRoom = new Room(allPossibleRooms.get(4));
        Vector2F center = randomRoom.getCenterRelativeToRoom();
        randomRoom.centerAroundPointInRoom(center);
        addRoom(randomRoom);
        loadRoom(randomRoom);

        toGenerateNeighbours.add(randomRoom);
        while (!toGenerateNeighbours.isEmpty() && allRooms.size() < 100) {
            generateAttached(toGenerateNeighbours.pollFirst());
        }

        for (Room r: allRooms) {
            r.closeEntrances();
        }
    }

    public void generateAttached(Room r) {
        for (Entrance e: r.getEntrances()) {
            if (e.isConnected()) continue;
            ArrayList<Room> compatibleRooms = new ArrayList<>();
            for (Room newRoom: allPossibleRooms) {
                boolean compatible = false;
                Room testRoom = new Room(newRoom);
                testRoom.setDrawLocation(r.getDrawLocation().getTranslated(r.getCenterLocation().getNegative()).getTranslated(e.getLocation()));
                for (Entrance connectingEntrance: testRoom.getEntrances()) {
                    if (!e.connects(connectingEntrance)) continue;
                    testRoom.centerAroundPointInRoom(connectingEntrance.getConnection());

                    boolean collides = false;
                    for (Room collsionTest: allRooms) {
                        if (testRoom.quickIntersect(collsionTest) && testRoom.intersects(collsionTest)) {
                            collides = true;
                            break;
                        }
                    }

                    if (collides) continue;
                    connectingEntrance.setConnected(true);
                    compatibleRooms.add(testRoom);
                    break;
                }
            }

            int randomRoom = (int)(Math.random() * compatibleRooms.size());
            if (compatibleRooms.isEmpty()) continue;
            e.setConnected(true);
            addRoom(compatibleRooms.get(randomRoom));
            loadRoom(compatibleRooms.get(randomRoom));
            toGenerateNeighbours.add(compatibleRooms.get(randomRoom));
        }
    }

    public void loadRoomsFromFile() {
        for (File f: Objects.requireNonNull(new File("src/Rooms").listFiles())) {
            try {
                allPossibleRooms.add(new Room(f));
            } catch (FileNotFoundException e) {
                System.out.println("Unable to load file " + f.getName());
                System.out.println(e);
            }
        }
    }

    private void loadRoom(Room r) {
        loadedRooms.add(r);
    }

    private void addRoom(Room r) {
        allRooms.add(r);
    }

    public void update() {

    }
}
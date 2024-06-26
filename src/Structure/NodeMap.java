package Structure;

import java.util.*;

import RoomEditor.Entrance;
import RoomEditor.Spawn;
import Universal.Camera;

/**
 * Represents a map of nodes and their connections within a room.
 */
public class NodeMap {

    // List of nodes in the map
    private final ArrayList<Vector2F> nodes;

    // Map of edges connecting nodes
    private Map<Vector2F, ArrayList<Vector2F>> edges;

    // List of enemy spawn points in the room
    private ArrayList<Spawn> enemySpawns;

    // Spawn point for the player
    private Spawn playerSpawn;

    // Room associated with the node map
    private Room room;

    // KDTree for efficient spatial queries
    private KDTree kdTree;

    // Offset used for translating grid coordinates
    private final int gridOffset = 500;

    // Offset vector for translation
    private Vector2F translateOffset;

    // Grid representing the spatial layout of the room
    private char[][] grid;

    /**
     * Constructor to initialize a NodeMap based on a room.
     *
     * @param room The room for which the node map is being constructed.
     */
    public NodeMap(Room room) {
        nodes = new ArrayList<Vector2F>();
        edges = new HashMap<Vector2F, ArrayList<Vector2F>>();
        enemySpawns = room.getEnemySpawns();

        // Set player spawn point; if not provided, default to first enemy spawn
        if (room.getPlayerSpawns().isEmpty()) {
            playerSpawn = new Spawn(room.getEnemySpawns().get(0).getLocation(), Spawn.SpawnType.PLAYER);
        } else {
            playerSpawn = room.getPlayerSpawns().get(0);
        }

        grid = new char[1000][1000];
        this.room = room;

        // Load grid and nodes
        loadGrid(room);
        loadNodes(new Vector2F(playerSpawn.getX()/1000 + 500, playerSpawn.getY()/1000 + 500), room);
        nodes.add(new Vector2F(playerSpawn.getX(), playerSpawn.getY()));

        HashSet<Vector2F> set = new HashSet<Vector2F> (nodes);
        nodes.clear();
        nodes.addAll(set);

        // Build KDTree for efficient nearest neighbor queries
        kdTree = new KDTree(nodes);

        // Connect nodes based on grid and room layout
        for (Vector2F node : nodes) {
            connectNodes(new Vector2F(node.getX() / 1000 + gridOffset, node.getY() / 1000 + gridOffset), room);
        }
    }

    /**
     * Copy constructor to create a deep copy of a NodeMap.
     *
     * @param copy The NodeMap instance to be copied.
     */
    public NodeMap(NodeMap copy) {
        nodes = copy.nodes;
        edges = copy.edges;
        enemySpawns = copy.enemySpawns;
        playerSpawn = copy.playerSpawn;
        grid = copy.grid;
        translateOffset = new Vector2F();
        kdTree = copy.kdTree;
        room = copy.room;
    }

    /**
     * Loads the grid representation of the room based on its hitboxes.
     *
     * @param room The room for which the grid is being loaded.
     */
    private void loadGrid(Room room) {
        for (Hitbox hitbox : room.getHitbox().getHitboxes()) {
            for (int i = hitbox.getTop()/1000; i < hitbox.getBottom()/1000; i++) {
                for (int j = hitbox.getLeft()/1000; j <= hitbox.getRight()/1000; j++) {
                    grid[i+gridOffset][j+gridOffset] = 'X'; // since array index must be > 0
                }
            }
        }

        for (Entrance e : room.getEntrances()) {
            for (int i = e.getHitbox().getTop()/1000; i < e.getHitbox().getBottom()/1000; i++) {
                for (int j = e.getHitbox().getLeft()/1000; j <= e.getHitbox().getRight()/1000; j++) {
                    if (grid[i+gridOffset][j+gridOffset] == 'X') continue;
                    grid[i+gridOffset][j+gridOffset] = 'E'; // since array index must be > 0
                }
            }
        }
    }

    /**
     * given a grid where 'X' represents a wall, use bfs to find possible node locations
     * within a raidus, then mark as 'V' and store as a node
     * @param start
     */
    private void loadNodes(Vector2F start, Room room) {
        Queue<Vector2F> q = new LinkedList<Vector2F>();
        Vector2F cur_node, ogCur_node, ogStart; // og stores original node coords
        q.add(start);
        boolean[][] v = new boolean[1001][1001];
        v[start.getX()][start.getY()] = true;
        ogStart = new Vector2F((start.getX() - gridOffset) * 1000, (start.getY() - gridOffset) * 1000);
        while (!q.isEmpty()) {
            cur_node = q.remove();
            ogCur_node = new Vector2F((cur_node.getX() - gridOffset) * 1000, (cur_node.getY() - gridOffset) * 1000);
            if (grid[cur_node.getY()][cur_node.getX()] == 'E' ||
                    grid[cur_node.getY()][cur_node.getX()] == 'D') {
                continue;
            }
            if (cur_node.getY() + 1 < 1000 && cur_node.getX() - 1 >= 0 && cur_node.getX() + 1 < 1000 &&
                    grid[cur_node.getY()][cur_node.getX()] != 'X' &&
                    grid[cur_node.getY()][cur_node.getX()] != 'V' &&
                    (grid[cur_node.getY() + 1][cur_node.getX()] == 'X' ||
                            grid[cur_node.getY() + 1][cur_node.getX()] == 'E')) {

                if (grid[cur_node.getY() + 1][cur_node.getX() - 1] == 0 &&
                        grid[cur_node.getY()][cur_node.getX() - 1] == 0) {
                    fillPlatForm(cur_node, start, room);

                } else if (grid[cur_node.getY() + 1][cur_node.getX() + 1] == 0 &&
                        grid[cur_node.getY()][cur_node.getX() + 1] == 0) {
                    fillPlatForm(cur_node, start, room);
                } else if (grid[cur_node.getY()][cur_node.getX() - 1] == 'X' ||
                        grid[cur_node.getY()][cur_node.getX() - 1] == 'E') {
                    fillPlatForm(cur_node, start, room);
                } else if (grid[cur_node.getY()][cur_node.getX() + 1] == 'X' ||
                        grid[cur_node.getY()][cur_node.getX() + 1] == 'E') {
                    fillPlatForm(cur_node, start, room);
                } else if ((grid[cur_node.getY() + 1][cur_node.getX() + 1] == 'X' ||
                        grid[cur_node.getY() + 1][cur_node.getX() - 1] == 'X') &&
                        grid[cur_node.getY() + 1][cur_node.getX()] == 'E') {
                    grid[cur_node.getY()][cur_node.getX()] = 'V';
                    nodes.add(ogCur_node);
                }
            }

            if (cur_node.getY() + 1 < 1000 &&
                    grid[cur_node.getY() + 1][cur_node.getX()] != 'X' &&
                    !v[cur_node.getY() + 1][cur_node.getX()]) {
                q.add(new Vector2F(cur_node.getX(), cur_node.getY() + 1));
                v[cur_node.getY() + 1][cur_node.getX()] = true;
            }
            if (cur_node.getY() - 1 >= 0 &&
                    grid[cur_node.getY() - 1][cur_node.getX()] != 'X' &&
                    !v[cur_node.getY() - 1][cur_node.getX()]) {
                q.add(new Vector2F(cur_node.getX(), cur_node.getY() - 1));
                v[cur_node.getY() - 1][cur_node.getX()] = true;
            }
            if (cur_node.getX() + 1 < 1000 &&
                    grid[cur_node.getY()][cur_node.getX() + 1] != 'X' &&
                    !v[cur_node.getY()][cur_node.getX() + 1]) {
                q.add(new Vector2F(cur_node.getX() + 1, cur_node.getY()));
                v[cur_node.getY()][cur_node.getX() + 1] = true;
            }
            if (cur_node.getX() - 1 >= 0 &&
                    grid[cur_node.getY()][cur_node.getX() - 1] != 'X' &&
                    !v[cur_node.getY()][cur_node.getX() - 1]) {
                q.add(new Vector2F(cur_node.getX() - 1, cur_node.getY()));
                v[cur_node.getY()][cur_node.getX() - 1] = true;
            }
        }
        grid[start.getY()][start.getX()] = 'V';
    }

    /**
     * Connects nodes starting from a given position in a grid-based system.
     * Uses BFS to explore neighboring nodes and connects them if conditions are met.
     *
     * @param start The starting position of node connectivity.
     * @param room The room object containing hitboxes to avoid intersections.
     */
    public void connectNodes(Vector2F start, Room room) {
        Queue<Vector2F> q = new LinkedList<>();
        q.add(start);

        Vector2F cur_node, ogCur_node, ogStart = new Vector2F((start.getX() - gridOffset) * 1000, (start.getY() - gridOffset) * 1000);

        boolean[][] v = new boolean[1001][1001];
        v[start.getX()][start.getY()] = true;
        while (!q.isEmpty()) {
            cur_node = q.remove();
            ogCur_node = new Vector2F((cur_node.getX() - gridOffset) * 1000, (cur_node.getY() - gridOffset) * 1000);

            if (cur_node.getEuclideanDistance(start) > 500) {
                continue;
            }
            if (Math.abs(cur_node.getXDistance(start)) > 20) {
                continue;
            }
            if (cur_node.getYDistance(start) > 20) {
                continue;
            }

            if (grid[ cur_node.getY()][ cur_node.getX()] == 'V' && cur_node.getX() != start.getX()) {
                if (!doesIntersectRoom(new Line(ogCur_node, ogStart), room)) {
                    edges.computeIfAbsent(new Vector2F(ogCur_node), k -> new ArrayList<>()).add(new Vector2F(ogStart));
                }

            }

            if (grid[cur_node.getY()+1][cur_node.getX()] != 'X' &&
                    !v[cur_node.getY()+1][cur_node.getX()]) {
                q.add(new Vector2F(cur_node.getX(), cur_node.getY() + 1));
                v[cur_node.getY()+1][cur_node.getX()] = true;
            }
            if (grid[cur_node.getY()-1][cur_node.getX()] != 'X' &&
                    !v[cur_node.getY()-1][cur_node.getX()]) {
                q.add(new Vector2F(cur_node.getX(), cur_node.getY() - 1));
                v[cur_node.getY()-1][cur_node.getX()] = true;
            }
            if (grid[cur_node.getY()][cur_node.getX() + 1] != 'X' &&
                    !v[cur_node.getY()][cur_node.getX() + 1]) {
                q.add(new Vector2F(cur_node.getX() + 1, cur_node.getY()));
                v[cur_node.getY()][cur_node.getX() + 1] = true;
            }
            if (grid[cur_node.getY()][cur_node.getX() - 1] != 'X' &&
                    !v[cur_node.getY()][cur_node.getX() - 1]) {
                q.add(new Vector2F(cur_node.getX() - 1, cur_node.getY()));
                v[cur_node.getY()][cur_node.getX() - 1] = true;
            }
        }
    }


    /**
     * Fills a platform starting from a given position in the grid.
     * Adds nodes to the nodes list and marks corresponding positions in the grid.
     *
     * @param start The starting position in the grid for platform filling.
     * @param prev The previous position used for direction tracking.
     * @param room The room object containing hitboxes to avoid intersections.
     */
    private void fillPlatForm(Vector2F start, Vector2F prev, Room room) {
        int curX = start.getX();
        int dist = 0;

        // Traverse horizontally in the grid to fill platform
        while (grid[start.getY() + 1][curX] == 'X' && !(grid[start.getY()][curX] == 'X')) {
            if (dist % 10 == 0) {
                // Add node to nodes list and mark position in the grid as visited
                nodes.add(new Vector2F((curX - gridOffset) * 1000, (start.getY() - gridOffset) * 1000));
                grid[start.getY()][curX] = 'V';
            } else if (grid[start.getY() + 1][curX + 1] == 0 ||
                    grid[start.getY()][curX + 1] == 'X') {
                // Add node to nodes list and mark position in the grid as visited
                nodes.add(new Vector2F((curX - gridOffset) * 1000, (start.getY() - gridOffset) * 1000));
                grid[start.getY()][curX] = 'V';
            }
            dist++;
            curX++;
        }
    }


    /**
     * Checks if a line segment intersects with any walls in a given room.
     *
     * @param line The line segment to check for intersections.
     * @param room The room object containing hitboxes representing walls.
     * @return true if the line intersects with any wall in the room, false otherwise.
     */
    private boolean doesIntersectRoom(Line line, Room room) {
        for (Hitbox wall : room.getHitbox().getHitboxes()) {
            if (wall.quickIntersect(new Hitbox(line.getStart(), line.getEnd()))) {
                if (line.doesIntersect(wall)) {
                    return true;
                }
            }
        }
        return false;
    }


    public Map<Vector2F, ArrayList<Vector2F>> getEdges() {
        return edges;
    }

    public ArrayList<Vector2F> getNodes() {
        return nodes;
    }

    public Vector2F getTranslateOffset() {
        return translateOffset;
    }
    public void setTranslateOffset(Vector2F value) {
        translateOffset = new Vector2F(translateOffset.getTranslated(value));
    }

    public Vector2F getNearestNode(Vector2F point) {

        return kdTree.findNearest(point);
    }

    public char[][] getGrid() {
        return grid;
    }

    public int getGridOffset() {
        return gridOffset;
    }

    public Room getRoom() { return room; }

    public void drawNodes(Camera c) {
        for (Vector2F n : nodes) {
            if (edges.get(n) == null) {
                continue;
            }
            for (Vector2F connectedNode : edges.get(n)) {

            }
        }
    }
}

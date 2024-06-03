package Structure;

import java.awt.*;
import java.util.*;

import Camera.Camera;

public class NodeMap {

    private ArrayList<Vector2F> nodes;
    private Map<Vector2F, ArrayList<Vector2F>> edges;

    private char[][] grid;

    public NodeMap(Room room) {
        nodes = new ArrayList<Vector2F> ();
        edges = new HashMap<Vector2F, ArrayList<Vector2F>> (); //add edge connecting

        grid = new char[500][500];

        loadGrid(room);
        loadNodes(new Vector2F(252, 252), room);
//        loadNodes(new Vector2F(room.getCenterRelativeToRoom().getX() + 250, room.getCenterRelativeToRoom().getY() + 250), room);
        nodes.add(new Vector2F(2, 2));
        int idx = 0;
        while (idx < nodes.size()) {
            loadNodes(new Vector2F(nodes.get(idx).getX() + 250, nodes.get(idx).getY() + 250), room);
            idx++;
        }
        HashSet<Vector2F> set = new HashSet<Vector2F> (nodes);
        nodes.clear();
        nodes.addAll(set);

//        System.out.println(nodes);
//        System.out.println(nodes.size());
    }

    private void loadGrid(Room room) {
        for (Hitbox hitbox : room.getHitbox().getHitboxes()) {
            for (int i = (int)hitbox.getTop(); i <= (int)hitbox.getBottom(); i++) {
                for (int j = (int)hitbox.getLeft(); j <= hitbox.getRight(); j++) {
//                        System.out.printf("%d %d\n", i, j);
                    grid[i+250][j+250] = 'X'; // since array index must be > 0
                }
            }
        }

        for (Entrance e : room.getEntrances()) {
            for (int i = (int)e.getHitbox().getTop(); i <= (int)e.getHitbox().getBottom(); i++) {
                for (int j = (int)e.getHitbox().getLeft(); j <= e.getHitbox().getRight(); j++) {
//                        System.out.printf("%d %d\n", i, j);
                    if (grid[i+250][j+250] == 'X') continue;
                    grid[i+250][j+250] = 'E'; // since array index must be > 0
                }
            }
        }
//        grid[(int)room.getCenterRelativeToRoom().getY() + 250][(int)room.getCenterRelativeToRoom().getX() + 250] = 'S';

//        for(int i = 100; i < 300; i++) {
//            for (int j = 100; j < 300; j++) {
//                System.out.printf("%c", grid[i][j]);
//            }
//            System.out.println();
//        }
//        System.out.println(room.getCenterRelativeToRoom());
    }

    /**
     * given a grid where 'X' represents a wall, use bfs to find possible node locations
     * within a raidus, then mark as 'V' and store as a node
     * @param start
     */
    private void loadNodes(Vector2F start, Room room) {
        Queue<Vector2F> q = new LinkedList<Vector2F> ();
        Vector2F cur_node, ogCur_node, ogStart; // og stores original node coords
        boolean[][] v = new boolean[2000][2000];
        q.add(start);
        v[(int)start.getX()][(int)start.getY()] = true;
        ogStart = new Vector2F(start.getX() - 250, start.getY() - 250);
//        grid[(int)start.getY()][(int)start.getX()] = 'S';
//        System.out.println("start = " + start);
        while(!q.isEmpty()) {
            cur_node = q.remove();
            ogCur_node = new Vector2F(cur_node.getX() - 250, cur_node.getY() - 250);
            if (grid[(int)cur_node.getY()][(int)cur_node.getX()] == 'E') {
                continue;
            }
            if (cur_node.getEuclideanDistance(start) > 5000 || Math.abs(cur_node.getY() - start.getY()) > 15) {
                continue;
            }
            if (Math.abs(cur_node.getYDistance(start)) > 0.1 && Math.abs(cur_node.getXDistance(start)) > 20) {
                continue;
            }

            if (grid[(int)cur_node.getY()][(int)cur_node.getX()] == 'V' &&
            !(cur_node.getY() == start.getY() && cur_node.getX() == start.getX())) {
                continue;
            }

//            System.out.println(q.size());
            if (grid[(int)cur_node.getY()][(int)cur_node.getX()] != 'X' &&
                grid[(int)cur_node.getY()][(int)cur_node.getX()] != 'V' &&
                (grid[(int)cur_node.getY()+1][(int)cur_node.getX()] == 'X' ||
                grid[(int)cur_node.getY()+1][(int)cur_node.getX()] == 'E') &&
                !(cur_node.getX() == start.getX() && cur_node.getY() == start.getY())) {

                if (grid[(int)cur_node.getY()+1][(int)cur_node.getX()-1]  == 0 &&
                    grid[(int)cur_node.getY()][(int)cur_node.getX()-1] == 0) {
                    if (doesIntersectRoom(new Line(ogCur_node, ogStart), room)) {
                        continue;
                    }
//                    grid[(int)cur_node.getY()][(int)cur_node.getX()] = 'V';
                    nodes.add(ogCur_node);
                    edges.computeIfAbsent(ogStart, k -> new ArrayList<>()).add(ogCur_node);
//                    System.out.println("adding " + ogCur_node + " to " + ogStart);

                }
                else if (grid[(int)cur_node.getY()+1][(int)cur_node.getX()+1] == 0 &&
                        grid[(int)cur_node.getY()][(int)cur_node.getX()+1] == 0) {
                    if (doesIntersectRoom(new Line(ogCur_node, ogStart), room)) {
                        continue;
                    }
//                    grid[(int)cur_node.getY()][(int)cur_node.getX()] = 'V';
                    nodes.add(ogCur_node);
                    edges.computeIfAbsent(ogStart, k -> new ArrayList<>()).add(ogCur_node);
                    edges.computeIfAbsent(ogCur_node, k -> new ArrayList<>()).add(ogStart);
//                    System.out.println("adding " + ogCur_node + " to " + ogStart);
                }
                else if (grid[(int)cur_node.getY()][(int)cur_node.getX()-1] == 'X') {
                    if (doesIntersectRoom(new Line(ogCur_node, ogStart), room)) {
                        continue;
                    }
//                    grid[(int)cur_node.getY()][(int)cur_node.getX()] = 'V';
                    nodes.add(ogCur_node);
                    edges.computeIfAbsent(ogStart, k -> new ArrayList<>()).add(ogCur_node);
                    edges.computeIfAbsent(ogCur_node, k -> new ArrayList<>()).add(ogStart);
//                    System.out.println("adding " + ogCur_node + " to " + ogStart);
                }
                else if (grid[(int)cur_node.getY()][(int)cur_node.getX()+1] == 'X') {
                    if (doesIntersectRoom(new Line(ogCur_node, ogStart), room)) {
                        continue;
                    }
//                    grid[(int)cur_node.getY()][(int)cur_node.getX()] = 'V';
                    nodes.add(ogCur_node);
                    edges.computeIfAbsent(ogStart, k -> new ArrayList<>()).add(ogCur_node);
                    edges.computeIfAbsent(ogCur_node, k -> new ArrayList<>()).add(ogStart);
//                    System.out.println("adding " + ogCur_node + " to " + ogStart);
                }
            }

            if (grid[(int)cur_node.getY()+1][(int)cur_node.getX()] != 'X' &&
                !v[(int)cur_node.getY()+1][(int)cur_node.getX()]) {
                q.add(new Vector2F(cur_node.getX(), cur_node.getY() + 1));
                v[(int)cur_node.getY()+1][(int)cur_node.getX()] = true;
            }
            if (grid[(int)cur_node.getY()-1][(int)cur_node.getX()] != 'X' &&
                !v[(int)cur_node.getY()-1][(int)cur_node.getX()]) {
                q.add(new Vector2F(cur_node.getX(), cur_node.getY() - 1));
                v[(int)cur_node.getY()-1][(int)cur_node.getX()] = true;
            }
            if (grid[(int)cur_node.getY()][(int)cur_node.getX() + 1] != 'X' &&
                !v[(int)cur_node.getY()][(int)cur_node.getX() + 1]) {
                q.add(new Vector2F(cur_node.getX() + 1, cur_node.getY()));
                v[(int)cur_node.getY()][(int)cur_node.getX() + 1] = true;
            }
            if (grid[(int)cur_node.getY()][(int)cur_node.getX() - 1] != 'X' &&
                !v[(int)cur_node.getY()][(int)cur_node.getX() - 1]) {
                q.add(new Vector2F(cur_node.getX() - 1, cur_node.getY()));
                v[(int)cur_node.getY()][(int)cur_node.getX() - 1] = true;
            }
        }
        grid[(int)start.getY()][(int)start.getX()] = 'V';
//        for (Vector2F n : nodes) {
//            System.out.println(n + " is connect to " + edges.get(n));
//        }
//        for(int i = 200; i < 300; i++) {
//            for (int j = 200; j < 300; j++) {
//                System.out.printf("%c", grid[i][j]);
//            }
//            System.out.println();
//        }
    }

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

    public void translateNodes(Vector2F newDrawLocation) {
        Vector2F newNode, newNextNode;
        for (int i = 0; i < nodes.size(); i++) {
            ArrayList<Vector2F> oldNodes = edges.remove(nodes.get(i));
            newNode = new Vector2F(nodes.get(i).getX() + newDrawLocation.getX(), nodes.get(i).getY() + newDrawLocation.getY());
            if (oldNodes == null) {
                continue;
            }
            for (Vector2F node : oldNodes) {
                newNextNode = new Vector2F(node.getX() + newDrawLocation.getX(), node.getY() + newDrawLocation.getY());
                edges.computeIfAbsent(newNode, k -> new ArrayList<Vector2F> ()).add(newNextNode);
            }
            nodes.set(i, newNode);
        }
    }

    public Map<Vector2F, ArrayList<Vector2F>> getEdges() {
        return edges;
    }

    public ArrayList<Vector2F> getNodes() {
        return nodes;
    }

    public void drawNodes(Camera c) {
        for (Vector2F n : nodes) {
//            c.drawCoordinate(n);
//            System.out.println(n + " " + edges.get(n));

            if (edges.get(n) == null) {
                continue;
            }
            for (Vector2F connectedNode : edges.get(n)) {
//                System.out.println("drawing line from" + n + " to " + connectedNode + "\n");
                c.drawCoordinate(connectedNode);
//                c.drawLine(n, connectedNode, Color.BLUE);
            }
        }
    }
}
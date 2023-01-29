package me.dinozoid.strife.util.pathfinder;

import net.minecraft.block.BlockAir;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.Comparator;

public class PathFinder {

    private static final Node[] expansions = {
            new Node(0, 1, 0),
            new Node(1, 0, 0),
            new Node(0, 0, 1),
            new Node(0, -1, 0),
            new Node(-1, 0, 0),
            new Node(0, 0, -1),
            new Node(1, 0, 1),
            new Node(-1, 0, -1),
            new Node(1, 0, -1),
            new Node(-1, 0, 1),
            new Node(1, 1, 0),
            new Node(-1, 1, 0),
            new Node(0, 1, -1),
            new Node(0, 1, -1),
            new Node(1, -1, 0),
            new Node(-1, -1, 0),
            new Node(0, -1, -1),
            new Node(0, -1, -1),
            new Node(1, 1, 1),
            new Node(-1, 1, 1),
            new Node(1, 1, -1),
            new Node(-1, 1, -1),
            new Node(1, -1, 1),
            new Node(-1, -1, 1),
            new Node(1, -1, -1),
            new Node(-1, -1, -1)
    };

    /**
     * Author: Submaryne#4616. DO NOT DISTRIBUTE
     */

    Node start;
    Node destination;
    ArrayList<Node> open = new ArrayList<>();
    ArrayList<Node> closed = new ArrayList<>();

    public PathFinder() {
    }

    public PathFinder(Node start, Node destination) {
        setStart(start);
        setDestination(destination);
    }

    public static double calculateDistance(Node target, Node current) {
        double distX = Math.abs(current.getX() - target.getX());
        double distY = Math.abs(current.getY() - target.getY());
        double distZ = Math.abs(current.getZ() - target.getZ());
        return Math.sqrt(distX * distX + distY * distY + distZ * distZ);
    }

    public ArrayList<Node> getPath(int reiterations) {
        if (start == null) return new ArrayList<>();
        Node current = start;
        current.setParentNode(null);
        open.add(current);
        for (int i = 0; i < reiterations; i++) {
            current = getLowestCostNode();
            for (Node expansion : expansions) {
                Node expand = current.expand(expansion);
                Node closedNode = null;
                for (Node n : closed)
                    if (n.equals(expand))
                        closedNode = n;
                if (closedNode != null && !isTraversable(expand, current)) {
                    continue;
                }
                expand.setParentNode(current);
                expand.setFCost(calculateDistance(start, expand) + calculateDistance(destination, expand));
                Node existingNode = null;
                for (Node n : open)
                    if (n.equals(expand))
                        existingNode = n;
                if (existingNode != null) {
                    double gCostExisting = calculateDistance(start, existingNode);
                    double gCostExpand = calculateDistance(start, expand);
                    if (gCostExpand < gCostExisting) {
                        existingNode.setFCost(gCostExisting + calculateDistance(destination, existingNode));
                        existingNode.setParentNode(current);
                    }
                } else {
                    open.add(expand);
                }
            }
            switchNode(current);
            if (current.equals(destination)) {
                break;
            }
        }
        ArrayList<Node> path = new ArrayList<>();
//        for(int i = 0; i < getPathSize(current); i++) {
//            path.add(current);
//            current = current.getParentNode();
//        }
        while (current.getParentNode() != null) {
            path.add(current);
            current = current.getParentNode();
        }
        return path;
    }

    private int getPathSize(Node current) {
        int pathSize = 0;
        for (Node closed : closed) {
            if (current != null && current.equals(closed)) {
                current = current.getParentNode();
                pathSize++;
            }
        }
        return pathSize;
    }

    private boolean isInClosed(Node n) {
        for (Node node : closed)
            if (n.equals(node))
                return true;
        return false;
    }

    public void switchNode(Node node) {
        closed.add(node);
        open.remove(node);
    }

    public Node getLowestCostNode() {
        ArrayList<Node> sortingList = new ArrayList<>(open);
        sortingList.sort(new OpenListComparator());
        if (sortingList.isEmpty()) {
            return null;
        }
        return sortingList.get(0);
    }

    public Node getStart() {
        return start;
    }

    public void setStart(Node start) {
        start.setX((int) start.getX());
        start.setY((int) start.getY());
        start.setZ((int) start.getZ());
        reset();
        this.start = start;
    }

    public void reset() {
        open.clear();
        closed.clear();
    }

    public Node getDestination() {
        return destination;
    }

    public void setDestination(Node destination) {
        destination.setX((int) destination.getX());
        destination.setY((int) destination.getY());
        destination.setZ((int) destination.getZ());
        reset();
        this.destination = destination;
    }

    public boolean isTraversable(Node node, Node current) {
        BlockPos pos = new BlockPos(node.getX(), node.getY(), node.getZ());
        double expandX = node.getX() - current.getX();
        double expandZ = node.getZ() - current.getZ();
        Node expandedX = current.expand(new Node(expandX, 0, 0));
        Node expandedZ = current.expand(new Node(0, 0, expandZ));
        BlockPos expandPosX = new BlockPos(expandedX.getX(), expandedX.getY(), expandedX.getZ());
        BlockPos expandPosZ = new BlockPos(expandedZ.getX(), expandedZ.getY(), expandedZ.getZ());
        return pos.getBlock() instanceof BlockAir && pos.add(0, 1, 0).getBlock() instanceof BlockAir && expandPosX.getBlock() instanceof BlockAir && expandPosZ.getBlock() instanceof BlockAir;
    }


    public static class OpenListComparator implements Comparator<Node> {
        @Override
        public int compare(Node o1, Node o2) {
            if (o1.getFCost() < o2.getFCost()) {
                return -1;
            } else if (o1.getFCost() > o2.getFCost()) {
                return 1;
            }
            return 0;
        }
    }
}

package me.dinozoid.strife.util.pathfinder;

public class Node {

    private double x;
    private double y;
    private double z;
    private double fCost;
    private Node parentNode;

    public Node(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Node getParentNode() {
        return parentNode;
    }

    public void setParentNode(Node parentNode) {
        this.parentNode = parentNode;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public Node expand(Node node) {
        return new Node(this.getX() + node.getX(), this.getY() + node.getY(), this.getZ() + node.getZ());
    }

    public double getFCost() {
        return fCost;
    }

    public void setFCost(double fCost) {
        this.fCost = fCost;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Node) {
            Node node = (Node) o;
            return PathFinder.calculateDistance(node, this) < 1.0D;
        }
        return false;
    }
}
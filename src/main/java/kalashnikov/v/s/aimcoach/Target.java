package kalashnikov.v.s.aimcoach;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

class Target {
    private static final Image targetImage = new Image("file:src/images/target.png");
    private double x;
    private double y;
    private double radius;
    private boolean popped = false;

    public Target(double x, double y, double radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getRadius() { return radius; }
    public void setRadius(double radius) { this.radius = radius; }

    public void pop() { popped = true; }
    public boolean isPopped() {
        return popped;
    }

    public boolean contains(double x, double y) {
        if (popped) {
            return false;
        }
        double dx = this.x - x;
        double dy = this.y - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance <= radius;
    }
    public void draw(GraphicsContext gc) {
        if (!popped) {
            gc.drawImage(targetImage, x - radius, y - radius, radius * 2, radius * 2);
        }
    }
}
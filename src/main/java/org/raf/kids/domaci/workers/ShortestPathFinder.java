package org.raf.kids.domaci.workers;

import org.raf.kids.domaci.vo.Message;
import org.raf.kids.domaci.vo.MessageType;

public class ShortestPathFinder implements Runnable {

    private Node node;

    public ShortestPathFinder(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        node.setLength(0);
        node.broadcastMessage(new Message(node.getId(), MessageType.UPDATE, 0));


    }

    public int minimumNeighbourDistance() {
        int min = 10000;
        for (Node node: node.getNeighbours()) {
            min = min > node.getWeight()? node.getWeight():min;
        }
        return min;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }
}

package org.raf.kids.domaci.workers;


import org.raf.kids.domaci.listeners.MessageListener;
import org.raf.kids.domaci.vo.Message;
import org.raf.kids.domaci.vo.MessageType;
import org.raf.kids.domaci.vo.NodeStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Node implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(Node.class);

    private int id;
    private String ip;
    private int communicationPort;
    private int weight;
    private int length = 10000;
    private boolean source;
    private List<Node> neighbours;
    private Node parent;
    private NodeStatus status;
    private Thread nodeThread;
    private String shortestPath;
    private ExecutorService executorService;


    public Node(int id, String ip, int communicationPort, int weight, boolean source, List<Node> neighbours) {
        this.id = id;
        this.ip = ip;
        this.communicationPort = communicationPort;
        this.weight = weight;
        this.source = source;
        this.neighbours = neighbours;
    }

    public Node(int id, String ip, int communicationPort, int weight) {
        this.id = id;
        this.ip = ip;
        this.communicationPort = communicationPort;
        this.weight = weight;
        this.status = NodeStatus.NOT_STARTED;
    }

    public void activateNode() {
        nodeThread = new Thread(this);
        nodeThread.start();
    }

    public void broadcastMessage(Message message) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (Node node : neighbours) {
            executorService.submit(new MessageSender(node, this, message));
        }
    }

    public void sendMessage(Node node, Message message) {
        executorService.submit(new MessageSender(node, this, message));
    }

    @Override
    public void run() {
        this.status = NodeStatus.ACTIVE;
        for (Node node : neighbours) {
            node.setStatus(NodeStatus.ACTIVE);
        }
        executorService = Executors.newCachedThreadPool();
        try {
            MessageListener messageListener = new MessageListener(this);
            messageListener.startListener();
            logger.info("Started node listener for node {}, {} on communicationPort {}", id, ip, communicationPort);
        } catch (Exception e) {
            logger.error("Error opening node listener socket for node {}, {} on communicationPort {}, error: {}", id, ip, communicationPort, e.getMessage());
        }

        if (isSource()) {
            logger.info("Source Node started!");
            executorService.submit(new ShortestPathFinder(this));
        } else {
            logger.info("Node Started");
        }


    }

    public Node getNodeNeighbourById(int nodeId) {
        for (Node node : neighbours) {
            if (node.getId() == nodeId) {
                return node;
            }
        }
        return null;
    }

    public void getClosestPath() {
        sendMessage(getParent(), new Message(getId(), MessageType.CLOSEST_PATH, getId()));
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getCommunicationPort() {
        return communicationPort;
    }

    public void setCommunicationPort(int communicationPort) {
        this.communicationPort = communicationPort;
    }

    public List<Node> getNeighbours() {
        return neighbours;
    }

    public void setNeighbours(List<Node> neighbours) {
        this.neighbours = neighbours;
    }

    public NodeStatus getStatus() {
        return status;
    }

    public void setStatus(NodeStatus status) {
        this.status = status;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public boolean isSource() {
        return source;
    }

    public void setSource(boolean source) {
        this.source = source;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public String getShortestPath() {
        return shortestPath;
    }

    public void setShortestPath(String shortestPath) {
        this.shortestPath = shortestPath;
    }

    @Override
    public String toString() {
        return "\nNode{" +
                "id=" + id +
                ", ip='" + ip + '\'' +
                ", communicationPort=" + communicationPort +
                ", weight=" + weight +
                ", neighbours=" + neighbours +
                ", status=" + status +
                ", nodeThread=" + nodeThread +
                ", executorService=" + executorService +
                '}';
    }
}

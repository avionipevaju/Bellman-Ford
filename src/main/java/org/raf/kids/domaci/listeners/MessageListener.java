package org.raf.kids.domaci.listeners;

import org.raf.kids.domaci.utils.SocketUtils;
import org.raf.kids.domaci.vo.Message;
import org.raf.kids.domaci.vo.MessageType;
import org.raf.kids.domaci.vo.NodeStatus;
import org.raf.kids.domaci.workers.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class MessageListener implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(MessageListener.class);

    private Node node;

    public MessageListener(Node node) {
        this.node = node;
    }

    public void startListener() {
        Thread thread =  new Thread(this);
        thread.start();
    }

    public boolean checkMessage(List<Message> messages, Message message) {
        for(Message m: messages) {
            if (m.getuId() == message.getuId())
                return true;
        }
        return false;
    }

    @Override
    public void run() {
        ServerSocket nodeListenerSocket = null;
        Socket clientSocket = null;
        try {
            nodeListenerSocket = new ServerSocket(node.getCommunicationPort());
            while (true) {
                clientSocket = nodeListenerSocket.accept();
                Message received = SocketUtils.readMessage(clientSocket);
                MessageType type = received.getMessageType();
                switch (type) {
                    case UPDATE:
                        logger.info("Received message {}", received);
                        int length = node.getLength();
                        Node updateSenderNode = node.getNodeNeighbourById(received.getTraceId());
                        int checkValue = received.getContent() + updateSenderNode.getWeight();
                        if (length > checkValue) {
                            length = checkValue;
                            node.setLength(checkValue);
                            node.setParent(updateSenderNode);
                            node.broadcastMessage(new Message(node.getId(), MessageType.UPDATE, length));
                        }
                        break;
                    case CLOSEST_PATH:
                        if (node.getParent() == null) {
                            logger.info("ClosestPath: {}", received.getContent());
                            StringBuilder sb = new StringBuilder(String.valueOf(received.getContent()));
                            sb.reverse();
                            String path = node.getId() + "->";
                            for (int i = 0; i < sb.length(); i++){
                                if (i == sb.length() -1) {
                                    path = path.concat(String.valueOf(sb.charAt(i)));
                                }else{
                                    path = path.concat(sb.charAt(i) + "->");
                                }

                            }
                            node.setShortestPath(path);
                        }
                        int receivedInt = received.getContent();
                        String s = String.valueOf(receivedInt);
                        s = s.concat(String.valueOf(node.getId()));
                        received.setContent(Integer.valueOf(s));
                        node.sendMessage(node.getParent(), received);
                        break;
                    default:
                        logger.info("MADRFAKR");
                }
            }
        } catch (Exception e) {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            logger.error("Error starting message listener socket at port: {} ", node.getCommunicationPort(), e);
        } finally {
            logger.info("Closing message listener socket on Node {}", node.getId());
            try {
                if(clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}

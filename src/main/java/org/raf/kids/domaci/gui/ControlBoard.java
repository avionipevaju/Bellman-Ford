package org.raf.kids.domaci.gui;

import org.raf.kids.domaci.vo.NodeStatus;
import org.raf.kids.domaci.workers.Node;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;

public class ControlBoard extends JFrame implements Runnable {

    private Node node;
    private JButton startButton, showClosestPath;
    private JPanel centerPanel;
    private JLabel label, shortestPathLabel;
    private HashMap<Integer, JLabel> labelList;

    public ControlBoard(String title, Node node) throws HeadlessException, UnknownHostException {
        super(title);
        this.node = node;
        this.labelList = new HashMap<>();
        initFrame();
        initComponents();
    }

    private void initFrame() {
        setSize(450,180);
        setBackground(Color.white);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void initComponents() throws UnknownHostException {
        centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.setMaximumSize(new Dimension(200,100));
        centerPanel.setBackground(Color.gray);
        String name = String.format("Node %d, ip: %s port: %d", node.getId(), Inet4Address.getLocalHost().getHostAddress(), node.getCommunicationPort());
        JLabel nameLabel = new JLabel(name);
        centerPanel.add(nameLabel);
        startButton = new JButton("Start Node");
        startButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                node.activateNode();
                centerPanel.setBackground(Color.GREEN);
            }
        });
        centerPanel.add(startButton, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        showClosestPath = new JButton("Show Closest Path");
        showClosestPath.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                node.getClosestPath();
            }
        });
        centerPanel.add(showClosestPath, BorderLayout.SOUTH);

        JPanel neighbourPanel = new JPanel();
        neighbourPanel.setLayout(new GridLayout(10,1));
        neighbourPanel.setBackground(Color.white);
        neighbourPanel.add(new JLabel("Neighbours"));
        for(Node node: node.getNeighbours()) {
            String marker = String.format("Node %s, ip: %s port: %s weight: %s", node.getId(), node.getIp(), node.getCommunicationPort(), node.getWeight());
            JLabel neighbourLabel = new JLabel(marker);
            neighbourLabel.setForeground(Color.gray);
            labelList.put(node.getId(), neighbourLabel);
            neighbourPanel.add(neighbourLabel);

        }
        neighbourPanel.add(new JLabel("Bellman-Ford Data: "));
        String marker = String.format("Length %s, Parent: %s", node.getLength(), node.getParent());
        label = new JLabel(marker);
        label.setForeground(Color.blue);
        neighbourPanel.add(label);

        if(node.isSource()) {
            neighbourPanel.add(new JLabel("Closest Path:"));
            shortestPathLabel = new JLabel();
            neighbourPanel.add(shortestPathLabel);
        }

        add(neighbourPanel, BorderLayout.EAST);
    }

    @Override
    public void run() {
        List<Node> nodes = node.getNeighbours();
        while (true) {
            int id;
            if (node.getParent() == null) {
                id = 0;
            } else {
                id = node.getParent().getId();
            }
            String marker = String.format("Length %s, Parent: %s", node.getLength(), id);
            label.setText(marker);
            if (shortestPathLabel != null) {
                shortestPathLabel.setText(node.getShortestPath());
            }
            for (Node node: nodes) {
                NodeStatus nodeStatus = node.getStatus();
                JLabel label = labelList.get(node.getId());
                switch (nodeStatus) {
                    case NOT_STARTED:
                        label.setForeground(Color.gray);
                        break;
                    case ACTIVE:
                        label.setForeground(Color.green);
                        break;
                    case SUSPECTED_FAILURE:
                        label.setForeground(Color.yellow);
                        break;
                    case FAILED:
                        label.setForeground(Color.red);
                        break;
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}

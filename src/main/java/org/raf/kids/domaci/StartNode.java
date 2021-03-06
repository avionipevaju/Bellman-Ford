package org.raf.kids.domaci;

import org.raf.kids.domaci.gui.ControlBoard;
import org.raf.kids.domaci.utils.ConfigurationUtils;
import org.raf.kids.domaci.workers.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.IOException;
import java.util.Scanner;

public class StartNode {

    private static Logger logger = LoggerFactory.getLogger(StartNode.class);
    private static Node node;
    private static Scanner scanner;
    public static final int NODE_COUNT = 4;

    public static void main(String[] args) {
        loadNode();
    }

    private static void loadNode() {
        scanner = new Scanner(System.in);
        logger.info("Enter node configuration number");
        String configNumber = scanner.next();
        SwingUtilities.invokeLater(() -> {
            try {
                String configurationUrl = "src/main/resources/configuration"+configNumber+".json";
                node = ConfigurationUtils.loadNodeConfiguration(configurationUrl);
                logger.info("Loaded node: {} ", node);
                String title;
                if (node.isSource()) {
                    title = "SOURCE Node" + String.valueOf(node.getId());
                } else {
                    title = "Node " + String.valueOf(node.getId());
                }
                ControlBoard controlBoard =  new ControlBoard(title, node);
                Thread t = new Thread(controlBoard);
                t.start();
            } catch (IOException e) {
                logger.error("Error loading and running node: ", e.getMessage());
            }
        });
}
}

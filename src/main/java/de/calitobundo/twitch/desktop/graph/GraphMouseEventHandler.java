package de.calitobundo.twitch.desktop.graph;

import java.util.stream.Collectors;

import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class GraphMouseEventHandler implements EventHandler<MouseEvent> {

    public static GraphUser selectedUser = null;
    private GraphUser draggedUser = null;
    private final GraphHandler handler;

    public GraphMouseEventHandler(GraphHandler handler){
        this.handler = handler;
    }
    
    @Override
    public void handle(MouseEvent event) {

        if(event.getEventType() == MouseEvent.MOUSE_CLICKED){
            if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2){
                handler.openGraphInStage();
            }
        }

        if(event.getEventType() == MouseEvent.MOUSE_MOVED){
            selectedUser = GraphUtils.findUser(handler.getGraphUsers().stream().filter(g -> !g.removed).collect(Collectors.toList()), event.getX(), event.getY(), GraphCanvas.showAll);
            handler.setRotation(selectedUser == null);
        }

        if(event.getEventType() == MouseEvent.MOUSE_DRAGGED){
            if(draggedUser != null){
                double dx = event.getX() - handler.getCenter();
                double dy = event.getY() - handler.getCenter();                
                draggedUser.angle = Math.atan2(dy, dx);
            }
        }

        if(event.getEventType() == MouseEvent.MOUSE_PRESSED){
            draggedUser = selectedUser;
            if (selectedUser != null && event.getButton() == MouseButton.SECONDARY){
                selectedUser.removed = true;
                draggedUser = selectedUser = null;
                handler.onUserRemoved(selectedUser);
            }
        }

        if(event.getEventType() == MouseEvent.MOUSE_RELEASED){
            draggedUser = null;
        }
    }
    
}

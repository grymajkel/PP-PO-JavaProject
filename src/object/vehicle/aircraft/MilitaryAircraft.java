package object.vehicle.aircraft;

import component.RouteComponent;
import data.Database;
import component.TableCellComponent;
import javafx.collections.ObservableList;
import object.network.Airport;
import util.Utility;

import java.util.ArrayList;
import java.util.Arrays;

public final class MilitaryAircraft extends Aircraft {
    private final String weaponType;

    public MilitaryAircraft(String data) {
        super(data);
        Utility.JSONInfo.init(data);
        weaponType = (String) Utility.JSONInfo.get("weaponType");
    }

    public MilitaryAircraft(String data, double x, double y) {
        this(data);
        movementComponent.init(new ArrayList<>(Arrays.asList(x, y)));
        update();
        connectToClosestJunction();
    }

    private void connectToClosestJunction() {
        String closestJunction = null;
        double closestDist = Double.MAX_VALUE;
        for (String junction: Database.getJunctions()) {
            if (junction.startsWith("JUW")) continue;
            double dist = Utility.Math.dist(getGUI_X(), getGUI_Y(), Database.getAppObjects().get(junction).getGUI_X(), Database.getAppObjects().get(junction).getGUI_Y());
            if (dist < closestDist) {
                closestDist = dist;
                closestJunction = junction;
            }
        }
        for (String airport: Database.getAirports()) {
            double dist = Utility.Math.dist(getGUI_X(), getGUI_Y(), Database.getAppObjects().get(airport).getGUI_X(), Database.getAppObjects().get(airport).getGUI_Y());
            if (dist < closestDist) {
                closestDist = dist;
                closestJunction = airport;
            }
        }
        movementComponent.setDest(new ArrayList<>(Arrays.asList(Database.getAppObjects().get(closestJunction).getGUI_X(), getGUI_Y())));
        routeComponent.setTmpDest(closestJunction);
        routeComponent.setState(RouteComponent.State.CONNECTING_TO_TRAFFIC_X);
        System.out.println(closestJunction);
    }

    @Override
    protected void airportActions() {
        switch (airport_action) {
            case NONE, EMERGENCY, DEBOARDING, SET_PASS_NUM, BOARDING -> airport_action = AIRPORT_ACTION.REFUEL;
            case REFUEL -> {
                if (refuel(20d / fps)) airport_action = AIRPORT_ACTION.READY;
            }
            case READY -> {
                if (((Airport) Database.getAppObjects().get(routeComponent.getDest())).removeUsing(getId())) {
                    routeComponent.setState(RouteComponent.State.WAITING_TRACK);
                    airport_action = AIRPORT_ACTION.NONE;
                    movementComponent.setDest(routeComponent.setNewDest());
                    if (routeComponent.getDest() == null) generateNewRoute();
                } else
                    System.out.println("Removing from airport error");
            }
        }
        refuel(20d / fps);
    }

    @Override
    public String toString() {
        return  super.toString() +
                String.format("  weaponType: %s\n", weaponType);
    }

    public ObservableList<TableCellComponent> getObjectInfo() {
        ObservableList<TableCellComponent> objectInfos = super.getObjectInfo();
        objectInfos.add(new TableCellComponent("weaponType", weaponType));
        return objectInfos;
    }
}

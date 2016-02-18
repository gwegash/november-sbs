package uk.ac.cam.november.messages;

import java.util.LinkedList;

import uk.ac.cam.november.boot.ShutDown;
import uk.ac.cam.november.buttons.ButtonNames;
import uk.ac.cam.november.decoder.AlertMessage;
import uk.ac.cam.november.decoder.BoatState;
import uk.ac.cam.november.decoder.MessageDecoder;
import uk.ac.cam.november.location.LatLng;
import uk.ac.cam.november.location.LocationUtil;
import uk.ac.cam.november.location.Port;
import uk.ac.cam.november.packet.Packet;

/**
 * 
 * @author Alan Tang
 * 
 * The purpose of this class is to respond to determine which button was pressed,
 * poll the StateDecoder for data information
 * format a message corresponding to the button pressed,
 * and assign a priority to the message before sending it to the MessageHandler class
 *
 */
public class MessageFormatter {

    private static final int MESSAGE_PRIORITY = 1;
    private static final int ALERT_PRIORITY = 2;
    private static final int SHUT_DOWN_PRIORITY = 3;

    private static MessageDecoder mDecoder = null;

    public static void setDecoder(MessageDecoder decoder)
    {
        mDecoder = decoder;
    }

    // Prevents instantiation
    private MessageFormatter() {}

    /**
     * Gets data which corresponds with input button, formats it, and sends it to the Messagehandler
     * 
     * @param buttonName The name of the button that was pressed.
     */
    public static void handleButtonPress(String buttonName)
    {
        // If the shut down button has been pressed,
        // we will deal with it separately by turning the system off
        if (buttonName.compareTo(ButtonNames.SHUT_DOWN) == 0) {
            // Before shutting down, we will announce a shut down message loudly.
            Message shutDownMessage = new Message("Turning the system completely off", SHUT_DOWN_PRIORITY);
            MessageHandler.receiveMessage(shutDownMessage);

            // Excecuting an actual shut down operation;
            ShutDown.shutDown();
            return;
        }

        String formattedString = formatButtonPress(buttonName); 

        // assign priority and wrap in Message Object
        Message m = new Message(formattedString, MESSAGE_PRIORITY);

        // call MessageHandler

        System.out.println("Sending Message: '" + formattedString +"'");
        MessageHandler.receiveMessage(m);
    }
    
    private static String formatButtonPress(String buttonName)
    {
        String formattedString;
        
        switch (buttonName)
        {
        case ButtonNames.BOAT_SPEED:
            formattedString = formatBoatSpeedButton();
            break;
        case ButtonNames.COMPASS_HEADING:
            formattedString = formatCompassHeadingButton();
            break;
        case ButtonNames.NEAREST_PORT:
            formattedString = formatNearestPortButton();
            break;
        case ButtonNames.WATER_DEPTH:
            formattedString = formatWaterDepthButton();
            break;
        case ButtonNames.WIND_DIRECTION:
            formattedString = formatWindDirButton();
            break;
        case ButtonNames.WIND_SPEED:
            formattedString = formatWindSpeedButton();
            break;

        default: 
            // Should not reach here
            System.err.println("Invalid button name: " + buttonName);
            throw new IllegalArgumentException("Invalid button name: " + buttonName);

        }
        
        return formattedString;
    }

    private static String formatNearestPortButton() {
        BoatState state = mDecoder.getState();
        LatLng myLoc = new LatLng(state.getLatitude(), state.getLongtitude());
        Port p = LocationUtil.nearestPort(myLoc);
        double dist = LocationUtil.distance(myLoc, p.location);
        double bearing = LocationUtil.initialBearing(myLoc, p.location);
        String distString = formatDistance(dist);
        String bearingString = truncateFloat((float)bearing);
        return distString + " at " + bearingString + " degrees to " + p.name;
    }

    private static String formatWindSpeedButton() {
        float speed = mDecoder.getState().getWindSpeed();
        String strSpd = truncateFloat(speed);
        
        return strSpd + " meters per second";
    }

    private static String formatWindDirButton() {
        float dir = mDecoder.getState().getWindAngle();
        String strDir = String.format("%.0f", dir);
        
        return strDir + " degrees from head";
    }

    private static String formatWaterDepthButton() {
        float depth = mDecoder.getState().getDepth();
        String strDpth = truncateFloat(depth);
        
        return strDpth + " meters deep";
    }

    private static String formatBoatSpeedButton() {
        
        float speed = mDecoder.getState().getSpeedWaterReferenced();
        String strSpd = truncateFloat(speed);
        
        return strSpd + " meters per second";
    }
    
    private static String formatCompassHeadingButton() {
        float heading = mDecoder.getState().getHeading();
        String strHdg = String.format("%.0f", heading);
        
        return strHdg + " degrees from north";
    }

    public static void handleAlert(AlertMessage alert)
    {
        int type = alert.getAlertType();
        int sensor = alert.getSensor();

        String formattedString = "Warning: "; 
        String sensorName;

        switch (sensor)
        {
        case 0:
            sensorName = "Water Depth";
            break;
        case 1:
            sensorName = "Wind Speed";
            break;
        case 2:
            sensorName = "Wind Angle";
            break;
        case 3:
            sensorName = "Boat Heading";
            break;
        case 4:
            sensorName = "Boat Speed";
            break;

        default:
            // Should not reach here:
            System.err.println("Invalid alert sensor type: " + sensor);
            throw new IllegalArgumentException("Invalid alert sensor type: " + sensor);
        }

        switch (type)
        {
        case 0: //Critical Change
            formattedString += "rapid change in " + sensorName;
            break;
        case 1: //Critical Max
            formattedString += sensorName + " is high";
            break;
        case 2: //Critical Min
            formattedString += sensorName + " is low";
            break;
        case 3: //Timeout 
            formattedString += sensorName + " is unresponsive";
            break;

        default:
            // Should not reach here:
            System.err.println("Invalid alert type: " + type);
            throw new IllegalArgumentException("Invalid alert type: " + type);
        }

        // assign priority and wrap in Message Object
        Message m = new Message(formattedString, ALERT_PRIORITY);

        // call MessageHandler
        System.out.println("Sending Alert Message: '" + formattedString +"'");
        MessageHandler.receiveMessage(m);
    }

    private static String formatDistance(double distance){
        String unit = "m";
        if(distance > 1000){
            distance /= 1000;
            unit = "km";
        }
        return truncateFloat((float)distance) + unit;
    }

    private static String truncateFloat(float v)
    {
        int l = 1; 

        if( v > 10 || (v - Math.floor(v) < 0.1) )
        {
            l = 0; 
        }
        String data = String.format("%."+ l  +"f", v);
        return data;
    }

/*
    private static float pollStateDecoder(String buttonName)
    {
        if(mDecoder == null)
        {
            System.err.println("MessageDecoder not set");
            throw new NullPointerException();
        }

        BoatState state = mDecoder.getState();
        switch(buttonName)
        {
        case ButtonNames.BOAT_SPEED:
            return state.getSpeedWaterReferenced();
        case ButtonNames.COMPASS_HEADING:
            return state.getHeading();
        case ButtonNames.NEAREST_PORT:
            return 0f; // not used
        case ButtonNames.WATER_DEPTH:
            return state.getDepth();
        case ButtonNames.WIND_DIRECTION:
            return state.getWindAngle();
        case ButtonNames.WIND_SPEED:
            return state.getWindSpeed();

        default:
            // Should not reach here
            System.err.println("Invalid button name: " + buttonName);
            throw new IllegalArgumentException("Invalid button name: " + buttonName);
        }
    }
 */
    /*
     * Given a data value and the field that it corresponds with, this creates a formatted string
     * that can be read by the MessageHandler
     * 
     *
    private static String formatMessage(float dataValue, String buttonName)
    {
        String data = truncateFloat(dataValue, buttonName);

        switch(buttonName)
        {
        case ButtonNames.NEAREST_PORT:
            BoatState state = mDecoder.getState();
            LatLng myLoc = new LatLng(state.getLatitude(), state.getLongtitude());
            Port p = LocationUtil.nearestPort(myLoc);
            double dist = LocationUtil.distance(myLoc, p.location);
            double bearing = LocationUtil.initialBearing(myLoc, p.location);
            String distString = formatDistance(dist);
            String bearingString = truncateFloat((float)bearing, buttonName);
            return distString + " at " + bearingString + " degrees to " + p.name;
        case ButtonNames.WATER_DEPTH:
            return data + " meters deep";
        case ButtonNames.WIND_SPEED:
            return data + " meters per second";
        case ButtonNames.WIND_DIRECTION:
            return data + " degrees from head";
        case ButtonNames.COMPASS_HEADING:
            return data + " degrees from north";
        case ButtonNames.BOAT_SPEED:
            return data + " meters per second";

        default:
            // Should not reach here
            System.err.println("Formatting error: " + data + " " + buttonName);
            throw new IllegalArgumentException("Formatting error: " + data + " " + buttonName);
        }

    } */

    public static void main(String args[])
    {
        mDecoder = new MessageDecoder(new LinkedList<Packet>());
        BoatState state = mDecoder.getState();
        
        for(int i = 0; i < 20; i ++)
        {
           
            state.setDepth((float)Math.random() * 100);
            state.setHeading((float)Math.random() * 360);
            state.setWindAngle((float)Math.random() * 360);
            state.setWindSpeed((float)Math.random() * 20);
            state.setSpeedWaterReferenced((float)Math.random() * 30);
            state.setLatitude((float) (Math.random() * 90));
            state.setLongtitude((float) (Math.random() * 180));
            int r = (int)(Math.random() * 6);
            
            String buttonName = "";
            switch(r)
            {
            case 0:
                buttonName = ButtonNames.BOAT_SPEED;
                break;
            case 1:
                buttonName = ButtonNames.COMPASS_HEADING;
                break;
            case 2:
                buttonName = ButtonNames.NEAREST_PORT;
                break;
            case 3:
                buttonName = ButtonNames.WATER_DEPTH;
                break;
            case 4:
                buttonName = ButtonNames.WIND_DIRECTION;
                break;
            case 5: 
                buttonName = ButtonNames.WIND_SPEED;
                break;
                
            }
            
            System.out.println(formatButtonPress(buttonName));
        }
    }

}

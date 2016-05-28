package at.se2.gruppe3.menschrgeredichnicht;

import java.util.ArrayList;

/**
 * Created by chris on 25.05.2016.
 */
public class Connection{

    private static Connection _instance;

    private ArrayList<Device> deviceList;

    private ArrayList<String> clientListID = new ArrayList<String>();
    private boolean isHost;

    public Connection(){
    }

    public void clear(){
        if(deviceList!=null){
            deviceList.clear();
        }
    }

    public static Connection getInstance()
    {
        if (_instance == null)
        {
            _instance = new Connection();
        }
        return _instance;
    }

    public ArrayList<Device> getDeviceList() {
        return deviceList;
    }

    public void setDeviceList(ArrayList<Device> deviceList) {
        clear();
        if(this.deviceList==null){
            this.deviceList = new ArrayList<>();
        }
        for(Device device : deviceList){
            this.deviceList.add(device);
        }

    }

    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean host) {
        isHost = host;
    }
}

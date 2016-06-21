package at.se2.gruppe3.menschrgeredichnicht;

/**
 * Created by chris on 24.05.2016.
 */
public class Device {

    private String endpointId;
    private String deviceId;
    private String endpointName;

    public Device(String endpointId,String deviceId,String endpointName){
        this.endpointId = endpointId;
        this.deviceId = deviceId;
        this.endpointName = endpointName;
    }

    public String getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getEndpointName() {
        return endpointName;
    }

    public void setEndpointName(String endpointName) {
        this.endpointName = endpointName;
    }

}
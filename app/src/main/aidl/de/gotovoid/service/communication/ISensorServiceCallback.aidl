// ISensorServiceCallback.aidl
package de.gotovoid.service.communication;

// Declare any non-default types here with import statements
import de.gotovoid.service.communication.Response;

interface ISensorServiceCallback {
    void onSensorValueChanged(in Response response);
}

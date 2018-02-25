// ISensorService.aidl
package de.gotovoid.service.communication;

// Declare any non-default types here with import statements
import de.gotovoid.service.communication.CallbackRegistration;
import de.gotovoid.service.communication.ISensorServiceCallback;

oneway interface ISensorService {
    void setUpdatePaused(in boolean isUpdatePaused);
    void startSensor(in CallbackRegistration registration, in ISensorServiceCallback callback);
    void stopSensor(in CallbackRegistration registration);
    void requestUpdate(in CallbackRegistration registration);
    void startRecording(in long recordingId);
    void stopRecording();
}

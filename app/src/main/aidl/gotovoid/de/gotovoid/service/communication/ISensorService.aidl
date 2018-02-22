// ISensorService.aidl
package gotovoid.de.gotovoid.service.communication;

// Declare any non-default types here with import statements
import gotovoid.de.gotovoid.service.communication.CallbackRegistration;
import gotovoid.de.gotovoid.service.communication.ISensorServiceCallback;

oneway interface ISensorService {
    void setUpdatePaused(in boolean isUpdatePaused);
    void startSensor(in CallbackRegistration registration, in ISensorServiceCallback callback);
    void stopSensor(in CallbackRegistration registration);
    void requestUpdate(in CallbackRegistration registration);
    void startRecording(in long recordingId);
    void stopRecording();
}

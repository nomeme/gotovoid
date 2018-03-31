package de.gotovoid.service.communication;

import android.content.Context;
import android.content.Intent;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import de.gotovoid.service.sensors.SensorType;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * Created by DJ on 04/03/18.
 */

/**
 * Verifies the functionality of the {@link SensorServiceMessenger}.
 */
public class SensorServiceMessengerTest {
    private static final long RECORDING_ID = 12345;
    private SensorServiceMessenger mMenssenger;
    private SensorServiceMessenger.SensorServiceConnection mConnection;
    private ISensorService mSensorService;
    private Context mContext;

    /**
     * Prepare the test cases.
     */
    @Before
    public void before() {
        mConnection = Mockito.mock(SensorServiceMessenger.SensorServiceConnection.class);
        mContext = Mockito.mock(Context.class);
        mSensorService = Mockito.mock(ISensorService.class);
        mMenssenger = new SensorServiceMessenger(mConnection);
    }

    /**
     * Verify that {@link SensorServiceMessenger#isBound()} returns the correct value.
     */
    @Test
    public void testBoundInitially() {
        assertThat(mMenssenger.isBound(), is(false));
    }

    /**
     * Verify that {@link SensorServiceMessenger#doBind(Context)} ()} works as expected.
     */
    @Test
    public void testDoBind() {
        Mockito.when(mContext.bindService(Mockito.any(Intent.class),
                Mockito.any(SensorServiceMessenger.SensorServiceConnection.class),
                Mockito.anyInt())).thenReturn(false).thenReturn(true);
        mMenssenger.doBind(mContext);
        assertThat(mMenssenger.isBound(), is(false));
        mMenssenger.doBind(mContext);
        assertThat(mMenssenger.isBound(), is(true));
    }

    /**
     * Verify that the {@link SensorServiceMessenger#doUnbind(Context)} works as expected.
     */
    @Test
    public void testDoUnbind() {
        Mockito.when(mContext.bindService(Mockito.any(Intent.class),
                Mockito.any(SensorServiceMessenger.SensorServiceConnection.class),
                Mockito.anyInt())).thenReturn(true);
        mMenssenger.doBind(mContext);
        assertThat(mMenssenger.isBound(), is(true));
        mMenssenger.doUnbind(mContext);
        assertThat(mMenssenger.isBound(), is(false));
    }

    /**
     * Verify that the
     * {@link SensorServiceMessenger#start(CallbackRegistration, ISensorServiceCallback)}
     * method works as expected when bound.
     */
    @Test
    public void testStart() {
        final CallbackRegistration registration = Mockito.mock(CallbackRegistration.class);
        final ISensorServiceCallback callback = Mockito.mock(ISensorServiceCallback.class);
        Mockito.when(mConnection.getService()).thenReturn(mSensorService);
        assertThat(mMenssenger.start(registration, callback), is(true));
    }

    /**
     * Verify that the
     * {@link SensorServiceMessenger#start(CallbackRegistration, ISensorServiceCallback)}
     * handles the unbound state gracefully.
     */
    @Test
    public void testStartUnbound() {
        final CallbackRegistration registration = Mockito.mock(CallbackRegistration.class);
        Mockito.when(registration.getType()).thenReturn(SensorType.PRESSURE);
        final ISensorServiceCallback callback = Mockito.mock(ISensorServiceCallback.class);
        assertThat(mMenssenger.start(registration, callback), is(false));
    }

    /**
     * Verify that the {@link SensorServiceMessenger#stop(CallbackRegistration)} works
     * as expected.
     */
    @Test
    public void testStop() {
        final CallbackRegistration registration = Mockito.mock(CallbackRegistration.class);
        Mockito.when(mConnection.getService()).thenReturn(mSensorService);
        assertThat(mMenssenger.stop(registration), is(true));
    }

    /**
     * Verify that the {@link SensorServiceMessenger#stop(CallbackRegistration)} method handles
     * the unbound state gracefully.
     */
    @Test
    public void testStopUnbound() {
        final CallbackRegistration registration = Mockito.mock(CallbackRegistration.class);
        assertThat(mMenssenger.stop(registration), is(false));
    }

    /**
     * Verify that the {@link SensorServiceMessenger#startRecording(long)} method works as
     * expected.
     */
    @Test
    public void testStartRecording() {
        Mockito.when(mConnection.getService()).thenReturn(mSensorService);
        assertThat(mMenssenger.startRecording(RECORDING_ID), is(true));
    }

    /**
     * Verify that the {@link SensorServiceMessenger#startRecording(long)}
     */
    @Test
    public void testStartRecordingUnbound() {
        assertThat(mMenssenger.startRecording(RECORDING_ID), is(false));
    }

    /**
     * Verify that stopping the recording works as expected.
     */
    @Test
    public void testStopRecording() {
        Mockito.when(mConnection.getService()).thenReturn(mSensorService);
        assertThat(mMenssenger.stopRecording(), is(true));
    }

    /**
     * Verify that stopping the recording returns false when unbound.
     */
    @Test
    public void testStopRecordingUnbound() {
        assertThat(mMenssenger.stopRecording(), is(false));
    }

    /**
     * Verify that {@link SensorServiceMessenger#setUpdatesEnabled(boolean)} works as expected.
     */
    @Test
    public void testSetUpdateEnabled() {
        Mockito.when(mConnection.getService()).thenReturn(mSensorService);
        assertThat(mMenssenger.setUpdatesEnabled(true), is(true));
        assertThat(mMenssenger.setUpdatesEnabled(false), is(true));
    }

    /**
     * Verify that {@link SensorServiceMessenger#setUpdatesEnabled(boolean)} returns false
     * when unbound.
     */
    @Test
    public void testSetUpdateEnabledUnbound() {
        assertThat(mMenssenger.setUpdatesEnabled(true), is(false));
        assertThat(mMenssenger.setUpdatesEnabled(false), is(false));
    }

}

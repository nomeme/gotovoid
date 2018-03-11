package de.gotovoid.service.communication;


import android.os.Parcel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.gotovoid.service.sensors.SensorType;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
/**
 * Created by DJ on 04/03/18.
 */

/**
 * Test to verify the functionality of the {@link CallbackRegistration}.
 */
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Parameterized.class)
@PrepareForTest({Parcel.class})
public class CallbackRegistrationTest {

    @Parameter(value = 0)
    public SensorType mSensorType;

    @Parameter(value = 1)
    public long mUpdateFrequency;

    @Parameter(value = 2)
    public ISensorServiceCallback mCallback;

    @Before
    public void before() {
        PowerMockito.mockStatic(Parcel.class);
    }

    @Parameters(name = "type: {0}, freq: {1}, callback: {2}")
    public static Collection<Object[]> initParameters() {
        Long[] updateFrequencies = {0l, 10l, 100l, 1000l, 10000l, 100000l};
        ISensorServiceCallback[] callbacks = {Mockito.mock(ISensorServiceCallback.class),
                Mockito.mock(ISensorServiceCallback.class),
                Mockito.mock(ISensorServiceCallback.class)};
        List<Object[]> list = new ArrayList<>();
        for (final SensorType type : SensorType.values()) {
            for (final long updateFrequency : updateFrequencies) {
                for (final ISensorServiceCallback calback : callbacks) {
                    list.add(new Object[]{type, updateFrequency, calback});
                }
            }
        }
        return list;
    }

    @Test
    public void verifyDataSet() {
        final CallbackRegistration registration = new CallbackRegistration(mSensorType,
                mCallback,
                mUpdateFrequency);
        assertThat(registration.getType(), is(mSensorType));
        assertThat(registration.getUpdateFrequency(), is(mUpdateFrequency));
        assertThat(registration.getCallbackId(), is(System.identityHashCode(mCallback)));
    }

    @Test
    public void testWriteToParcel() {
        final CallbackRegistration registration = new CallbackRegistration(mSensorType,
                mCallback,
                mUpdateFrequency);
        final Parcel parcel = Mockito.mock(Parcel.class);
        registration.writeToParcel(parcel, 0);
        final CallbackRegistration result = CallbackRegistration.CREATOR.createFromParcel(parcel);
        Mockito.verify(parcel, Mockito.times(1))
                .writeInt(mSensorType.ordinal());
        Mockito.verify(parcel, Mockito.times(1))
                .writeInt(System.identityHashCode(mCallback));
        Mockito.verify(parcel, Mockito.times(1))
                .writeLong(mUpdateFrequency);
    }

    @Test
    public void testCreateFromParcel() {
        final Parcel parcel = Mockito.mock(Parcel.class);
        Mockito.when(parcel.readInt())
                .thenReturn(mSensorType.ordinal())
                .thenReturn(System.identityHashCode(mCallback));
        Mockito.when(parcel.readLong()).thenReturn(mUpdateFrequency);
        final CallbackRegistration result =
                CallbackRegistration.CREATOR.createFromParcel(parcel);
        assertThat(result.getType(), is(mSensorType));
        assertThat(result.getCallbackId(), is(System.identityHashCode(mCallback)));
        assertThat(result.getUpdateFrequency(), is(mUpdateFrequency));
    }
}

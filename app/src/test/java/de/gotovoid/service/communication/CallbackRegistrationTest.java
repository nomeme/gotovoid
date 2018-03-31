package de.gotovoid.service.communication;


import android.os.Parcel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.gotovoid.BuildConfig;
import de.gotovoid.service.sensors.SensorType;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
/**
 * Created by DJ on 04/03/18.
 */

/**
 * Test to verify the functionality of the {@link CallbackRegistration}.
 */
@RunWith(ParameterizedRobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class CallbackRegistrationTest {

    public SensorType mSensorType;

    public long mUpdateFrequency;

    public ISensorServiceCallback mCallback;

    /**
     * Constructor.
     *
     * @param type            the {@link SensorType}
     * @param updateFrequency the update frequency
     */
    public CallbackRegistrationTest(final SensorType type, final long updateFrequency) {
        mSensorType = type;
        mUpdateFrequency = updateFrequency;
    }

    @Before
    public void before() {
        mCallback = Mockito.mock(ISensorServiceCallback.class);
    }

    /**
     * Prepare the parameters
     *
     * @return the parameters
     */
    @ParameterizedRobolectricTestRunner.Parameters(name = "type: {0}, freq: {1}, callback: {2}")
    public static Collection<Object[]> initParameters() {
        Long[] updateFrequencies = {0l, 10l, 100l, 1000l, 10000l, 100000l};

        List<Object[]> list = new ArrayList<>();
        for (final SensorType type : SensorType.values()) {
            for (final long updateFrequency : updateFrequencies) {
                list.add(new Object[]{type, updateFrequency});
            }
        }
        return list;
    }

    /**
     * Verifies the data set.
     */
    @Test
    public void verifyDataSet() {
        final CallbackRegistration registration = new CallbackRegistration(mSensorType,
                mCallback,
                mUpdateFrequency);
        assertThat(registration.getType(), is(mSensorType));
        assertThat(registration.getUpdateFrequency(), is(mUpdateFrequency));
        assertThat(registration.getCallbackId(), is(System.identityHashCode(mCallback)));
    }

    /**
     * Verifies the id algorithm for equality.
     */
    @Test
    public void verifyEqualRegistration() {
        final CallbackRegistration registration1 = new CallbackRegistration(mSensorType,
                mCallback,
                mUpdateFrequency);
        final CallbackRegistration registration2 = new CallbackRegistration(mSensorType,
                mCallback,
                mUpdateFrequency);
        assertThat(registration1.getCallbackId(), is(registration2.getCallbackId()));
    }

    /**
     * Verifies the id algorithm for inequality.
     */
    @Test
    public void verifyUnequalRegistration() {
        final CallbackRegistration registration1 = new CallbackRegistration(mSensorType,
                Mockito.mock(ISensorServiceCallback.class),
                mUpdateFrequency);
        final CallbackRegistration registration2 = new CallbackRegistration(mSensorType,
                Mockito.mock(ISensorServiceCallback.class),
                mUpdateFrequency);
        assertThat(registration1.getCallbackId(), not(registration2.getCallbackId()));
    }

    /**
     * Verify that writing to a {@link Parcel} works as expected.
     */
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

    /**
     * Verifies that creation from a {@link Parcel} works as expected.
     */
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

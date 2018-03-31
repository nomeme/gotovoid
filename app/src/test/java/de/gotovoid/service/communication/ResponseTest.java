package de.gotovoid.service.communication;

import android.os.Parcel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized.Parameter;
import org.mockito.Mockito;
import org.robolectric.ParameterizedRobolectricTestRunner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.gotovoid.domain.model.geodata.ExtendedGeoCoordinate;
import de.gotovoid.service.sensors.AbstractSensor;
import de.gotovoid.service.sensors.SensorState;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * Created by DJ on 04/03/18.
 */

/**
 * Test for the {@link Response} class.
 */
@RunWith(ParameterizedRobolectricTestRunner.class)
public class ResponseTest {
    private static final double LNG = 12.56;
    private static final double LAT = 9.45;
    private static final double ALT = 555;
    private static final float ACC = 33;

    public Serializable mData;

    /**
     * Constructor.
     *
     * @param data the data to be serialized
     */
    public ResponseTest(final Serializable data) {
        mData = data;
    }

    /**
     * Prepare the parameters for this test.
     *
     * @return parameters
     */
    @ParameterizedRobolectricTestRunner.Parameters(name = "data: {0}")
    public static Collection<Object[]> initParameters() {
        List<Object[]> parameters = new ArrayList<>();
        Serializable[] string = new Serializable[]{"test"};
        Serializable[] intVal = new Serializable[]{1};
        Serializable[] longVal = new Serializable[]{1l};
        Serializable[] geoCoord = new Serializable[]{
                new ExtendedGeoCoordinate(LNG, LAT, ALT, ACC)};
        parameters.add(string);
        parameters.add(intVal);
        parameters.add(longVal);
        parameters.add(geoCoord);
        return parameters;
    }

    /**
     * Prepare the test run
     */
    @Before
    public void before() {
    }

    /**
     * Verify that writing to a {@link Parcel} works as expected.
     */
    @Test
    public void testWriteToParcel() {
        final Parcel parcel = Mockito.mock(Parcel.class);
        final AbstractSensor.Result result = new AbstractSensor.Result(
                SensorState.RUNNING,
                mData);
        final Response response = new Response(result);
        response.writeToParcel(parcel, 0);
        Mockito.verify(parcel, Mockito.times(1)).writeSerializable(result);
    }

    /**
     * Verify that creation from a {@link Parcel} works as expected.
     */
    @Test
    public void testCreateFromParcel() {
        final Parcel parcel = Mockito.mock(Parcel.class);
        final AbstractSensor.Result result = new AbstractSensor.Result(
                SensorState.RUNNING,
                mData);
        Mockito.when(parcel.readSerializable()).thenReturn(result);
        final Response response = Response.CREATOR.createFromParcel(parcel);
        assertThat(result.getValue(), is(mData));
    }
}

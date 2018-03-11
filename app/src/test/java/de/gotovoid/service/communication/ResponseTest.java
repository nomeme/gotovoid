package de.gotovoid.service.communication;

import android.os.Parcel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized.Parameter;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.gotovoid.domain.model.geodata.ExtendedGeoCoordinate;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * Created by DJ on 04/03/18.
 */
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Parameterized.class)
@PrepareForTest({Parcel.class})
public class ResponseTest {
    private static final double LNG = 12.56;
    private static final double LAT = 9.45;
    private static final double ALT = 555;
    private static final float ACC = 33;

    @Parameter
    public Serializable mData;

    @Parameters(name = "data: {0}")
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

    @Before
    public void before() {
        PowerMockito.mockStatic(Parcel.class);
    }

    @Test
    public void testWriteToParcel() {
        final Parcel parcel = Mockito.mock(Parcel.class);
        final Response response = new Response(mData);
        response.writeToParcel(parcel, 0);
        Mockito.verify(parcel, Mockito.times(1)).writeSerializable(mData);
    }

    public void testCreateFromParcel() {
        final Parcel parcel = Mockito.mock(Parcel.class);
        Mockito.when(parcel.readSerializable()).thenReturn(mData);
        final Response result = Response.CREATOR.createFromParcel(parcel);
        assertThat(result.getValue(), is(mData));
    }
}

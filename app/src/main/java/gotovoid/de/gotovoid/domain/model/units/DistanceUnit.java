package gotovoid.de.gotovoid.domain.model.units;


/**
 * {@link IUnit} implementation for distance.
 * <p>
 * Created by DJ on 05/01/18.
 */
public enum DistanceUnit implements IUnit<DistanceUnit> {
    // TODO: use string resource
    METERS(1d, "m"),
    KILOMETERS(1000d, "km");
    private static final String TAG = DistanceUnit.class.getSimpleName();

    final String mShortName;
    final double mConversionFactor;

    /**
     * Constructor taking the short name of the unit and the conversion factor.
     * The conversion factor defines teh value by which the the unit has to be multiplied
     * to be converted into {@link #getBase()}.
     *
     * @param conversionFactor the conversion factor
     * @param shortName        short name of the unit.
     */
    DistanceUnit(final double conversionFactor, final String shortName) {
        mShortName = shortName;
        mConversionFactor = conversionFactor;
    }

    @Override
    public DistanceUnit getBase() {
        return METERS;
    }

    @Override
    public String getShortName() {
        return mShortName;
    }

    @Override
    public double getBase(final double value) {
        return value * mConversionFactor;
    }

    @Override
    public UnitValue<DistanceUnit> convert(final UnitValue<DistanceUnit> unitValue) {
        final double baseValue = unitValue.getUnit().getBase(unitValue.getValue());
        final double result = baseValue / mConversionFactor;
        return new UnitValue<>(result, this);
    }
}

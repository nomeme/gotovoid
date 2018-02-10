package gotovoid.de.gotovoid.domain.model.units;

/**
 * Created by DJ on 05/01/18.
 */

public class UnitValue<Type extends IUnit<Type>> {
    private final double mValue;
    private final Type mUnit;

    public UnitValue(final double value, final Type unit) {
        mValue = value;
        mUnit = unit;
    }

    public double getValue() {
        return mValue;
    }

    public Type getUnit() {
        return mUnit;
    }

    public UnitValue<Type> getBase() {
        return new UnitValue<>(mUnit.getBase(mValue), mUnit.getBase());
    }

    public String getShortString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(mValue);
        builder.append(mUnit.getShortName());
        return builder.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getClass().getSimpleName());
        builder.append("{value: ");
        builder.append(mValue);
        builder.append(", unit: ");
        builder.append(mUnit);
        builder.append('}');
        return builder.toString();
    }
}

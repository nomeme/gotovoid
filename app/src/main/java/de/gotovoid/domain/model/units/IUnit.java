package de.gotovoid.domain.model.units;

/**
 * Created by DJ on 05/01/18.
 */

public interface IUnit<Unit extends IUnit<Unit>> {
    String getShortName();

    /**
     * Converts the given value in the given unit to this unit.
     *
     * @return converted value in this {@link IUnit}
     */
    UnitValue<Unit> convert(final UnitValue<Unit> unitValue);

    Unit getBase();

    double getBase(final double value);
}


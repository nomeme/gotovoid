package gotovoid.de.gotovoid.repository;

import gotovoid.de.gotovoid.service.repository.LocationRepository;

/**
 * Created by DJ on 10/02/18.
 */

public interface IRepositoryProvider {
    LocationRepository getLocationRepository();
}

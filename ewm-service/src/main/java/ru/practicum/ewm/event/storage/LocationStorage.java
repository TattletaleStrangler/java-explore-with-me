package ru.practicum.ewm.event.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.event.model.Location;

import java.util.Optional;

public interface LocationStorage extends JpaRepository<Location, Long> {

    Optional<Location> findByLatAndLon(Float lat, Float lon);

}

package ru.practicum.ewm.event.model;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Builder
@Entity
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name = "location", schema = "public")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Float lat;

    @Column
    private Float lon;
}

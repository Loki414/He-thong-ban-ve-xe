package com.example.busticket.repository;

import com.example.busticket.entity.Trip;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {

    @Override
    @EntityGraph(attributePaths = {"bus", "route"})
    List<Trip> findAll();

    @EntityGraph(attributePaths = {"bus", "route"})
    List<Trip> findByRouteOriginIgnoreCaseContainingAndRouteDestinationIgnoreCaseContaining(String origin, String destination);
}


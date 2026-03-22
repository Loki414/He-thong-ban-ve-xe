package com.example.busticket.service;

import com.example.busticket.dto.RouteRequest;
import com.example.busticket.dto.RouteResponse;
import com.example.busticket.entity.Route;
import com.example.busticket.repository.RouteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RouteService {

    private final RouteRepository routeRepository;
    private final MapperService mapperService;

    public RouteService(RouteRepository routeRepository, MapperService mapperService) {
        this.routeRepository = routeRepository;
        this.mapperService = mapperService;
    }

    public List<RouteResponse> getAll() {
        return routeRepository.findAll().stream().map(mapperService::toRouteResponse).toList();
    }

    public RouteResponse create(RouteRequest request) {
        Route route = new Route();
        route.setOrigin(request.origin());
        route.setDestination(request.destination());
        route.setDistance(request.distance());
        return mapperService.toRouteResponse(routeRepository.save(route));
    }
}


package com.example.busticket.service;

import com.example.busticket.dto.BusRequest;
import com.example.busticket.dto.BusResponse;
import com.example.busticket.entity.Bus;
import com.example.busticket.repository.BusRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class BusService {

    private final BusRepository busRepository;
    private final MapperService mapperService;

    public BusService(BusRepository busRepository, MapperService mapperService) {
        this.busRepository = busRepository;
        this.mapperService = mapperService;
    }

    public List<BusResponse> getAll() {
        return busRepository.findAll().stream().map(mapperService::toBusResponse).toList();
    }

    public BusResponse create(BusRequest request) {
        Bus bus = new Bus();
        bus.setBusNumber(request.busNumber());
        bus.setBusType(request.busType());
        bus.setTotalSeats(request.totalSeats());
        return mapperService.toBusResponse(busRepository.save(bus));
    }

    public BusResponse update(Long id, BusRequest request) {
        Bus bus = busRepository.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Bus not found"));
        bus.setBusNumber(request.busNumber());
        bus.setBusType(request.busType());
        bus.setTotalSeats(request.totalSeats());
        return mapperService.toBusResponse(busRepository.save(bus));
    }

    public void delete(Long id) {
        if (!busRepository.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, "Bus not found");
        }
        busRepository.deleteById(id);
    }
}


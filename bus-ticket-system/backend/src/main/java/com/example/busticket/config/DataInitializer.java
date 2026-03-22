package com.example.busticket.config;

import com.example.busticket.entity.*;
import com.example.busticket.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(UserRepository userRepository,
                               BusRepository busRepository,
                               RouteRepository routeRepository,
                               TripRepository tripRepository,
                               SeatRepository seatRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@busbooking.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(UserRole.ROLE_ADMIN);

                User user = new User();
                user.setUsername("customer");
                user.setEmail("customer@busbooking.com");
                user.setPassword(passwordEncoder.encode("123456"));
                user.setRole(UserRole.ROLE_USER);

                userRepository.saveAll(List.of(admin, user));
            }

            if (busRepository.count() == 0) {
                Bus bus1 = new Bus();
                bus1.setBusNumber("FUTA-01");
                bus1.setBusType("Limousine");
                bus1.setTotalSeats(20);

                Bus bus2 = new Bus();
                bus2.setBusNumber("VEX-02");
                bus2.setBusType("Sleeper");
                bus2.setTotalSeats(24);

                busRepository.saveAll(List.of(bus1, bus2));
            }

            if (routeRepository.count() == 0) {
                Route route1 = new Route();
                route1.setOrigin("TP. HCM");
                route1.setDestination("Da Lat");
                route1.setDistance(310.0);

                Route route2 = new Route();
                route2.setOrigin("TP. HCM");
                route2.setDestination("Nha Trang");
                route2.setDistance(430.0);

                Route route3 = new Route();
                route3.setOrigin("Da Nang");
                route3.setDestination("Hue");
                route3.setDistance(95.0);

                routeRepository.saveAll(List.of(route1, route2, route3));
            }

            if (tripRepository.count() == 0) {
                List<Bus> buses = busRepository.findAll();
                List<Route> routes = routeRepository.findAll();

                Trip trip1 = new Trip();
                trip1.setBus(buses.get(0));
                trip1.setRoute(routes.get(0));
                trip1.setDepartureTime(LocalDateTime.now().plusDays(1).withHour(8).withMinute(0));
                trip1.setPrice(new BigDecimal("300000"));

                Trip trip2 = new Trip();
                trip2.setBus(buses.get(1));
                trip2.setRoute(routes.get(1));
                trip2.setDepartureTime(LocalDateTime.now().plusDays(1).withHour(21).withMinute(30));
                trip2.setPrice(new BigDecimal("350000"));

                Trip trip3 = new Trip();
                trip3.setBus(buses.get(0));
                trip3.setRoute(routes.get(2));
                trip3.setDepartureTime(LocalDateTime.now().plusDays(2).withHour(9).withMinute(15));
                trip3.setPrice(new BigDecimal("180000"));

                List<Trip> trips = tripRepository.saveAll(List.of(trip1, trip2, trip3));

                List<Seat> seats = new ArrayList<>();
                for (Trip trip : trips) {
                    for (int i = 1; i <= trip.getBus().getTotalSeats(); i++) {
                        Seat seat = new Seat();
                        seat.setTrip(trip);
                        seat.setSeatNumber(String.format("A%02d", i));
                        seat.setBooked(false);
                        seats.add(seat);
                    }
                }
                seatRepository.saveAll(seats);
            }
        };
    }
}


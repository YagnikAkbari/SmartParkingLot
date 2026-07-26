package com.example.smartparkinglot.integration;

import com.example.smartparkinglot.dto.CheckInRequest;
import com.example.smartparkinglot.dto.CheckOutRequest;
import com.example.smartparkinglot.dto.SpotCreationRequest;
import com.example.smartparkinglot.model.SpotSize;
import com.example.smartparkinglot.model.VehicleType;
import com.example.smartparkinglot.repository.ParkingSpotRepository;
import com.example.smartparkinglot.repository.ParkingTicketRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ParkingFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ParkingSpotRepository spotRepository;

    @Autowired
    private ParkingTicketRepository ticketRepository;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
        spotRepository.deleteAll();
    }

    // ==================== Admin Spot Management ====================

    @Test
    @DisplayName("POST /api/admin/spots - Should create a parking spot")
    void shouldCreateSpot() throws Exception {
        SpotCreationRequest request = SpotCreationRequest.builder()
                .spotNumber("SPOT-A01")
                .floor(1)
                .spotSize(SpotSize.MEDIUM)
                .build();

        mockMvc.perform(post("/api/admin/spots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.spotNumber").value("SPOT-A01"))
                .andExpect(jsonPath("$.floor").value(1))
                .andExpect(jsonPath("$.spotSize").value("MEDIUM"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    @DisplayName("POST /api/admin/spots - Should reject invalid request body")
    void shouldRejectInvalidSpotCreation() throws Exception {
        // Missing required fields
        String invalidJson = "{}";

        mockMvc.perform(post("/api/admin/spots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/admin/spots - Should return all spots")
    void shouldReturnAllSpots() throws Exception {
        // Create spots first
        createSpot("SPOT-A01", 1, SpotSize.SMALL);
        createSpot("SPOT-B01", 2, SpotSize.MEDIUM);

        mockMvc.perform(get("/api/admin/spots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // ==================== Check-In ====================

    @Test
    @DisplayName("POST /api/parking/check-in - Should check in a car successfully")
    void shouldCheckInCarSuccessfully() throws Exception {
        createSpot("SPOT-M01", 1, SpotSize.MEDIUM);

        CheckInRequest request = CheckInRequest.builder()
                .licensePlate("MH-12-AB-1234")
                .vehicleType(VehicleType.CAR)
                .build();

        mockMvc.perform(post("/api/parking/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate").value("MH-12-AB-1234"))
                .andExpect(jsonPath("$.vehicleType").value("CAR"))
                .andExpect(jsonPath("$.spotNumber").value("SPOT-M01"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.entryTime").exists());
    }

    @Test
    @DisplayName("POST /api/parking/check-in - Should assign motorcycle to smallest available spot")
    void shouldAssignMotorcycleToSmallestSpot() throws Exception {
        createSpot("SPOT-L01", 1, SpotSize.LARGE);
        createSpot("SPOT-S01", 1, SpotSize.SMALL);

        CheckInRequest request = CheckInRequest.builder()
                .licensePlate("MH-01-MC-0001")
                .vehicleType(VehicleType.MOTORCYCLE)
                .build();

        mockMvc.perform(post("/api/parking/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spotNumber").value("SPOT-S01"));
    }

    @Test
    @DisplayName("POST /api/parking/check-in - Should reject duplicate check-in")
    void shouldRejectDuplicateCheckIn() throws Exception {
        createSpot("SPOT-M01", 1, SpotSize.MEDIUM);
        createSpot("SPOT-M02", 1, SpotSize.MEDIUM);

        CheckInRequest request = CheckInRequest.builder()
                .licensePlate("MH-12-AB-1234")
                .vehicleType(VehicleType.CAR)
                .build();

        // First check-in
        mockMvc.perform(post("/api/parking/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Duplicate check-in
        mockMvc.perform(post("/api/parking/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("already checked in")));
    }

    @Test
    @DisplayName("POST /api/parking/check-in - Should return conflict when no spots available")
    void shouldReturnConflictWhenNoSpots() throws Exception {
        // No spots created

        CheckInRequest request = CheckInRequest.builder()
                .licensePlate("MH-12-AB-1234")
                .vehicleType(VehicleType.CAR)
                .build();

        mockMvc.perform(post("/api/parking/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("No compatible parking spots")));
    }

    @Test
    @DisplayName("POST /api/parking/check-in - Should reject invalid request")
    void shouldRejectInvalidCheckInRequest() throws Exception {
        String invalidJson = "{\"licensePlate\": \"\"}";

        mockMvc.perform(post("/api/parking/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    // ==================== Check-Out ====================

    @Test
    @DisplayName("POST /api/parking/check-out - Should check out and return fee")
    void shouldCheckOutAndReturnFee() throws Exception {
        createSpot("SPOT-M01", 1, SpotSize.MEDIUM);

        // Check in first
        CheckInRequest checkInReq = CheckInRequest.builder()
                .licensePlate("MH-12-AB-1234")
                .vehicleType(VehicleType.CAR)
                .build();
        mockMvc.perform(post("/api/parking/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkInReq)))
                .andExpect(status().isOk());

        // Check out
        CheckOutRequest checkOutReq = CheckOutRequest.builder()
                .licensePlate("MH-12-AB-1234")
                .build();
        mockMvc.perform(post("/api/parking/check-out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkOutReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.fee").isNumber())
                .andExpect(jsonPath("$.exitTime").exists());
    }

    @Test
    @DisplayName("POST /api/parking/check-out - Should release spot after checkout")
    void shouldReleaseSpotAfterCheckout() throws Exception {
        createSpot("SPOT-M01", 1, SpotSize.MEDIUM);

        // Check in
        CheckInRequest checkInReq = CheckInRequest.builder()
                .licensePlate("MH-12-AB-1234")
                .vehicleType(VehicleType.CAR)
                .build();
        mockMvc.perform(post("/api/parking/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkInReq)))
                .andExpect(status().isOk());

        // Verify availability is 0
        mockMvc.perform(get("/api/parking/availability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableSpots").value(0));

        // Check out
        CheckOutRequest checkOutReq = CheckOutRequest.builder()
                .licensePlate("MH-12-AB-1234")
                .build();
        mockMvc.perform(post("/api/parking/check-out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkOutReq)))
                .andExpect(status().isOk());

        // Verify availability is 1 again
        mockMvc.perform(get("/api/parking/availability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableSpots").value(1));
    }

    @Test
    @DisplayName("POST /api/parking/check-out - Should return 404 for unknown license plate")
    void shouldReturn404ForUnknownPlate() throws Exception {
        CheckOutRequest request = CheckOutRequest.builder()
                .licensePlate("UNKNOWN-PLATE")
                .build();

        mockMvc.perform(post("/api/parking/check-out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("No active ticket found")));
    }

    // ==================== Availability ====================

    @Test
    @DisplayName("GET /api/parking/availability - Should return correct availability breakdown")
    void shouldReturnCorrectAvailability() throws Exception {
        createSpot("SPOT-S01", 1, SpotSize.SMALL);
        createSpot("SPOT-S02", 1, SpotSize.SMALL);
        createSpot("SPOT-M01", 2, SpotSize.MEDIUM);
        createSpot("SPOT-L01", 3, SpotSize.LARGE);

        mockMvc.perform(get("/api/parking/availability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSpots").value(4))
                .andExpect(jsonPath("$.availableSpots").value(4))
                .andExpect(jsonPath("$.breakdown.SMALL.total").value(2))
                .andExpect(jsonPath("$.breakdown.SMALL.available").value(2))
                .andExpect(jsonPath("$.breakdown.MEDIUM.total").value(1))
                .andExpect(jsonPath("$.breakdown.MEDIUM.available").value(1))
                .andExpect(jsonPath("$.breakdown.LARGE.total").value(1))
                .andExpect(jsonPath("$.breakdown.LARGE.available").value(1));
    }

    @Test
    @DisplayName("GET /api/parking/availability - Should update availability after check-in")
    void shouldUpdateAvailabilityAfterCheckIn() throws Exception {
        createSpot("SPOT-M01", 1, SpotSize.MEDIUM);
        createSpot("SPOT-M02", 1, SpotSize.MEDIUM);

        // Check in one car
        CheckInRequest request = CheckInRequest.builder()
                .licensePlate("MH-12-AB-1234")
                .vehicleType(VehicleType.CAR)
                .build();
        mockMvc.perform(post("/api/parking/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify availability decreased
        mockMvc.perform(get("/api/parking/availability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSpots").value(2))
                .andExpect(jsonPath("$.availableSpots").value(1))
                .andExpect(jsonPath("$.breakdown.MEDIUM.total").value(2))
                .andExpect(jsonPath("$.breakdown.MEDIUM.available").value(1));
    }

    // ==================== Full Parking Flow ====================

    @Test
    @DisplayName("Full flow: create spots -> check in -> availability -> check out -> availability")
    void fullParkingFlow() throws Exception {
        // 1. Create spots
        createSpot("SPOT-S01", 1, SpotSize.SMALL);
        createSpot("SPOT-M01", 2, SpotSize.MEDIUM);
        createSpot("SPOT-L01", 3, SpotSize.LARGE);

        // 2. Check in a car (should get MEDIUM spot)
        CheckInRequest carCheckIn = CheckInRequest.builder()
                .licensePlate("MH-12-CAR-001")
                .vehicleType(VehicleType.CAR)
                .build();
        mockMvc.perform(post("/api/parking/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carCheckIn)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spotNumber").value("SPOT-M01"));

        // 3. Check in a motorcycle (should get SMALL spot)
        CheckInRequest motoCheckIn = CheckInRequest.builder()
                .licensePlate("MH-01-MC-001")
                .vehicleType(VehicleType.MOTORCYCLE)
                .build();
        mockMvc.perform(post("/api/parking/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(motoCheckIn)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spotNumber").value("SPOT-S01"));

        // 4. Check availability - 1 LARGE spot remaining
        mockMvc.perform(get("/api/parking/availability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableSpots").value(1));

        // 5. Check out the car
        CheckOutRequest carCheckOut = CheckOutRequest.builder()
                .licensePlate("MH-12-CAR-001")
                .build();
        mockMvc.perform(post("/api/parking/check-out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carCheckOut)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.fee").isNumber());

        // 6. Verify availability is now 2 (MEDIUM released + LARGE still available)
        mockMvc.perform(get("/api/parking/availability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableSpots").value(2));
    }

    // ==================== Helpers ====================

    private void createSpot(String spotNumber, int floor, SpotSize spotSize) throws Exception {
        SpotCreationRequest request = SpotCreationRequest.builder()
                .spotNumber(spotNumber)
                .floor(floor)
                .spotSize(spotSize)
                .build();
        mockMvc.perform(post("/api/admin/spots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}

package com.parkit.parkingsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;

public class FareCalculatorServiceTest {

	private static FareCalculatorService fareCalculatorService;
	private Ticket ticket;

	@BeforeAll
	private static void setUp() {
		fareCalculatorService = new FareCalculatorService();
	}

	@BeforeEach
	private void setUpPerTest() {
		ticket = new Ticket();
	}

	@Test
	public void calculateFareCar() {
		LocalDateTime inTime = LocalDateTime.now(ZoneId.systemDefault()).minusHours(1);
		LocalDateTime outTime = LocalDateTime.now(ZoneId.systemDefault());

		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		assertEquals(ticket.getPrice(), 60 * Fare.CAR_RATE_PER_HOUR);
	}

	@Test
	public void calculateFareBike() {
		LocalDateTime inTime = LocalDateTime.now(ZoneId.systemDefault()).minusHours(1);
		LocalDateTime outTime = LocalDateTime.now(ZoneId.systemDefault());

		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		assertEquals(ticket.getPrice(), 60 * Fare.BIKE_RATE_PER_HOUR);
	}

	@Test
	public void calculateFareUnkownType() {
		LocalDateTime inTime = LocalDateTime.now(ZoneId.systemDefault()).minusHours(1);
		LocalDateTime outTime = LocalDateTime.now(ZoneId.systemDefault());

		ParkingSpot parkingSpot = new ParkingSpot(1, null, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
	}

	@Test
	public void calculateFareBikeWithFutureInTime() {
		LocalDateTime inTime = LocalDateTime.now(ZoneId.systemDefault());
		LocalDateTime outTime = LocalDateTime.now(ZoneId.systemDefault()).minusHours(1);

		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
	}

	@Test
	public void calculateFareBikeWithLessThanOneHourParkingTime() {
		LocalDateTime inTime = LocalDateTime.now(ZoneId.systemDefault());

		LocalDateTime outTime = LocalDateTime.now(ZoneId.systemDefault()).plusMinutes(45);// 45 minutes parking time
																							// should give 3/4th
		// parking fare

		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		assertEquals((45 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
	}

	@Test
	public void calculateFareCarWithLessThanOneHourParkingTime() {
		// GIVEN
		LocalDateTime inTime = LocalDateTime.now(ZoneId.systemDefault()).minusMinutes(45);
		LocalDateTime outTime = LocalDateTime.now(ZoneId.systemDefault());// 45 minutes parking time
																			// should give 3/4th
		// parking fare
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		// WHEN
		fareCalculatorService.calculateFare(ticket);
		// THEN
		assertEquals((45 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
	}

	@Test
	public void calculateFareCarWithMoreThanADayParkingTime() {
		LocalDateTime inTime = LocalDateTime.now(ZoneId.systemDefault()).minusDays(1);
		LocalDateTime outTime = LocalDateTime.now(ZoneId.systemDefault());// 24 hours parking time should give 24 *
																			// parking fare per hour

		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		assertEquals((24 * 60 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
	}

	@Test
	public void calculateFareBikeWithLessThanThirtyMinutesParkingTime() {
		LocalDateTime inTime = LocalDateTime.now(ZoneId.systemDefault()).minusMinutes(29);
		LocalDateTime outTime = LocalDateTime.now(ZoneId.systemDefault());

		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		assertEquals((0 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
	}

	@Test
	public void calculateFareCarWithLessThanThirtyMinutesParkingTime() {
		LocalDateTime inTime = LocalDateTime.now(ZoneId.systemDefault()).minusMinutes(29);
		LocalDateTime outTime = LocalDateTime.now(ZoneId.systemDefault());

		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		assertEquals((0 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
	}

	@Test
	public void calculateFareCarWithExactlynThirtyMinutesParkingTime() {
		LocalDateTime inTime = LocalDateTime.now(ZoneId.systemDefault()).minusMinutes(30);

		LocalDateTime outTime = LocalDateTime.now(ZoneId.systemDefault());

		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		assertEquals((30 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
	}

	@Test
	public void calculateFareBikeWithExactlynThirtyMinutesParkingTime() {
		LocalDateTime inTime = LocalDateTime.now(ZoneId.systemDefault()).minusMinutes(30);
		LocalDateTime outTime = LocalDateTime.now(ZoneId.systemDefault());

		ParkingSpot parkingSpot = new ParkingSpot(4, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		assertEquals((30 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
	}
}

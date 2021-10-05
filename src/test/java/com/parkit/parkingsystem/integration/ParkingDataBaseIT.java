package com.parkit.parkingsystem.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

	private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
	private static ParkingSpotDAO parkingSpotDAO;
	private static TicketDAO ticketDAO;
	private static DataBasePrepareService dataBasePrepareService;
	private ParkingType parkingType;
	private ParkingSpot parkingSpot;

	@Mock
	private static InputReaderUtil inputReaderUtil;

	@BeforeAll
	private static void setUp() throws Exception {
		parkingSpotDAO = new ParkingSpotDAO();
		parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
		ticketDAO = new TicketDAO();
		ticketDAO.dataBaseConfig = dataBaseTestConfig;
		dataBasePrepareService = new DataBasePrepareService();
	}

	@BeforeEach
	private void setUpPerTest() throws Exception {
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		dataBasePrepareService.clearDataBaseEntries();
	}

	@AfterAll
	private static void tearDown() {

	}

	@Test
	public void testParkingACar() {

		// TODO: check that a ticket is actually saved in DB and Parking table is
		// updated with availability

		// ARRANGE
		when(inputReaderUtil.readSelection()).thenReturn(1);
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		// ACT
		parkingService.processIncomingVehicle();

		// ASSERT
		// Ticket
		Ticket getTicketSaved = ticketDAO.getTicket("ABCDEF");
		assertThat(getTicketSaved.getVehicleRegNumber()).isEqualTo("ABCDEF");
		assertThat(getTicketSaved.getPrice()).isEqualTo(0);
		assertThat(getTicketSaved.getOutTime()).isNull();

		// Parking
		int availabilityParking = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
		assertThat(availabilityParking).isEqualTo(2);

	}

	@Test
	public void testParkingLotExitCar() {
		testParkingACar();
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processExitingVehicle();

		// TODO: check that the fare generated and out time are populated correctly in
		// the database

		Ticket ticket = ticketDAO.getTicket("ABCDEF");
		int numberOfNextAvailableSlot = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

		assertNotNull(ticket.getOutTime());
		assertEquals(1, numberOfNextAvailableSlot);
	}

	@Test
	public void testParkingABike() throws Exception {
		// TODO: check that a ticket is actually saved in DB and Parking table is
		// updated with availability

		when(inputReaderUtil.readSelection()).thenReturn(2);
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("BCDEFG");
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingType = ParkingType.BIKE;
		parkingSpotDAO.getNextAvailableSlot(parkingType);

		// WHEN
		parkingService.processIncomingVehicle();

		// THEN
		assertThat(parkingSpotDAO.getNextAvailableSlot(parkingType)).isNotEqualTo(4);

		assertThat(ticketDAO.getTicket("BCDEFG").getParkingSpot().getId()).isEqualTo(4);
	}

	@Test
	public void testParkingLotExitBike() throws Exception {
		testParkingABike();

		// TODO: check that the fare generated and out time are populated correctly in
		// the database

		// GIVEN
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("BCDEFG2");
		parkingSpot = new ParkingSpot(5, ParkingType.BIKE, false);
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		LocalDateTime inTime = LocalDateTime.now(ZoneId.systemDefault()).minusMinutes(60);

		Ticket ticket = new Ticket();
		ticket.setParkingSpot(parkingSpot);
		ticket.setInTime(inTime);

		ticket.setVehicleRegNumber("BCDEFG2");
		ticketDAO.saveTicket(ticket);

		// WHEN
		parkingService.processExitingVehicle();
		ticket = ticketDAO.getTicket("BCDEFG2");

		// THEN

		assertThat(ticket.getPrice()).isNotEqualTo(60);
		assertThat(ticket.getParkingSpot().getId()).isEqualTo(5);
		assertNotNull(ticket.getOutTime());
	}

}

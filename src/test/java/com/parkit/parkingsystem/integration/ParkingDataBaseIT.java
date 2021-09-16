package com.parkit.parkingsystem.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

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
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

	private static final String VehicleRegNumber = null;
	private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
	private static ParkingSpotDAO parkingSpotDAO;
	private static TicketDAO ticketDAO;
	private static DataBasePrepareService dataBasePrepareService;

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
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle();
		// TODO: check that a ticket is actually saved in DB and Parking table is
		// updated with availability : equals pour number, getout time et intime et id
		String vehicleRegNumber = "ABCDEF";
		Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
		double price = 0;
		boolean saved = ticketDAO.isSaved(VehicleRegNumber);
		// THEN
		assertEquals(true, saved);
		assertEquals(ticket.getPrice(), price);
		assertEquals(ticket.getVehicleRegNumber(), "ABCDEF");
		assertEquals(2, parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR));
	}

	@Test
	public void testParkingLotExit() {
		testParkingACar();
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processExitingVehicle();

		// TODO: check that the fare generated and out time are populated correctly in
		// the database

		Ticket ticket = ticketDAO.getTicket(VehicleRegNumber);
		int numberOfNextAvailableSlot = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

		assertNotNull(ticket.getOutTime());
		assertEquals(1, numberOfNextAvailableSlot);
	}

	@Test
	public void testParkingABike() {
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle();
		// TODO: check that a ticket is actually saved in DB and Parking table is
		// updated with availability : equals pour number,
		String vehicleRegNumber = "ABCDEF";
		Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
		double price = 0;
		boolean saved = ticketDAO.isSaved(VehicleRegNumber);
		// THEN
		assertEquals(true, saved);
		assertEquals(ticket.getPrice(), price);
		assertEquals(ticket.getVehicleRegNumber(), "ABCDEF");
		assertEquals(2, parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE));
	}

	@Test
	public void testParkingLotExitBike() {
		testParkingABike();
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processExitingVehicle();

		// TODO: check that the fare generated and out time are populated correctly in
		// the database

		Ticket ticket = ticketDAO.getTicket(VehicleRegNumber);
		int numberOfNextAvailableSlot = parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE);

		assertNotNull(ticket.getOutTime());
		assertEquals(1, numberOfNextAvailableSlot);
	}

}

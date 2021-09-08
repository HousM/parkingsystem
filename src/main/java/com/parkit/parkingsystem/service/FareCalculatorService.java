package com.parkit.parkingsystem.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

	public void calculateFare(Ticket ticket) {
		if ((ticket.getOutTime() == null) || (ticket.getOutTime().isBefore(ticket.getInTime()))) {
			throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
		}

		LocalDateTime inHour = ticket.getInTime();
		LocalDateTime outHour = ticket.getOutTime();

		// TODO: Some tests are failing here. Need to check if this logic is correct
		long duration = ChronoUnit.SECONDS.between(inHour, outHour) / 3600;
		if (duration >= 30 * 60) {
			switch (ticket.getParkingSpot().getParkingType()) {
			case CAR: {
				ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
				calculDiscount(ticket.getPrice(), ticket);
				break;
			}
			case BIKE: {
				ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
				calculDiscount(ticket.getPrice(), ticket);
				break;
			}
			default:
				throw new IllegalArgumentException("Unkown Parking Type");
			}
		} else {
			ticket.setPrice(0.0);
		}
	}

	public void calculDiscount(double price, Ticket ticket) {
		if (ticket.isDiscount()) {
			double discount = 5 / 100;
			price = price * (1 - discount);
		}
		ticket.setPrice(price);
	}

}
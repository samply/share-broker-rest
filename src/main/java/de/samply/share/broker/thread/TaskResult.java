/*
 * Copyright (C) 2015 Working Group on Joint Research,
 * Division of Medical Informatics,
 * Institute of Medical Biometrics, Epidemiology and Informatics,
 * University Medical Center of the Johannes Gutenberg University Mainz
 *
 * Contact: info@osse-register.de
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with Jersey (https://jersey.java.net) (or a modified version of that
 * library), containing parts covered by the terms of the General Public
 * License, version 2.0, the licensors of this Program grant you additional
 * permission to convey the resulting work.
 */
package de.samply.share.broker.thread;

/**
 * An instance of this class needs to be returned by any Task to be executed by Application. Extend this class to include custom return values.
 */
public class TaskResult {

	/** The error code. */
	private int errorCode;

	/** The message to be logged. */
	private String messageToBeLogged;

	/**
	 * Instantiates a new task result.
	 *
	 * @param errorCode
	 *            the error code
	 * @param messageToBeLogged
	 *            the message to be logged
	 */
	public TaskResult(int errorCode, String messageToBeLogged) {
		this.errorCode = errorCode;
		this.messageToBeLogged = messageToBeLogged;
	}

	/**
	 * Gets the error code.
	 *
	 * @return the error code
	 */
	public int getErrorCode() {
		return errorCode;
	}

	/**
	 * Gets the message to be logged.
	 *
	 * @return the message to be logged
	 */
	public String getMessageToBeLogged() {
		return messageToBeLogged;
	}
}

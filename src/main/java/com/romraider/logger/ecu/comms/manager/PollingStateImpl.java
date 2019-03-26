/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2018 RomRaider.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.romraider.logger.ecu.comms.manager;

public final class PollingStateImpl implements PollingState {
    // todo: something is not right here! why are these fields all 'static'? looks like should be instance variables
    // todo: not class variables?
    private static State currentState;
    private static State lastpollState;
    private static boolean newQuery;
    private static boolean lastQuery;
    private static boolean fastPoll;

    public PollingStateImpl() {
        setCurrentState(State.STATE_0);
        setLastState(State.STATE_0);
        setNewQuery(true);
        setLastQuery(false);
        setFastPoll(false);
    }

    public State getCurrentState() {
        return currentState;
    }

    public void setCurrentState(State state) {
        currentState = state;
    }

    public State getLastState() {
        return lastpollState;
    }

    public void setLastState(State state) {
        lastpollState = state;
    }

    public boolean isNewQuery() {
        return newQuery;
    }

    public void setNewQuery(boolean state) {
        newQuery = state;
    }

    public boolean isLastQuery() {
        return lastQuery;
    }

    public void setLastQuery(boolean state) {
        lastQuery = state;
    }

    public boolean isFastPoll() {
        return fastPoll;
    }

    public void setFastPoll(boolean state) {
        fastPoll = state;
    }

    public String toString() {
        final String state = String.format(
                "Polling State [isFastPoll=%s, CurrentState=%s, LastState=%s, " +
                "isNewQuery=%s, isLastQuery=%s]",
            isFastPoll(),
            getCurrentState(),
            getLastState(),
            isNewQuery(),
            isLastQuery()
        );
        return state;
    }
}

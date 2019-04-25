package uk.ac.bris.cs.scotlandyard.model;

import java.util.*;

class NotifySpectators {
    static void doubleMoveNotif(Collection<Spectator> spectators, DoubleMove move, int currentRound, List<Boolean> rounds, int revealedLocation, ScotlandYardView view) {
        // Hidden to hidden
        DoubleMove H2H = new DoubleMove(move.colour(), move.firstMove().ticket(), revealedLocation, move.secondMove().ticket(), revealedLocation);
        // Reveal to hidden
        DoubleMove R2H = new DoubleMove(move.colour(), move.firstMove().ticket(), move.firstMove().destination(), move.secondMove().ticket(), move.firstMove().destination());
        // Hidden to reveal
        DoubleMove H2R = new DoubleMove(move.colour(), move.firstMove().ticket(), revealedLocation, move.secondMove().ticket(), move.finalDestination());

        // Depending on which rounds are true, choose the correct DoubleMove from above
        for (Spectator spectator : spectators) {
            if (rounds.get(currentRound) && rounds.get(currentRound + 1)) spectator.onMoveMade(view, move);
            else if (rounds.get(currentRound)) spectator.onMoveMade(view, R2H);
            else if (rounds.get(currentRound + 1)) spectator.onMoveMade(view, H2R);
            else spectator.onMoveMade(view, H2H);
        }
    }

    static void roundNotif (Collection<Spectator> spectators, int currentRound, ScotlandYardView view) {
        for (Spectator spectator : spectators) spectator.onRoundStarted(view, currentRound);
    }

    static void ticketMoveNotif(Collection<Spectator> spectators, TicketMove move, int currentRound, List<Boolean> rounds, int destination, ScotlandYardView view) {
        for (Spectator spectator : spectators) {
            if (move.colour().isMrX()) {
                if (rounds.get(currentRound)) spectator.onMoveMade(view, move); // Reveal location
                else spectator.onMoveMade(view, new TicketMove(move.colour(), move.ticket(), destination)); // Don't reveal location
            }
            else spectator.onMoveMade(view, move);
        }
    }

    static void passMoveNotif(Collection<Spectator> spectators, PassMove move, ScotlandYardView view) {
        for (Spectator spectator : spectators) spectator.onMoveMade(view, move);
    }

    static void gameOverNotif(Collection<Spectator> spectators, Set<Colour> players,  ScotlandYardView view) {
        for (Spectator spectator : spectators) spectator.onGameOver(view, players);
    }
}

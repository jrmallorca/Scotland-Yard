package uk.ac.bris.cs.scotlandyard.model;

import java.util.*;

class NotifySpectators {
    static DoubleMove doubleMoveNotif(Collection<Spectator> spectators, DoubleMove move, int currentRound, List<Boolean> rounds, int revealedLocation, ScotlandYardView view) {
        // Hidden to hidden
        DoubleMove H2H = new DoubleMove(move.colour(), move.firstMove().ticket(), revealedLocation, move.secondMove().ticket(), revealedLocation);
        // Reveal to hidden
        DoubleMove R2H = new DoubleMove(move.colour(), move.firstMove().ticket(), move.firstMove().destination(), move.secondMove().ticket(), move.firstMove().destination());
        // Hidden to reveal
        DoubleMove H2R = new DoubleMove(move.colour(), move.firstMove().ticket(), revealedLocation, move.secondMove().ticket(), move.finalDestination());

        // Depending on which rounds are true, choose the correct DoubleMove from above
        DoubleMove chooseMove = move;

        for (Spectator spectator : spectators) {
            if (rounds.get(currentRound) && rounds.get(currentRound + 1)) {
                spectator.onMoveMade(view, chooseMove);
                spectator.onRoundStarted(view, currentRound);
            }
            else if (rounds.get(currentRound)) {
                chooseMove = R2H;
                spectator.onMoveMade(view, chooseMove);
                spectator.onRoundStarted(view, currentRound);
            }
            else if (rounds.get(currentRound + 1)) {
                chooseMove = H2R;
                spectator.onMoveMade(view, chooseMove);
                spectator.onRoundStarted(view, currentRound);
            }
            else {
                chooseMove = H2H;
                spectator.onMoveMade(view, chooseMove);
                spectator.onRoundStarted(view, currentRound);
            }
        }

        return chooseMove;
    }

    static void ticketMoveWithRoundNotifAlt(Collection<Spectator> spectators, TicketMove move, int currentRound, List<Boolean> rounds, int destination, ScotlandYardView view) {
        for (Spectator spectator : spectators) {
            if (move.colour().isMrX()) {
                spectator.onRoundStarted(view, currentRound); // Update round

                --currentRound;

                if (rounds.get(currentRound)) spectator.onMoveMade(view, move); // Reveal location
                else spectator.onMoveMade(view, new TicketMove(move.colour(), move.ticket(), destination)); // Don't reveal location
            }
            else spectator.onMoveMade(view, move);
        }
    }

    static void ticketMoveWithRoundNotif(Collection<Spectator> spectators, TicketMove move, int currentRound, List<Boolean> rounds, int destination, ScotlandYardView view) {
        for (Spectator spectator : spectators) {
            if (move.colour().isMrX()) {
                if (rounds.get(currentRound)) spectator.onMoveMade(view, move); // Reveal location
                else spectator.onMoveMade(view, new TicketMove(move.colour(), move.ticket(), destination)); // Don't reveal location

                spectator.onRoundStarted(view, currentRound); // Update round
            }
            else spectator.onMoveMade(view, move);
        }
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

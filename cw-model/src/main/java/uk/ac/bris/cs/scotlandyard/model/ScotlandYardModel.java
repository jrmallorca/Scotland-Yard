package uk.ac.bris.cs.scotlandyard.model;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static uk.ac.bris.cs.scotlandyard.model.Colour.BLACK;
import static uk.ac.bris.cs.scotlandyard.model.NotifySpectators.*;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import com.google.errorprone.annotations.Var;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.ImmutableGraph;
import uk.ac.bris.cs.gamekit.graph.Node;

public class ScotlandYardModel implements ScotlandYardGame, Consumer<Move>, MoveVisitor {

	// Fields
	final List<Boolean> rounds; // True rounds are reveal rounds. False rounds are hidden rounds
	private final Graph<Integer, Transport> graph;
	final List<ScotlandYardPlayer> players;
	private final ScotlandYardView view = this;

	// Fields which are constantly updated
	private int currentRound = NOT_STARTED;
	private boolean roundFinished = false;
	private int playerIndex = 0; // The index of the current player in List<ScotlandYardPlayer> players;
	private int revealedLocation = 0; // Stores the revealed locations of Mr X
	private Set<Move> validMoves;

	// Spectators (also a constantly updated field)
	private Collection<Spectator> spectators = new ArrayList<>();

    // ----------------------------------------------------------------------------------------------------------------
	// Constructor

	public ScotlandYardModel(List<Boolean> rounds, Graph<Integer, Transport> graph,
			PlayerConfiguration mrX, PlayerConfiguration firstDetective,
			PlayerConfiguration... restOfTheDetectives) {

		// Returns a bool for empty rounds/maps via isEmpty() method
		if (rounds.isEmpty()) throw new IllegalArgumentException("Empty rounds");
		if (graph.isEmpty()) throw new IllegalArgumentException("Empty map");

		/*
		requireNonNull() method allows for a NullPointerException to be thrown when encountering a null.
		We want to fail as fast as possible when we encounter a problem. Tests for nulls.
		 */
		this.rounds = requireNonNull(rounds);
		this.graph = requireNonNull(graph);

		// Returns bool if mrX is BLACK or not
		if (mrX.colour.isDetective()) throw new IllegalArgumentException("MrX should be Black");

		/*
		We'll temporarily put the detectives into an ArrayList so that we can loop through tests for them.
		configuration represents mrX and first detective. Implement a for-each loop.

		Code different to website because testNullDetectiveShouldThrow wouldn't work. We separately test
		the firstDetective.
		 */
		ArrayList<PlayerConfiguration> configurations = new ArrayList<>();

		configurations.add(0, requireNonNull(mrX)); // Add mrX into configurations (Always first in list)
		configurations.add(requireNonNull(firstDetective)); // Add firstDetective into configurations
		for (PlayerConfiguration detective : restOfTheDetectives) { // Add restOfTheDetectives into configurations
			configurations.add(requireNonNull(detective));
		}

		/*
		We're making a set of locations and colours to check if there are any duplicated. If there's none,
		add them to the set.
		 */
		Set<Integer> LocationSet = new HashSet<>();
		for (PlayerConfiguration configuration : configurations) {
			if (LocationSet.contains(configuration.location)) throw new IllegalArgumentException("Duplicate location");
			LocationSet.add(configuration.location);
		}

		Set<Colour> ColourSet = new HashSet<>();
		for (PlayerConfiguration configuration : configurations) {
			if (ColourSet.contains(configuration.colour)) throw new IllegalArgumentException("Duplicate colour");
			ColourSet.add(configuration.colour);
		}

		/*
		Checking SECRET and DOUBLE tickets for detectives are more than 0.
		Also checks if all players have all types of tickets (e.g. Detectives should have SECRET key but value = 0).
		 */
		for (PlayerConfiguration configuration : configurations) {
			if(!(configuration.tickets.containsKey(DOUBLE) &&
				 configuration.tickets.containsKey(SECRET) &&
				 configuration.tickets.containsKey(TAXI) &&
				 configuration.tickets.containsKey(BUS) &&
				 configuration.tickets.containsKey(UNDERGROUND)) ) {
				throw new IllegalArgumentException("Detective/Mr X is missing tickets");
			}

			if (configuration.colour.isDetective()) {
				if(configuration.tickets.get(DOUBLE) > 0) throw new IllegalArgumentException("Detective has DOUBLE ticket");
				if(configuration.tickets.get(SECRET) > 0) throw new IllegalArgumentException("Detective has SECRET ticket");
			}
		}

		/*
		Create a list of ScotlandYardPlayer(s) (Their indexes are representative to the specific player)
		 */
		this.players = new ArrayList<>();
		for (PlayerConfiguration configuration : configurations) {
			ScotlandYardPlayer p = new ScotlandYardPlayer(configuration.player, configuration.colour,
														  configuration.location, configuration.tickets);
			players.add(p);
		}
	}

	// ----------------------------------------------------------------------------------------------------------------
    // Methods

	@Override
	public void registerSpectator(Spectator spectator) {
		if (spectators.contains(spectator)) throw new IllegalArgumentException("Can't register a spectator that's in the list already");
		spectators.add(requireNonNull(spectator));
	}

	@Override
	public void unregisterSpectator(Spectator spectator) {
		if (!spectators.contains(requireNonNull(spectator))) throw new IllegalArgumentException("Can't unregister a spectator not in the list. \n" +
																								"Can't unregister a null spectator");
		spectators.remove(requireNonNull(spectator));
	}

    // Gets a set of TicketMoves (for getValidMoves())
	private Set<TicketMove> getTicketMoves(ScotlandYardPlayer player, Graph<Integer, Transport> graphView, int location) {
        Set<TicketMove> temp = new HashSet<>();

        Node<Integer> currentNode = graphView.getNode(location);
        Collection<Edge<Integer, Transport>> edges = graphView.getEdgesFrom(currentNode);
        Colour colour = player.colour();

        boolean occupiedByDetective = false;

        for (Edge<Integer, Transport> edge : edges) {
            // Checking if another detective has already taken position in the path's destination
            // This is used for the methods of the 2 following comments below
            for (ScotlandYardPlayer detective : players) {
                if (detective.isDetective() && detective != player) {
                	occupiedByDetective = detective.location() == edge.destination().value();
					// Breaks out of loop if occupied by detective is true
					// (We do this as it may be overridden if detective isn't the last player in the list)
                	if (occupiedByDetective) break;
				}
            }

            // Checking if player has enough tickets for this single move and add it if true
            Ticket ticket = fromTransport(edge.data());
            if (player.hasTickets(ticket) && !occupiedByDetective) temp.add(new TicketMove(colour, ticket, edge.destination().value()));

            // If player has a SECRET ticket(s), give them a SECRET version of the ticket above
            if (player.hasTickets(SECRET) && !occupiedByDetective) temp.add(new TicketMove(colour, SECRET, edge.destination().value()));
        }

        // If there are no valid moves, return an empty set
        // (If it's detective we give it a PassMove in the getValidMoves() method)
        if (temp.isEmpty()) return emptySet();
        else return temp;
    }

    // Gets a set of DoubleMoves (for getValidMoves())
	private Set<Move> getDoubleMoves(ScotlandYardPlayer player, Graph<Integer, Transport> graphView, Set<TicketMove> firstTicketMoves) {
        Set<Move> temp = new HashSet<>();

        Colour colour = player.colour();

        // Checking if the player has enough DoubleMove tickets
        // Checking if the currentRound is also the last round (Mr X can't do a double move in the last round)
        if ((getCurrentRound() != getRounds().size() - 1 ) && player.hasTickets(DOUBLE)) {
            for (TicketMove firstMove : firstTicketMoves) {
                // Getting second set of moves
                Set<TicketMove> secondTicketMoves = getTicketMoves(player, graphView, firstMove.destination());

                for (TicketMove secondMove : secondTicketMoves) {
                    // Checking if the ticket needed for the first movement is the same as the second movement ticket
                    if (firstMove.ticket() == secondMove.ticket()) {
                        // Checking if that ticket type is of quantity 2 or above. If true, add
                        if (player.hasTickets(firstMove.ticket(), 2)) temp.add(new DoubleMove(colour, firstMove.ticket(), firstMove.destination(), secondMove.ticket(), secondMove.destination()));
                    }
                    // Else, check if player has enough tickets for both different ticket types
                    else if (player.hasTickets(firstMove.ticket()) && player.hasTickets(secondMove.ticket())) temp.add(new DoubleMove(colour, firstMove.ticket(), firstMove.destination(), secondMove.ticket(), secondMove.destination()));
                }
            }
        }

        // If there are no valid moves, return an empty set
        if (temp.isEmpty()) return emptySet();
        else return temp;
    }

    // The main method of validMoves which unifies the set of TicketMoves, DoubleMoves and adds PassMove if needed
    // Can also return an empty set if Mr X has no more moves left
	private Set<Move> getValidMoves(ScotlandYardPlayer player) {
        Graph<Integer, Transport> graphView = getGraph();

        // Getting set of the first movements in DoubleMove
        Set<TicketMove> firstTicketMoves = getTicketMoves(player, graphView, player.location());

        // Unification of TicketMoves and DoubleMoves
        Set<Move> tempMoves = new HashSet<>(firstTicketMoves); // First assigns set of TicketMoves inside tempMoves
        if (player.isMrX()) tempMoves.addAll(getDoubleMoves(player, graphView, firstTicketMoves)); // Add all DoubleMoves
        // If detective has no more tickets, give them a PassMove
        if (player.isDetective() && tempMoves.isEmpty()) tempMoves.add(new PassMove(player.colour())); // Add a PassMove

        // If Mr X has no tickets left (or has nothing but DOUBLE tickets), return empty set
        if (tempMoves.isEmpty()) return emptySet();
        else return tempMoves;
    }

	@Override
	public void startRotate() {
		if (!isGameOver()) {
			Colour currentColour = getCurrentPlayer();
			ScotlandYardPlayer currentPlayer = getCurrentScotlandYardPlayer(currentColour);
			validMoves = getValidMoves(currentPlayer);

			/*
			1. You pass 'this' for the 1st parameter because it's essentially a ScotlandYardView
			2. You pass the currentPlayer's location for the 2nd parameter
			3. You pass the valid set of moves the player can choose to do for the 3rd parameter
			4. You pass 'this' for the 4th parameter so that the accept method is called, which then results in
			   notifying the next player to make a move.
			 */
			currentPlayer.player().makeMove(this, currentPlayer.location(), validMoves, this);
		}
		else throw new IllegalStateException("Can't do this when the game is over");
	}

	// Sees if move is non-null, then executes proper play logic from ticket of move
    @Override // Method from Consumer interface
    public void accept(Move move) {
        if (!validMoves.contains(requireNonNull(move))) throw new IllegalArgumentException("Can't pass null move");

		// Updates current player
		// Checks to see if round had finished
        ++playerIndex;
		if (playerIndex == players.size()) {
			roundFinished = true;
			playerIndex = 0;
		}
		else roundFinished = false;

		// Define accept() methods of visit and give proper play logic
		// Inform spectators of each event
		MoveVisitor visitor = new MoveVisitor() {
			ScotlandYardPlayer player = getCurrentScotlandYardPlayer(move.colour());
			Collection<Spectator> spectatorList = getSpectators();
			List<Boolean> roundList = getRounds();

			/*
			Play logic

			1. Remove appropriate amount of tickets
			2. Move player to destination
			3. Increment round
				3.1. If DoubleMove, then increment currentRound by 2
			4. Appropriately reveal Mr X in certain rounds
				4.1. Reveal at true rounds in List<Boolean> rounds
			5. Notify spectators of current state of game
			 */
        	@Override
			public void visit(TicketMove move) {
        		player.removeTicket(move.ticket());
				player.location(move.destination());

				if (player.isMrX()) {
					if (roundList.get(currentRound)) revealedLocation = player.location();
					++currentRound;
				}
				// Give player's ticket to Mr X
				else getCurrentScotlandYardPlayer(BLACK).addTicket(move.ticket());

				// Notify spectators that a move has been made
				// Notify spectators that a new round has started
				ticketMoveWithRoundNotifAlt(spectatorList, move, currentRound, roundList, revealedLocation, view);
			}

			@Override
			public void visit(DoubleMove move) {
				player.removeTicket(DOUBLE);

				// An alternative version to the current move that appropriately hides/reveals Mr X's location
				DoubleMove shownMove = doubleMoveNotif(getSpectators(), move, currentRound, roundList, revealedLocation, view);

        		// Checks first movement
				player.removeTicket(move.firstMove().ticket());
				player.location(move.firstMove().destination());
				if (roundList.get(currentRound)) revealedLocation = player.location(); // If on reveal round
				++currentRound;

				// Notify spectators of the first movement
				// Notify spectators that a new round has started
				// (Make a new TicketMove with the location as the last revealedLocation if going into hidden round)
				ticketMoveWithRoundNotif(getSpectators(), shownMove.firstMove(), currentRound, roundList, shownMove.firstMove().destination(), view);

				// Checks second movement
				player.removeTicket(move.secondMove().ticket());
				player.location(move.finalDestination());
				if (roundList.get(currentRound)) revealedLocation = player.location(); // If on reveal round
				++currentRound;

				// Notify spectators of the second movement
				ticketMoveNotif(getSpectators(), shownMove.secondMove(), currentRound, roundList, shownMove.secondMove().destination(), view);
        	}

			@Override
			public void visit(PassMove move) {
				passMoveNotif(getSpectators(), move, view);
			}
		};

		// Executes play logic
        move.visit(visitor);

		ScotlandYardPlayer currentPlayer = getCurrentScotlandYardPlayer(getCurrentPlayer());
		validMoves = getValidMoves(currentPlayer);

        // If the round is not finished, notify the next player to make a move
		// Else, notify all spectators the rotation has finished
		if (!roundFinished) currentPlayer.player().makeMove(this, currentPlayer.location(), validMoves, this);
		else if (isGameOver()) gameOverNotif(spectators, getWinningPlayers(), view);
		else for (Spectator spectator : spectators) spectator.onRotationComplete(view);
    }

	@Override
	public Collection<Spectator> getSpectators() {
		return unmodifiableCollection(spectators);
	}

	@Override
	public List<Colour> getPlayers() {
		List<Colour> tempPlayers = new ArrayList<>();
		for (ScotlandYardPlayer player : players) {
			tempPlayers.add(player.colour());
		}
		return unmodifiableList(tempPlayers);
	}

	@Override
	public Set<Colour> getWinningPlayers() {
		Set<Colour> temp = new HashSet<>();

		if (winners().equals("Detectives")) {
			for (ScotlandYardPlayer player : players) {
				if (player.isDetective()) temp.add(player.colour());
			}
		}
		if (winners().equals("Mr X")) temp.add(BLACK);

		return unmodifiableSet(temp);
	}

	// Optional is like a Maybe in Haskell. Useful for the prevention of being screwed up by nulls
	@Override
	public Optional<Integer> getPlayerLocation(Colour colour) {
		if (colour.equals(BLACK)) return Optional.of(revealedLocation);

		for (ScotlandYardPlayer player : players) {
			if (player.colour().equals(colour)) return Optional.of(player.location());
		}

		return Optional.empty();
	}

	@Override
	public Optional<Integer> getPlayerTickets(Colour colour, Ticket ticket) {
		for (ScotlandYardPlayer player : players) {
			if (player.colour().equals(colour)) {
				return Optional.of(player.tickets().get(ticket));
			}
		}
		return Optional.empty();
	}

	private String winners() {
		// If assigned "Detectives", detectives win
		// If assigned "Mr X", Mr X wins
		// If assigned "N/A", no-one has won yet
		String none = "N/A";
		String mrXWins = "Mr X";
		String detectivesWin = "Detectives";

		ScotlandYardPlayer mrX = getCurrentScotlandYardPlayer(BLACK);

		// If Mr X is stuck (no available valid moves), detectives win
		if (getValidMoves(mrX).size() == 0) return detectivesWin;

		// If max rounds is reached, Mr X wins
		if ((currentRound == getRounds().size()) && roundFinished) return mrXWins;

		// Checking if Mr X has been captured or if all detectives have no tickets left
		int sumOfTickets = 0;
		boolean hasTickets = false;

		for (ScotlandYardPlayer detective : players) {
			if (detective.isDetective()) {
				// If Mr X has been captured, detectives win
				if (detective.location() == mrX.location()) return detectivesWin;

				// If all detectives have no tickets left, Mr X wins
				for (int ticketCount : detective.tickets().values()) sumOfTickets += ticketCount;
				hasTickets = sumOfTickets != 0;
			}
		}
		if (!hasTickets) return mrXWins;

		// If all else fails, no-one has won yet
		return none;
	}

	// Simply checks if winners() returns a result
	// If there are winners, notify each spectator
	@Override
	public boolean isGameOver() {
		return winners().equals("Detectives") || winners().equals("Mr X");
	}

	@Override
	public Colour getCurrentPlayer() {
		return players.get(playerIndex).colour();
	}

    private ScotlandYardPlayer getCurrentScotlandYardPlayer(Colour colour) {
        int i = 0;
        while (i < players.size()) {
            if (!(players.get(i).colour().equals(colour))) ++i;
            else break;
        }
        return players.get(i);
    }

	@Override
	public int getCurrentRound() {
		return currentRound;
	}

	@Override
	public List<Boolean> getRounds() {
		return unmodifiableList(rounds);
	}

	@Override
	public Graph<Integer, Transport> getGraph() {
		return new ImmutableGraph<>(graph);
	}

}

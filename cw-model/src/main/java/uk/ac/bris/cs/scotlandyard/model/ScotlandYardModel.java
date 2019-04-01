package uk.ac.bris.cs.scotlandyard.model;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static uk.ac.bris.cs.scotlandyard.model.Colour.*;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.ImmutableGraph;

public class ScotlandYardModel implements ScotlandYardGame, Consumer<Move> {

	// Fields
	List<Boolean> rounds;
	Graph<Integer, Transport> graph;
	List<ScotlandYardPlayer> players;
	private Set<Move> validMoves;
	// FIelds which are being tracked
	int currentRound = NOT_STARTED;
	int playerIndex = 0; // The index of the current player in List<ScotlandYardPlayer> players;
	int mrXLocation = 0; // Starts as 0 if Mr X not revealed yet. Stores specified location number once revealed.

	// Constructor
	public ScotlandYardModel(List<Boolean> rounds, Graph<Integer, Transport> graph,
			PlayerConfiguration mrX, PlayerConfiguration firstDetective,
			PlayerConfiguration... restOfTheDetectives) {

		// Returns a bool for empty rounds/maps via isEmpty() method
		if (rounds.isEmpty()) {
			throw new IllegalArgumentException("Empty rounds");
		}
		if (graph.isEmpty()) {
			throw new IllegalArgumentException("Empty map");
		}

		/*
		requireNonNull() method allows for a NullPointerException to be thrown when encountering a null.
		We want to fail as fast as possible when we encounter a problem. Tests for nulls.
		 */
		this.rounds = requireNonNull(rounds);
		this.graph = requireNonNull(graph);

		// Returns bool if mrX is BLACK or not
		if (mrX.colour.isDetective()) {
			throw new IllegalArgumentException("MrX should be Black");
		}

		/*
		We'll temporarily put the detectives into an ArrayList so that we can loop through tests for them.
		configuration represents mrX and first detective. Implement a for-each loop.

		Code different to website because testNullDetectiveShouldThrow wouldn't work. We seperately test
		the firstDetective.
		 */
		ArrayList<PlayerConfiguration> configurations = new ArrayList<>();

		configurations.add(0, requireNonNull(mrX)); // Add mrX into configurations
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
			if (LocationSet.contains(configuration.location)) {
				throw new IllegalArgumentException("Duplicate location");
			}
			LocationSet.add(configuration.location);
		}

		Set<Colour> ColourSet = new HashSet<>();
		for (PlayerConfiguration configuration : configurations) {
			if (ColourSet.contains(configuration.colour)) {
				throw new IllegalArgumentException("Duplicate colour");
			}
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
				if(configuration.tickets.get(DOUBLE) > 0) {
					throw new IllegalArgumentException("Detective has DOUBLE ticket");
				}
				if(configuration.tickets.get(SECRET) > 0) {
					throw new IllegalArgumentException("Detective has SECRET ticket");
				}
			}
		}

		/*
		Create a list of ScotlandYardPlayer(s) (Their indexes are representative to the specific player hopefully)
		 */
		this.players = new ArrayList<>();
		for (PlayerConfiguration configuration : configurations) {
			ScotlandYardPlayer p = new ScotlandYardPlayer(configuration.player, configuration.colour,
														  configuration.location, configuration.tickets);
			players.add(p);
		}
	}

	// Methods
	public ScotlandYardPlayer getCurrentScotlandYardPlayer(Colour colour) {
		int i = 0;
		while (i < players.size()) {
			if (!(players.get(i).colour().equals(colour))) {
				++i;
			}
			else break;
		}
		return players.get(i);
	}

	/*
	CONCEPT FOR validMoves

	We're using TicketMove because we're literally giving them a choice of using for example a TAXI ticket to a specific
	node or a TAXI ticket to another specific node or a BUS node... etc. We don't just give them tickets, we also give
	the location of using that ticket.

	In actuality, it's a set of TicketMove but the makeMove takes in a parameter of only Move.

	Example of a valid move set based on the player's current location where a tuple represents:
	(Ticket type, Destination),
		{ (TAXI, Node 52), (TAXI, Node 48), (BUS, Node 53) }

	1. Check playerLocation
	2. Check if currentNode is a bus station, ferry station (SECRET Move), underground, or only taxi.
	2. Check # of tickets for each type of ticket.
		2.1. If ticket count = 0 for a specific ticket type, there are no locations you can use for nodes which use that
			 ticket (unless you can use another ticket like using a BUS instead of TAXI for that node).
		2.2. If over 0, check for the location that it can go to and add it in the set.
		2.3. DUNNO FOR SECRET
		2.4. DUNNO FOR DOUBLEMOVE (Using multiple dispatch apparently).
	3. Check if destination is occupied (Different cases for detective/Mr X).
	 */
	public Set<Move> getValidMoves(Colour colour) {
		Set<Move> temp = new HashSet<>();
		getPlayerLocation(colour);



		return temp;
	}

	/*
	REMEMBER TO IMPLEMENT THIS
	Will probably have to interact with MoveVisitor
	 */
	@Override // Method from Consumer interface
	public void accept(Move move) {
		if (!(validMoves.contains(requireNonNull(move)))) {
		    throw new IllegalArgumentException("Can't pass null move");
        }
	}

	@Override
	public void registerSpectator(Spectator spectator) {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public void unregisterSpectator(Spectator spectator) {
		// TODO
		throw new RuntimeException("Implement me");
	}

	/*
	CONCEPT FOR startROTATE
	1. We get the current player
	2. Depending on their colour, we give them a valid set of moves they can make
	3. We use this so that they can make a move
	4. We check whether the move they made was ok via the accept method by consumer
		4.1. If true, go ahead and call back for the next player
		4.2. If false, throw an error ???
	 */
	@Override
	public void startRotate() {
		if (!(isGameOver())) {
			Colour currentColour = getCurrentPlayer();
			ScotlandYardPlayer currentPlayer = getCurrentScotlandYardPlayer(currentColour);
			validMoves = getValidMoves(currentPlayer);

			/*
			1. You pass 'this' for the 1st parameter because it's essentially a ScotlandYardView
			2. You pass the currentPlayer's location for the 2nd parameter
			3. You pass the valid set of moves the player can choose to do for the 3rd parameter
			4. You pass 'this' for the 4th parameter so that the method can be "called back" so
			   the next player will use this method.
			 */
			currentPlayer.player().makeMove(this, getPlayerLocation(currentPlayer.colour()).get(), validMoves, this);
		}
		else throw new IllegalArgumentException("Can't do this when the game is over");
	}

	@Override
	public Collection<Spectator> getSpectators() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public List<Colour> getPlayers() {
		List<Colour> tempPlayers = new ArrayList<>();
		for (ScotlandYardPlayer player : players) {
			tempPlayers.add(player.colour());
		}
		return unmodifiableList(tempPlayers);
	}

	/*
	COME BACK LATER
	 */
	@Override
	public Set<Colour> getWinningPlayers() {
		Set<Colour> temp = new HashSet<>();
		return unmodifiableSet(temp);
	}

	/*
	PUT A TRACKER LATER ONCE YOU'RE DOING ROUNDS
	FOR MR X
	 */
	@Override
	public Optional<Integer> getPlayerLocation(Colour colour) {
		if (colour.equals(BLACK)) {
			return Optional.of(mrXLocation);
		}
		for (ScotlandYardPlayer player : players) {
			if (player.colour().equals(colour)) return Optional.of(player.location());
		}
		return Optional.empty();
	}

	@Override
	public Optional<Integer> getPlayerTickets(Colour colour, Ticket ticket) {
		for (ScotlandYardPlayer player : players) {
			if (player.colour().equals(colour)) {
				return Optional.ofNullable(player.tickets().get(ticket));
			}
		}
		return Optional.empty();
	}

	/*
	Return for later
	 */
	@Override
	public boolean isGameOver() {
		return false;
	}

	/*
	ALERT (Once you implement startRotate method, then come back)
	 */
	@Override
	public Colour getCurrentPlayer() {
		return players.get(playerIndex).colour();
	}

	/*
	(Once you implement startRotate method, then come back)
	 */
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
		return new ImmutableGraph<Integer, Transport>(graph);
	}

}

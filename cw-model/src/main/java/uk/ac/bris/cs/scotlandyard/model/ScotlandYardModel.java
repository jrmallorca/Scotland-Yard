package uk.ac.bris.cs.scotlandyard.model;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static uk.ac.bris.cs.scotlandyard.model.Colour.BLACK;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.DOUBLE;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.SECRET;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.TAXI;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.BUS;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.UNDERGROUND;
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

// TODO implement all methods and pass all tests
public class ScotlandYardModel implements ScotlandYardGame {

    /**
     * An immutable list whose length is the maximum number of moves that Mr X
     * can play in a game. True means that Mr.X reveals its location where as
     * False conceals it.
	 *
	 * Must be non-null and non-empty
     */ // Copied from ScotlandYardView.java
	List<Boolean> rounds;

	/**
	 * An immutable view of the graph the game is using.
	 *
	 * Must be non-null and non-empty
	 */ // Copied from ScotlandYardView.java
	Graph<Integer, Transport> graph;

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

		configurations.add(requireNonNull(firstDetective));
		configurations.add(0, mrX);
		for (PlayerConfiguration configuration : restOfTheDetectives) {
			configurations.add(requireNonNull(configuration));
		}

		/*
		We're making a set of locations and colours to check if there are any duplicated. If there's none,
		add them to the set.
		 */
		Set<Integer> setLocations = new HashSet<>();
		for (PlayerConfiguration configuration : configurations) {
			if (setLocations.contains(configuration.location)) {
				throw new IllegalArgumentException("Duplicate location");
			}
			setLocations.add(configuration.location);
		}

		Set<Colour> setColours = new HashSet<>();
		for (PlayerConfiguration configuration : configurations) {
			if (setColours.contains(configuration.colour)) {
				throw new IllegalArgumentException("Duplicate colour");
			}
			setColours.add(configuration.colour);
		}

		/*
		Checking SECRET and DOUBLE tickets, and missing tickets FOR DETECTIVES
		 */
		configurations.remove(mrX);
		for(PlayerConfiguration configuration : configurations) {
			if(configuration.tickets.containsKey(SECRET)) {
				throw new IllegalArgumentException("Detective has SECRET ticket");
			}
			if(configuration.tickets.containsKey(DOUBLE)) {
				throw new IllegalArgumentException("Detective has DOUBLE ticket");
			}
			if(!(configuration.tickets.containsKey(TAXI) &&
				 configuration.tickets.containsKey(BUS) &&
				 configuration.tickets.containsKey(UNDERGROUND))) {
				throw new IllegalArgumentException("Detective is missing tickets");
			}
		}
		/*
		Checking if MrX is missing any tickets
		 */
		if(!(mrX.tickets.containsKey(TAXI) &&
				mrX.tickets.containsKey(BUS) &&
				mrX.tickets.containsKey(UNDERGROUND) &&
				mrX.tickets.containsKey(DOUBLE) &&
				mrX.tickets.containsKey(SECRET))) {
			throw new IllegalArgumentException("mrX is missing tickets");
		}

		/*
		Create a list of ScotlandYardPlayer's using a for loop (maybe)
		 */

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

	@Override
	public void startRotate() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public Collection<Spectator> getSpectators() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public List<Colour> getPlayers() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public Set<Colour> getWinningPlayers() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public Optional<Integer> getPlayerLocation(Colour colour) {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public Optional<Integer> getPlayerTickets(Colour colour, Ticket ticket) {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public boolean isGameOver() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public Colour getCurrentPlayer() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public int getCurrentRound() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public List<Boolean> getRounds() {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public Graph<Integer, Transport> getGraph() {
		// TODO
		throw new RuntimeException("Implement me");
	}

}

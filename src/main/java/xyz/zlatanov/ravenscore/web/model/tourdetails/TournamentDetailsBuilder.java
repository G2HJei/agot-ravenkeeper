package xyz.zlatanov.ravenscore.web.model.tourdetails;

import static java.math.RoundingMode.UP;
import static xyz.zlatanov.ravenscore.Utils.*;

import java.math.BigDecimal;
import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.ravenscore.domain.domain.*;
import xyz.zlatanov.ravenscore.web.service.RavenscoreException;

@RequiredArgsConstructor
public class TournamentDetailsBuilder {

	private final Tournament			tournament;
	private final List<TournamentStage>	tournamentStageList;
	private final List<Substitute>		substituteList;
	private final List<Participant>		participantList;
	private final List<Game>			gameList;
	private final List<Player>			playerList;

	public TournamentDetailsModel build() {
		return new TournamentDetailsModel()
				.id(tournament.id().toString())
				.name(tournament.name())
				.description(tournament.description())
				.startDate(DATE_FORMATTER.format(tournament.startDate()))
				.substituteModelList(getSubstitutes())
				.tournamentStageModelList(getTournamentStages());
	}

	private List<SubstituteModel> getSubstitutes() {
		return substituteList.stream()
				.map(sub -> new SubstituteModel()
						.name(sub.name())
						.profileLinks(List.of(sub.profileLinks())))
				.toList();
	}

	private List<TournamentStageModel> getTournamentStages() {
		return tournamentStageList.stream()
				.filter(stage -> stage.tournamentId().equals(tournament.id()))
				.map(stage -> new TournamentStageModel()
						.name(stage.name())
						.startDate(DATE_FORMATTER.format(stage.startDate()))
						.participantModelList(getParticipants(stage))
						.gameModelList(getGames(stage.id())))
				.toList();
	}

	private List<GameModel> getGames(UUID stageId) {
		return gameList.stream()
				.filter(g -> g.tournamentStageId().equals(stageId))
				.map(g -> new GameModel()
						.name(g.name())
						.type(g.type().toString())
						.link(g.link())
						.round(g.round().toString())
						.completed(g.completed())
						.playerModelList(getPlayers(g.id())))
				.toList();
	}

	private List<PlayerModel> getPlayers(UUID gameId) {
		return playerList.stream()
				.filter(player -> player.gameId().equals(gameId))
				.map(player -> new PlayerModel()
						.name(Optional.ofNullable(player.participantId())
								.map(participantId -> participantList.stream()
										.filter(p -> p.id().equals(participantId))
										.findFirst()
										.map(Participant::name))
								.orElseThrow(() -> new RavenscoreException("Invalid player-participant connection."))
								.orElse(capitalizeFirstLetter(player.house())))
						.house(capitalizeFirstLetter(player.house()))
						.castles(player.castles())
						.score(player.score())
						.penaltyPoints(player.penaltyPoints()))
				.toList();
	}

	private List<ParticipantModel> getParticipants(TournamentStage stage) {
		val participantIds = Arrays.stream(stage.participantIdList()).toList();
		return participantList.stream()
				.filter(p -> participantIds.contains(p.id()))
				.map(participant -> {
					val games = gameList.stream()
							.filter(game -> Arrays.stream(game.participantIdList()).toList().contains(participant.id()))
							.toList();
					val players = playerList.stream()
							.filter(player -> participant.id().equals(player.participantId()))
							.toList();
					val completedGames = games.stream().filter(Game::completed).toList().size();
					val points = new PlayerPointsCalculator(games, players, tournament.scoring()).calculatePoints();
					return new ParticipantModel()
							.name(participant.name())
							.profileLinks(Arrays.asList(participant.profileLinks()))
							.games(completedGames + "/" + games.size())
							.points(points)
							.penaltyPoints(players.stream().map(Player::penaltyPoints).reduce(Integer::sum).orElse(0))
							.wins(players.stream().map(Player::rank).filter(r -> r == 1).toList().size())
							.avgPoints(completedGames == 0 ? null
									: DECIMAL_FORMATTER
											.format(BigDecimal.valueOf(points).divide(BigDecimal.valueOf(completedGames), 2, UP)));
				})
				.toList();
	}

	@RequiredArgsConstructor
	public static class PlayerPointsCalculator {

		private final List<Game>	games;
		private final List<Player>	players;
		private final Scoring		scoring;

		public Integer calculatePoints() {
			val gamesMap = new LinkedHashMap<UUID, Game>();
			games.stream()
					.filter(Game::completed)
					.toList()
					.forEach(g -> gamesMap.put(g.id(), g));
			return switch (scoring) {
				case POSITION_PLUS_CITIES -> players.stream()
						.filter(p -> gamesMap.containsKey(p.gameId()))
						.map(p -> (gamesMap.get(p.gameId()).participantIdList().length - p.rank() + p.castles() + (p.rank() == 1 ? 3 : 0)))
						.reduce(Integer::sum)
						.orElse(0);
			};
		}
	}
}

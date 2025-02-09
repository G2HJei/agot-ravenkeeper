package xyz.zlatanov.ravenscore.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.ravenscore.domain.domain.Game;
import xyz.zlatanov.ravenscore.domain.domain.TournamentStage;
import xyz.zlatanov.ravenscore.domain.repository.*;
import xyz.zlatanov.ravenscore.model.tourdetails.TournamentDetailsModel;
import xyz.zlatanov.ravenscore.security.TourInfoService;
import xyz.zlatanov.ravenscore.service.builder.tourdetails.RankingMode;
import xyz.zlatanov.ravenscore.service.builder.tourdetails.TournamentDetailsBuilder;

@Service
@RequiredArgsConstructor
public class TournamentDetailsService {

	private final TourInfoService			tourInfoService;
	private final TournamentRepository		tournamentRepository;
	private final ParticipantRepository		participantRepository;
	private final SubstituteRepository		substituteRepository;
	private final TournamentStageRepository	tournamentStageRepository;
	private final GameRepository			gameRepository;
	private final PlayerRepository			playerRepository;

	@Transactional(readOnly = true)
	public TournamentDetailsModel getTournamentDetails(UUID tournamentId, RankingMode rankingMode) {
		val tournament = tournamentRepository.findById(tournamentId).orElseThrow();
		val tourStages = tournamentStageRepository.findByTournamentIdInOrderByStartDateDesc(List.of(tournament.id()));
		val stageIds = tourStages.stream().map(TournamentStage::id).toList();
		val participants = participantRepository.findByIdInOrderByName(
				tourStages.stream()
						.flatMap(ts -> Arrays.stream(ts.participantIdList()))
						.distinct()
						.toList());
		val substitutes = substituteRepository.findByTournamentIdInOrderByName(List.of(tournamentId));
		val games = gameRepository.findByTournamentStageIdInOrderByTypeAscNameAsc(stageIds);
		val gameIds = games.stream().map(Game::id).toList();
		val players = playerRepository.findByGameIdInOrderByPointsDesc(gameIds);
		return new TournamentDetailsBuilder(tourInfoService.tournamentIsUnlocked(tournamentId), tournament, tourStages, substitutes,
				participants, games, players, rankingMode).build();
	}

}

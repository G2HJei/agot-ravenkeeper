package xyz.zlatanov.ravenscore.web.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class TournamentSummaryModel {

	private String	id;
	private String	name;
	private Integer	numberOfParticipants;
	private String	startDate;
	private String	lastStage;
}

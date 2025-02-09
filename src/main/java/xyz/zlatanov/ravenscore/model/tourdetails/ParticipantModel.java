package xyz.zlatanov.ravenscore.model.tourdetails;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import xyz.zlatanov.ravenscore.service.builder.tourdetails.GameWin;

@Data
@Accessors(fluent = true)
public class ParticipantModel {

	private String				id;
	private String				name;
	private String				replacedLabel;
	private List<ProfileLink>	profileLinks	= new ArrayList<>();
	private Integer				games;
	private Integer				points;
	private Integer				penaltyPoints;
	private List<GameWin>		wins;
	private Integer				cleanWins;
	private Double				avgPtsDouble;
	private String				avgPoints;

	public Integer score() {
		return points - penaltyPoints;
	}

}

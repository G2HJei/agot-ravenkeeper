package xyz.zlatanov.ravenscore.web.model.tourdetails;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@Getter(onMethod = @__(@JsonProperty)) // allow (de)serialization of fluent accessors
public class UnlockRequest {

	private UUID	tournamentId;
	private String	tournamentKey;
}

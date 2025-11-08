package dk.clanie.mongo.entity;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractEntity {

	@Id
	private UUID id;
	
	@Version
	private Long version;

	@CreatedDate
	private Instant createdDate;	

	@LastModifiedDate
	private Instant lastModifiedDate;	


}

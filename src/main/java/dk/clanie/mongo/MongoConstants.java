package dk.clanie.mongo;

import static dk.clanie.core.Utils.asUuid;

import java.util.UUID;

public final class MongoConstants {


	private MongoConstants() {
		// Not meant to be instantiated
	}

	public static final UUID ADMIN_TENANT_ID = asUuid("00000000-0000-0000-0000-000000000000");


}

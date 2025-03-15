package dk.clanie.mongo.convert;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class DateToZonedDateTimeConverter implements Converter<Date, ZonedDateTime> {


	@Override
	public ZonedDateTime convert(Date source) {
		return source.toInstant().atZone(ZoneOffset.systemDefault());
	}


}

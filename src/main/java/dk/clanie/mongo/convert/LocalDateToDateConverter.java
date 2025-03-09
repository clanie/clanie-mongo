package dk.clanie.mongo.convert;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class LocalDateToDateConverter implements Converter<LocalDate, Date> {


	private static final LocalDate MIN_SUPPORTED_DATE = LocalDate.of(1, 1, 1); // Closest to MongoDB's minimum date
	private static final LocalDate MAX_SUPPORTED_DATE = LocalDate.of(9999, 12, 31); // Closest to MongoDB's maximum date


	@Override
	public Date convert(LocalDate source) {
		if (source.isBefore(MIN_SUPPORTED_DATE)) {
			source = MIN_SUPPORTED_DATE;
		} else if (source.isAfter(MAX_SUPPORTED_DATE)) {
			source = MAX_SUPPORTED_DATE;
		}
		return Date.from(source.atStartOfDay(ZoneOffset.UTC).toInstant());
	}


}

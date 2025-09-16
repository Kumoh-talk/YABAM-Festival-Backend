package com.pos.global.id;

import java.lang.reflect.Field;

public class IdMapper {
	public static void idMapping(Object entity, Long id) {
		try {
			Field field = entity.getClass().getDeclaredField("id");

			field.setAccessible(true);

			field.set(entity, id);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}

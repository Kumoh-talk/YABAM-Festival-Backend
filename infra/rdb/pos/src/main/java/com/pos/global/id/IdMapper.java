package com.pos.global.id;

import static java.util.Objects.*;

import java.lang.reflect.Field;

public class IdMapper {
	public static void idMapping(Object entity, Long id) {
		requireNonNull(entity, "entity must not be null");
		requireNonNull(id, "id must not be null");

		try {
			Field field = findField(entity.getClass(), "id");
			if (field == null) {
				throw new IllegalStateException("No field named 'id' found on " + entity.getClass().getName());
			}

			Class<?> type = field.getType();
			if (!(type == Long.class || type == long.class)) {
				throw new IllegalStateException("'id' field must be Long/long but was " + type.getName());
			}

			field.setAccessible(true);
			field.set(entity, id);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Failed to set 'id' via reflection", e);
		}
	}

	private static Field findField(Class<?> type, String name) {
		for (Class<?> cls = type; cls != null; cls = cls.getSuperclass()) {
			try {
				return cls.getDeclaredField(name);
			} catch (NoSuchFieldException ignore) {
			}
		}
		return null;
	}
}

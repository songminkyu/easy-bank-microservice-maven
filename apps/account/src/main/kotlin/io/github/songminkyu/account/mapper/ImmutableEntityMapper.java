package io.github.songmyu.account.mapper;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

public interface EntityMapper<D, E> {
    E toEntity(D dto);

    D toDto(E entity);

    /**
    * Convert a DTO list to an Entity list (thread-safe)
    * @param dtoList: A list of DTOs to convert
    * @return a thread-safe Entity list
    */
    default List<E> toEntity(Collection<D> dtoList) {
        if (dtoList == null) {
            return new CopyOnWriteArrayList<>();
        }
        return dtoList.stream()
                .map(this::toEntity)
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
    }

    /**
    * Convert a list of entities to a list of DTOs (thread-safe)
    * @param entityList A list of entities to convert
    * @return a thread-safe list of DTOs
    */
    default List<D> toDto(Collection<E> entityList) {
        if (entityList == null) {
            return new CopyOnWriteArrayList<>();
        }
        return entityList.stream()
                .map(this::toDto)
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
    }

    /**
    * Convert to an immutable list (safest method)
    * ​​@param dtoList List of DTOs to convert
    * @return List of immutable entities
    */
    default List<E> toEntityImmutable(Collection<D> dtoList) {
        if (dtoList == null) {
            return List.of();
        }
        return dtoList.stream()
                .map(this::toEntity)
                .toList();
    }

    /**
    * Convert to an immutable list (safest method)
    * ​​@param entityList List of entities to convert
    * @return List of immutable DTOs
    */
    default List<D> toDtoImmutable(Collection<E> entityList) {
        if (entityList == null) {
            return List.of();
        }
        return entityList.stream()
                .map(this::toDto)
                .toList();
    }

    @Named("partialUpdate")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void partialUpdate(@MappingTarget E entity, D dto);
}
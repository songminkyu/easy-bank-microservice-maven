package io.github.songminkyu.account.util;

import static java.util.stream.Collectors.toSet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import lombok.experimental.UtilityClass;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

@UtilityClass
public class FieldScannerUtil {

    public static Set<Field> findFieldsWithAnnotation(
            String basePackage,
            Class<? extends Annotation> classAnnotation,
            Class<? extends Annotation> fieldAnnotation) {
        Assert.notNull(basePackage, "Base package must not be null");
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        try {
            scanner.addIncludeFilter(new AnnotationTypeFilter(classAnnotation));

            return scanner.findCandidateComponents(basePackage)
                    .stream()
                    .map(bd -> {
                        try {
                            return Class.forName(bd.getBeanClassName());
                        } catch (ClassNotFoundException e) {
                            throw new IllegalStateException("Error occurred while scanning classes", e);
                        }
                    })
                    .flatMap(clazz -> {
                        Set<Field> annotatedFields = new HashSet<>();
                        ReflectionUtils.doWithFields(clazz, field -> {
                            if (field.isAnnotationPresent(fieldAnnotation)) {
                                annotatedFields.add(field);
                            }
                        });
                        return annotatedFields.stream();
                    })
                    .collect(toSet());

        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid base package provided", ex);
        } finally {
            scanner.clearCache();
        }
    }
}

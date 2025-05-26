package io.github.susimsek.account.remote;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

class RemoteCallerServiceTest {

    @Test
    void testReflection() {
        // Spring uygulamasını başlat
        String packageName = "io.github.susimsek.account.remote";
        Set<Field> remoteEndpointFields = findFieldsWithAnnotation(packageName, RemoteEndpoint.class);

        // Sonuçları yazdır
        System.out.println("@RemoteEndpoint annotation'ına sahip alanlar:");
        for (Field field : remoteEndpointFields) {
            System.out.println(field.getDeclaringClass().getName() + " - " + field.getName());
        }
    }

    public static Set<Field> findFieldsWithAnnotation(String packageName, Class<? extends Annotation> annotation) {
        Set<Field> fields = new HashSet<>();
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Component.class));
        var beanDefinitions = scanner.findCandidateComponents(packageName);
        for (org.springframework.beans.factory.config.BeanDefinition bd : beanDefinitions) {
            try {
                Class<?> cls = Class.forName(bd.getBeanClassName());
                ReflectionUtils.doWithFields(cls, field -> {
                    if (AnnotationUtils.findAnnotation(field, annotation) != null) {
                        fields.add(field);
                    }
                });
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return fields;
    }

}
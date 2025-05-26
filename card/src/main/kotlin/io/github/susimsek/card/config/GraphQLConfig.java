package io.github.susimsek.card.config;

import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLScalarType;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.validation.rules.OnValidationErrorStrategy;
import graphql.validation.rules.ValidationRules;
import graphql.validation.schemawiring.ValidationSchemaWiring;
import io.github.susimsek.card.graphql.directive.SchemaDirective;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

@Configuration(proxyBeanMethods = false)
public class GraphQLConfig {

    @Bean
    public GraphQLScalarType dateTimeScalarType() {
        return ExtendedScalars.DateTime;
    }

    @Bean
    public SchemaDirectiveWiring validationSchemaDirective() {
        var validationRules = ValidationRules.newValidationRules()
            .onValidationErrorStrategy(OnValidationErrorStrategy.RETURN_NULL)
            .build();
        return new ValidationSchemaWiring(validationRules);
    }

    @Bean
    public RuntimeWiringConfigurer graphqlConfigurer(
        List<GraphQLScalarType> graphQLScalarTypes,
        SchemaDirectiveWiring validationSchemaDirective,
        List<SchemaDirective> schemaDirectives
    ) {
        return builder -> {
            graphQLScalarTypes.forEach(
                builder::scalar);
            builder.directiveWiring(validationSchemaDirective);
            schemaDirectives.forEach(schemaDirective -> builder.directive(
                schemaDirective.getName(), schemaDirective.getDirective()));
        };
    }
}

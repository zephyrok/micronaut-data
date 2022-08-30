package io.micronaut.data.document.mongodb.repositories;

import io.micronaut.data.cosmos.annotation.CosmosRepository;
import io.micronaut.data.document.tck.repositories.CitizenRepository;

@CosmosRepository
public interface CosmosCitizenRepository extends CitizenRepository {
}
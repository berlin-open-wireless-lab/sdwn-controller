package de.tuberlin.inet.sdwn.core.api.entity;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

public interface SdwnEntityDecoder<T extends SdwnEntity> {

    T fromJson(ObjectNode node) throws IllegalArgumentException;

    List<T> fromJson(ArrayNode nods) throws IllegalArgumentException;
}

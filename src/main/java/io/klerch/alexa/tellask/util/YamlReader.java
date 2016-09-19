package io.klerch.alexa.tellask.util;

import io.klerch.alexa.tellask.model.AlexaOutput;
import io.klerch.alexa.tellask.schema.UtteranceReader;
import org.apache.commons.lang3.Validate;
import org.yaml.snakeyaml.Yaml;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class YamlReader {
    private final UtteranceReader utteranceReader;
    private final Map<String, List<Object>> phrases = new HashMap<>();
    private final String locale;

    public YamlReader(final UtteranceReader utteranceReader, final String locale) {
        this.utteranceReader = utteranceReader;
        this.locale = locale;
    }

    public List<String> getUtterances(final AlexaOutput output) {
        return getUtterances(output.getIntentName());
    }

    public Optional<String> getRandomUtterance(final AlexaOutput output) {
        return getRandomUtterance(output.getIntentName());
    }

    public List<String> getUtterances(final String intentName) {
        return getPhrasesForIntent(intentName, 0);
    }

    public Optional<String> getRandomUtterance(final String intentName) {
        return getRandomOf(getPhrasesForIntent(intentName, 0));
    }

    public List<String> getReprompts(final AlexaOutput output) {
        return getReprompts(output.getIntentName());
    }

    public Optional<String> getRandomReprompt(final AlexaOutput output) {
        return getRandomReprompt(output.getIntentName());
    }

    public List<String> getReprompts(final String intentName) {
        return getPhrasesForIntent(intentName, 1);
    }

    public Optional<String> getRandomReprompt(final String intentName) {
        return getRandomOf(getPhrasesForIntent(intentName, 1));
    }

    private List<Object> loadUtterances(final String intentName) {
        // leverage reader to get yaml with utterances
        final Map<?, ?> content = new Yaml().loadAs(utteranceReader.read(locale), Map.class);

        // flatten yaml strings values beneath intent node of interest
        return content.entrySet().stream()
                .filter(k -> k.getKey().equals(intentName))
                .flatMap(k -> flatten(k.getValue())).collect(Collectors.toList());
    }

    /**
     * Recursively go along yaml nodes beneath the given one to flatten string values
     * @param o YAML node point of start
     * @return flattened values beneath given YAML node
     */
    private Stream<Object> flatten(final Object o) {
        if (o instanceof Map<?, ?>) {
            return ((Map<?, ?>) o).values().stream().flatMap(this::flatten);
        }
        return Stream.of(o);
    }

    @SuppressWarnings("unchecked")
    private List<String> getPhrasesForIntent(final String intentName, final Integer index) {
        Validate.notBlank(intentName, "Intent name is null or empty.");
        // return list of utterances if already read out and saved to local list
        final List<Object> contents = phrases.entrySet().stream()
                .filter(k -> k.getKey().equals(intentName))
                .findFirst()
                // otherwise load utterances from resource of utterance reader
                .orElse(new AbstractMap.SimpleEntry<>(intentName, loadUtterances(intentName)))
                .getValue();
        // cache the result
        phrases.putIfAbsent(intentName, contents);

        final List<String> utterances = new ArrayList<>();

        if (contents.size() > index) {
            // group node assumed to be an array list
            Object assumedUtteranceCollection = contents.get(index);

            // if utterances (not reprompts) are desired and YAML node(s) are Strings then
            // there's no container-node like "utterances:" but instant enumeration of utterances
            if (index == 0 && assumedUtteranceCollection instanceof String) {
                utterances.addAll(contents.stream().map(String::valueOf).collect(Collectors.toList()));
            }
            else if (assumedUtteranceCollection instanceof ArrayList) {
                // parse each phrase as string and add to return collection
                ((ArrayList)assumedUtteranceCollection).forEach(utterance -> utterances.add(String.valueOf(utterance)));
            }
        }
        return utterances;
    }

    private Optional<String> getRandomOf(final List<String> list) {
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(new Random().nextInt(list.size())));
    }
}

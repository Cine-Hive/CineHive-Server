package com.example.CineHive.client.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * TMDB API의 External IDs 응답을 담는 DTO입니다.
 */
public record TmdbExternalIdsResponse(
        @JsonProperty("imdb_id") String imdbId,
        @JsonProperty("freebase_mid") String freebaseMid,
        @JsonProperty("freebase_id") String freebaseId,
        @JsonProperty("tvdb_id") Integer tvdbId,
        @JsonProperty("tvrage_id") Integer tvrageId,
        @JsonProperty("wikidata_id") String wikidataId,
        @JsonProperty("facebook_id") String facebookId,
        @JsonProperty("instagram_id") String instagramId,
        @JsonProperty("twitter_id") String twitterId
) {}
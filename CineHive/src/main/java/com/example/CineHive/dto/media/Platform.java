package com.example.CineHive.dto.media;

import com.example.CineHive.dto.response.PlatformOptionDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum Platform {
    NETFLIX(213L, "Netflix", "/t2yyOv4xD9xpcGPNavKrDdGFEly.jpg"),
    DISNEY_PLUS(2739L, "Disney+", "/uzKjDo45H33D4nJ2T2aC2L8b20.jpg"),
    AMAZON_PRIME(1024L, "Amazon Prime Video", "/emthSpie82kbr2s4fM0M3aL2h29.jpg"),
    HBO(49L, "HBO", "/tuomPhY2UtuPTqqFnKMVHvroqBA.jpg"),
    APPLE_TV_PLUS(2552L, "Apple TV+", "/4f3T3Z1yK2dYvKaS3d2p2y9N2B.jpg"),
    HULU(453L, "Hulu", "/pqUTCleNUiTLAVaH28p3OP_2hA.jpg"),
    WAVVE(3321L, "Wavve", "/1TB2a264J0gds6Teyvvr9a46L9E.jpg"),
    TVN(318L, "tvN", "/kGRavMqU4Oad2b2Hza53v2d2jaA.jpg"),
    SBS(67L, "SBS", "/j61aM2N2dK3mOo4L1so2pA14T3A.jpg"),
    KBS(62L, "KBS", "/11G3GzYg3g2iT3aB2i2b0O6om3.jpg"),
    MBC(74L, "MBC", "/wK2g6sY2yAl266eP3w5epDkw5dG.jpg"),
    JTBC(269L, "JTBC", "/sL43iR2nESpgh1g3d7s2iHw3Gz.jpg");

    private final Long id;
    private final String displayName;
    private final String logoPath;


    public static Platform fromString(String text) {
        return Arrays.stream(values())
                .filter(p -> p.name().equalsIgnoreCase(text))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown platform: " + text));
    }
}
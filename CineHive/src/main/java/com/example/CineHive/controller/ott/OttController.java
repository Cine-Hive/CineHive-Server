package com.example.CineHive.controller.ott;

import com.example.CineHive.dto.ott.OttDto;
import com.example.CineHive.service.ott.OttService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/ott")
@RequiredArgsConstructor
public class OttController {

    private final OttService ottService;

    @GetMapping("/{providerId}")
    public ResponseEntity<List<OttDto>> getMoviesByProvider(@PathVariable int providerId) {
        List<OttDto> movies = ottService.getMoviesByProvider(providerId);
        return ResponseEntity.ok(movies);
    }
}

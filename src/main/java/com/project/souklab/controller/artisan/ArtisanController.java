package com.project.souklab.controller.artisan;

import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.dto.profile.ArtisanPublicViewDTO;
import com.project.souklab.service.artisan.ArtisanProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/artisan")
@RequiredArgsConstructor
public class ArtisanController {

    private final ArtisanProfileService artisanProfileService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ArtisanPublicViewDTO>> getArtisanProfile(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(artisanProfileService.getArtisanProfile(id)));
    }
}

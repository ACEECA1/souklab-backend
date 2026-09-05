# User & Avatar DTO Package (`com.project.souklab.dto.user`)

Contracts for administrative user discipline and avatar gallery management.

---

## Classes Reference

| DTO Class | Direction | Description |
| :--- | :---: | :--- |
| [`BanRequestDTO`](BanRequestDTO.java) | Inbound | Administrative ban payload containing mandatory disciplinary justification `reason`. |
| [`TimeoutRequestDTO`](TimeoutRequestDTO.java) | Inbound | Administrative temporary suspension payload with positive `minutes` duration and `reason`. |
| [`AvatarResponseDTO`](AvatarResponseDTO.java) | Outbound | Avatar gallery item representation containing ID, URLs for all three resolution tiers (thumbnail, medium, full), and `isActive` boolean. |

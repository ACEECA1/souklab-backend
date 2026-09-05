# Formateur DTO Package (`com.project.souklab.dto.formateur`)

Data contracts for the artisan teacher accreditation lifecycle.

---

## Classes Reference

| DTO Class | Usage | Description |
| :--- | :---: | :--- |
| [`FormateurRequestDTO`](FormateurRequestDTO.java) | Inbound | Artisan application payload containing teaching motivation statement. |
| [`FormateurApproveDTO`](FormateurApproveDTO.java) | Inbound | Administrative approval payload containing optional review note. |
| [`FormateurRejectDTO`](FormateurRejectDTO.java) | Inbound | Administrative rejection payload containing admin note, `canReapply` boolean, and optional `cooldownUntil` date. |
| [`FormateurGrantDTO`](FormateurGrantDTO.java) | Inbound | Direct administrative accreditation grant payload with justification note. |
| [`FormateurRevokeDTO`](FormateurRevokeDTO.java) | Inbound | Administrative accreditation revocation payload with mandatory reason. |
| [`FormateurCooldownOverrideDTO`](FormateurCooldownOverrideDTO.java) | Inbound | Administrative override payload to lift cooldown or re-enable applications. |
| [`FormateurRequestResponseDTO`](FormateurRequestResponseDTO.java) | Outbound | Comprehensive status representation including request status, admin note, cooldown details, and decision audit dates. |

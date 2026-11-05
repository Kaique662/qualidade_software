package com.example.educationalqualityproject.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "resource_blocks")
public class ResourceBlock {

    @Id
    private String id;
    private String resourceId;
    private String reason;
    private String responsible;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    // PENDING aguarda revisao; somente CONFIRMED impede novas alocacoes.
    private BlockStatus status = BlockStatus.PENDING;
    // Guarda as atividades que a equipe deve revisar antes de confirmar o bloqueio.
    private List<String> affectedAllocationIds = new ArrayList<>();
    // Registra as operacoes do bloqueio sem apagar os eventos anteriores.
    private List<BlockHistoryEntry> history = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getResponsible() { return responsible; }
    public void setResponsible(String responsible) { this.responsible = responsible; }
    public LocalDateTime getStartsAt() { return startsAt; }
    public void setStartsAt(LocalDateTime startsAt) { this.startsAt = startsAt; }
    public LocalDateTime getEndsAt() { return endsAt; }
    public void setEndsAt(LocalDateTime endsAt) { this.endsAt = endsAt; }
    public BlockStatus getStatus() { return status; }
    public void setStatus(BlockStatus status) { this.status = status; }
    public List<String> getAffectedAllocationIds() { return affectedAllocationIds; }
    public void setAffectedAllocationIds(List<String> affectedAllocationIds) { this.affectedAllocationIds = affectedAllocationIds; }
    public List<BlockHistoryEntry> getHistory() { return history; }
    public void setHistory(List<BlockHistoryEntry> history) { this.history = history; }

    public enum BlockStatus {
        PENDING,
        CONFIRMED,
        CANCELLED
    }

    public static class BlockHistoryEntry {
        private String action;
        private String actor;
        private LocalDateTime occurredAt;

        public BlockHistoryEntry() { }

        public BlockHistoryEntry(String action, String actor, LocalDateTime occurredAt) {
            this.action = action;
            this.actor = actor;
            this.occurredAt = occurredAt;
        }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getActor() { return actor; }
        public void setActor(String actor) { this.actor = actor; }
        public LocalDateTime getOccurredAt() { return occurredAt; }
        public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
    }
}
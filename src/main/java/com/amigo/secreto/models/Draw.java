package com.amigo.secreto.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "tb_draw")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Draw {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "group_id")
    private Group group;

    @ElementCollection
    @CollectionTable(name = "draw_pairs", joinColumns = @JoinColumn(name = "draw_id"))
    @MapKeyJoinColumn(name = "giver_id")
    @Column(name = "receiver_id")
    private Map<UUID, UUID> pairs;
}

package org.moneymanagement.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name="tbl_profile")
public class ProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false , length = 100)
    private String fullName;

    @Column(unique = true , nullable = false , length = 100)
    private String email;

    @Column(nullable = false , length = 150)
    private String password;

    private String profilePictureUrl;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private Boolean isActive;

    private String activationToken;


    @PrePersist
    public void prePersist(){
        if(this.isActive == null){
            isActive = false;
        }
    }

}
